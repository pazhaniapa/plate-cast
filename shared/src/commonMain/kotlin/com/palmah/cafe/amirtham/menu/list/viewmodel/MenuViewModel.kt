package com.palmah.cafe.amirtham.menu.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.palmah.cafe.amirtham.common.repository.FirebaseUserRepository
import com.palmah.cafe.amirtham.common.repository.UserRepository
import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageInfo
import com.palmah.cafe.amirtham.digitalSignage.useCase.DigitalSignageUseCase
import com.palmah.cafe.amirtham.menu.list.model.Menu
import com.palmah.cafe.amirtham.menu.list.model.MenuCategory
import com.palmah.cafe.amirtham.menu.list.model.MenuItem
import com.palmah.cafe.amirtham.menu.list.model.RestaurantMenuItem
import com.palmah.cafe.amirtham.menu.list.ui.EditMenuItemFormState
import com.palmah.cafe.amirtham.menu.list.ui.MenuUiState
import com.palmah.cafe.amirtham.menu.list.ui.applyEdits
import com.palmah.cafe.amirtham.menu.list.usecase.MenuUseCase
import com.palmah.cafe.amirtham.utils.downloadImageBytes
import io.github.ismoy.imagepickerkmp.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.picker.PhotoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val MENU_SIGNAGE_BOARD_NAME = "Menu"

private val logger = Logger.withTag("PlateCastAi")

class MenuViewModel(
    private val menuUseCase: MenuUseCase = MenuUseCase(),
    private val digitalSignageUseCase: DigitalSignageUseCase = DigitalSignageUseCase(),
    private val userRepository: UserRepository = FirebaseUserRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState

    init {
        loadMenuItems()
    }

    private fun loadMenuItems() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val items = menuUseCase.getMenuItems()
                _uiState.update { it.copy(isLoading = false, menuItems = items) }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load saved menu items" }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onMenuItemClick(item: MenuItem) {
        _uiState.update { it.copy(itemPendingEdit = item, editForm = EditMenuItemFormState.from(item)) }
    }

    fun dismissEditMenuItem() {
        _uiState.update { it.copy(itemPendingEdit = null) }
    }

    fun onEditNameChange(value: String) {
        _uiState.update { it.copy(editForm = it.editForm.copy(name = value)) }
    }

    fun onEditPriceChange(value: String) {
        _uiState.update { it.copy(editForm = it.editForm.copy(price = value)) }
    }

    fun onEditTimingsChange(value: String) {
        _uiState.update { it.copy(editForm = it.editForm.copy(timings = value)) }
    }

    fun onEditDescriptionChange(value: String) {
        _uiState.update { it.copy(editForm = it.editForm.copy(description = value)) }
    }

    /** Persists [uiState]'s [MenuUiState.editForm] edits to [MenuUiState.itemPendingEdit] and reflects them in [uiState] on success. */
    fun saveMenuItemEdits() {
        val item = _uiState.value.itemPendingEdit?.applyEdits(_uiState.value.editForm) ?: return
        _uiState.update { it.copy(itemPendingEdit = null) }
        viewModelScope.launch {
            try {
                menuUseCase.updateMenuItem(item)
                _uiState.update { state ->
                    state.copy(menuItems = state.menuItems.map { if (it.id == item.id) item else it })
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to update menu item ${item.id}" }
            }
        }
    }

    /** Called once a photo has been captured/picked; extracts menu items from it via Gemini. */
    fun onPhotoSelected(photo: PhotoResult) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val imageBytes = withContext(Dispatchers.Default) { photo.loadBytes() }
                val imageFormat = photo.mimeType?.substringAfterLast('/') ?: "jpeg"
                val items = menuUseCase.extractMenuItems(imageBytes, imageFormat)
                logger.i { "PlateCast AI response: items=${items.size}" }
                menuUseCase.saveMenuItems(items)
                _uiState.update { it.copy(isLoading = false, menuItems = items) }
                saveMenuImage(imageBytes, photo.mimeType ?: "image/$imageFormat")
            } catch (e: Exception) {
                logger.e(e) { "PlateCast AI request failed" }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun saveMenuImage(imageBytes: ByteArray, mimeType: String) {
        try {
            menuUseCase.saveMenuImage(imageBytes, mimeType)
        } catch (e: Exception) {
            logger.e(e) { "Failed to upload/save menu image" }
        }
    }

    /**
     * Generates a digital signage board for the current menu, using the menu's reference image
     * (saved under the signed-in user's `menuMetaData`) as the sole input image, and uploads/saves
     * the result exactly like [DigitalSignageViewModel][com.palmah.cafe.amirtham.digitalSignage.viewModel.DigitalSignageViewModel].
     */
    fun generateDigitalSignageFromMenu() {
        val menu = _uiState.value.menuItems.toMenu()
        val jsonString = Json.encodeToString(menu)
        logger.i { "Menu JSON: $jsonString" }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val imageUrl = menuUseCase.getMenuImageUrl()
                if (imageUrl == null) {
                    logger.w { "Cannot generate digital signage image: no menu reference image found" }
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                val (imageBytes, imageFormat) = downloadImageBytes(imageUrl)
                val signageInfo = DigitalSignageInfo(
                    name = MENU_SIGNAGE_BOARD_NAME,
                    imageBytes = imageBytes,
                    imageFormat = imageFormat,
                )

                val response = digitalSignageUseCase.generateMenuDigitalSignageImage(
                    listOf(signageInfo),
                    jsonString,
                )
                val generatedImage = response.images.firstOrNull()
                _uiState.update { it.copy(isLoading = false) }

                if (generatedImage != null) {
                    uploadGeneratedSignageImage(generatedImage.bytes, generatedImage.mimeType)
                }
            } catch (e: Exception) {
                logger.e(e) { "Digital signage image generation from menu failed" }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun uploadGeneratedSignageImage(imageBytes: ByteArray, mimeType: String) {
        try {
            val userInfo = userRepository.getCurrentUserInfo()
            val brand = userInfo?.brand
            val outlet = userInfo?.outlet
            if (brand.isNullOrBlank() || outlet.isNullOrBlank()) {
                logger.w { "Cannot upload digital signage image: no signed-in user brand/outlet found" }
                return
            }
            val downloadUrl = digitalSignageUseCase.uploadGeneratedImage(imageBytes, mimeType, brand, outlet)
            digitalSignageUseCase.saveDigitalSignageUrl(brand, outlet, downloadUrl, MENU_SIGNAGE_BOARD_NAME)
            logger.i { "Uploaded menu digital signage image (contentType=$mimeType) downloadUrl:$downloadUrl" }
        } catch (e: Exception) {
            logger.e(e) { "Failed to upload menu digital signage image" }
        }
    }

    private fun List<MenuItem>.toMenu(): Menu {
        val categories = groupBy { it.categoryName }.map { (categoryName, items) ->
            MenuCategory(
                categoryName = categoryName,
                timings = items.first().categoryTimings,
                items = items.map {
                    RestaurantMenuItem(
                        name = it.name,
                        price = it.price,
                        timings = it.timings,
                        description = it.description,
                    )
                },
            )
        }
        return Menu(categories = categories)
    }
}
