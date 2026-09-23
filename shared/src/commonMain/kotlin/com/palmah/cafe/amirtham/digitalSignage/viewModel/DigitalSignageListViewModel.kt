package com.palmah.cafe.amirtham.digitalSignage.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.palmah.cafe.amirtham.digitalSignage.ui.DigitalSignageListUiState
import com.palmah.cafe.amirtham.digitalSignage.useCase.DigitalSignageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = Logger.withTag("DigitalSignageList")

class DigitalSignageListViewModel(
    private val digitalSignageUseCase: DigitalSignageUseCase = DigitalSignageUseCase(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalSignageListUiState())
    val uiState: StateFlow<DigitalSignageListUiState> = _uiState

    init {
        loadBoards()
    }

    fun loadBoards() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val boards = digitalSignageUseCase.getDigitalSignageBoards()
                _uiState.update { it.copy(isLoading = false, boards = boards) }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load digital signage boards" }
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteBoard(boardId: String) {
        viewModelScope.launch {
            try {
                digitalSignageUseCase.deleteDigitalSignageBoard(boardId)
                _uiState.update { state -> state.copy(boards = state.boards.filterNot { it.id == boardId }) }
            } catch (e: Exception) {
                logger.e(e) { "Failed to delete digital signage board: $boardId" }
            }
        }
    }
}
