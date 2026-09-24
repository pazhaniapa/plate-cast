package com.palmah.cafe.amirtham.digitalSignage.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.palmah.cafe.amirtham.common.repository.FirebaseUserRepository
import com.palmah.cafe.amirtham.common.repository.UserRepository
import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageInfo
import com.palmah.cafe.amirtham.digitalSignage.ui.BOARD_NAME_MAX_LENGTH
import com.palmah.cafe.amirtham.digitalSignage.ui.DigitalSignageUiState
import com.palmah.cafe.amirtham.digitalSignage.ui.DishFormState
import com.palmah.cafe.amirtham.digitalSignage.useCase.DigitalSignageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = Logger.withTag("DigitalSignage")

class DigitalSignageViewModel(
    private val digitalSignageUseCase: DigitalSignageUseCase = DigitalSignageUseCase(),
    private val userRepository: UserRepository = FirebaseUserRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalSignageUiState())
    val uiState: StateFlow<DigitalSignageUiState> = _uiState

    fun reset() {
        _uiState.value = DigitalSignageUiState()
    }

    fun onBoardNameChange(name: String) {
        if (name.length <= BOARD_NAME_MAX_LENGTH) {
            _uiState.update { it.copy(boardName = name) }
        }
    }

    /** Tracks which dish slot a just-launched camera/gallery pick should be applied to. */
    fun onActiveDishIndexChange(index: Int?) {
        _uiState.update { it.copy(activeDishIndex = index) }
    }

    fun onDishNameChange(index: Int, name: String) {
        updateDishForm(index) { it.copy(name = name) }
    }

    fun onDishPriceChange(index: Int, price: String) {
        if (price.isEmpty() || price.all { it.isDigit() || it == '.' }) {
            updateDishForm(index) { it.copy(price = price) }
        }
    }

    fun onDishExtraInfoChange(index: Int, extraInfo: String) {
        updateDishForm(index) { it.copy(extraInfo = extraInfo) }
    }

    fun onDishPhotoPicked(index: Int, imageBytes: ByteArray, imageFormat: String) {
        updateDishForm(index) { it.copy(imageBytes = imageBytes, imageFormat = imageFormat) }
    }

    private fun updateDishForm(index: Int, transform: (DishFormState) -> DishFormState) {
        _uiState.update { state ->
            state.copy(
                dishForms = state.dishForms.toMutableList().apply { this[index] = transform(this[index]) },
            )
        }
    }

    fun generateDigitalSignageImage() {
        val state = _uiState.value
        val signageInfo = state.dishForms.mapNotNull { form ->
            val bytes = form.imageBytes ?: return@mapNotNull null
            DigitalSignageInfo(
                name = form.name,
                imageBytes = bytes,
                imageFormat = form.imageFormat,
                price = form.price.toDoubleOrNull() ?: 0.0,
                extraInfo = form.extraInfo,
            )
        }
        if (signageInfo.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val response = digitalSignageUseCase.generateDigitalSignageImage(signageInfo, state.boardName)
                val generatedImage = response.images.firstOrNull()
                _uiState.update { it.copy(isLoading = false, generatedImageBytes = generatedImage?.bytes) }

                if (generatedImage != null) {
                    uploadGeneratedImage(generatedImage.bytes, generatedImage.mimeType, state.boardName)
                }
            } catch (e: Exception) {
                logger.e(e) { "Digital signage image generation failed" }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun uploadGeneratedImage(imageBytes: ByteArray, mimeType: String, boardName: String) {
        try {
            val userInfo = userRepository.getCurrentUserInfo()
            val brand = userInfo?.brand
            val outlet = userInfo?.outlet
            if (brand.isNullOrBlank() || outlet.isNullOrBlank()) {
                logger.w { "Cannot upload digital signage image: no signed-in user brand/outlet found" }
                return
            }
            val downloadUrl = digitalSignageUseCase.uploadGeneratedImage(imageBytes, mimeType, brand, outlet)
            digitalSignageUseCase.saveDigitalSignageUrl(brand, outlet, downloadUrl, boardName)
            logger.i { "Uploaded digital signage image (contentType=$mimeType) downloadUrl:$downloadUrl" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to upload digital signage image" }
        }
    }


}
