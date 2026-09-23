package com.palmah.cafe.amirtham.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.palmah.cafe.amirtham.common.repository.FirebaseUserRepository
import com.palmah.cafe.amirtham.common.repository.UserRepository
import com.palmah.cafe.amirtham.home.SideMenuUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val logger = Logger.withTag("SideMenu")

class SideMenuViewModel(
    private val userRepository: UserRepository = FirebaseUserRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SideMenuUiState())
    val uiState: StateFlow<SideMenuUiState> = _uiState

    init {
        loadUserInfo()
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val userInfo = userRepository.getCurrentUserInfo()
                _uiState.update { it.copy(userInfo = userInfo) }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load side menu user info" }
            }
        }
    }
}
