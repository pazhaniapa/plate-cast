package com.palmah.cafe.amirtham.digitalSignage.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.palmah.cafe.amirtham.digitalSignage.ui.DigitalSignageDisplayUiState
import com.palmah.cafe.amirtham.digitalSignage.useCase.DigitalSignageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = Logger.withTag("DigitalSignageDisplay")

class DigitalSignageDisplayViewModel(
    private val digitalSignageUseCase: DigitalSignageUseCase = DigitalSignageUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalSignageDisplayUiState())
    val uiState: StateFlow<DigitalSignageDisplayUiState> = _uiState

    init {
        loadImages()
    }

    fun loadImages() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val imageUrls = digitalSignageUseCase.getDigitalSignageBoards()
                    .map { it.imageUrl }
                    .filter { it.isNotBlank() }
                _uiState.update { it.copy(isLoading = false, imageUrls = imageUrls) }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load digital signage images" }
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
