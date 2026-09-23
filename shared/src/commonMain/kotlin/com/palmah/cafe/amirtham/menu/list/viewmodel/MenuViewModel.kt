package com.palmah.cafe.amirtham.menu.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.palmah.cafe.amirtham.menu.list.ui.MenuUiState
import com.palmah.cafe.amirtham.menu.list.usecase.MenuUseCase
import io.github.ismoy.imagepickerkmp.extensions.loadBytes
import io.github.ismoy.imagepickerkmp.picker.PhotoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val logger = Logger.withTag("PlateCastAi")

class MenuViewModel(
    private val menuUseCase: MenuUseCase = MenuUseCase(),
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
            } catch (e: Exception) {
                logger.e(e) { "PlateCast AI request failed" }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
