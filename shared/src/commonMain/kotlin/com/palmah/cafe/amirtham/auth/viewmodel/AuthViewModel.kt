package com.palmah.cafe.amirtham.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palmah.cafe.amirtham.auth.GoogleSignInHandler
import com.palmah.cafe.amirtham.auth.ui.AuthScreenUiState
import com.palmah.cafe.amirtham.auth.ui.CreateAccountUiState
import com.palmah.cafe.amirtham.auth.usecase.AuthUseCase
import com.palmah.cafe.amirtham.auth.usecase.UserRole
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object SignedOut : AuthUiState
    data class SignedIn(val user: FirebaseUser) : AuthUiState
}

class AuthViewModel(
    private val authUseCase: AuthUseCase,
) : ViewModel() {

    private val _signInScreenUiState = MutableStateFlow(AuthScreenUiState())
    val signInScreenUiState: StateFlow<AuthScreenUiState> = _signInScreenUiState

    private val _createAccountUiState = MutableStateFlow(CreateAccountUiState())
    val createAccountUiState: StateFlow<CreateAccountUiState> = _createAccountUiState

    val uiState: StateFlow<AuthUiState> = authUseCase.authState
        .map { user -> if (user != null) AuthUiState.SignedIn(user) else AuthUiState.SignedOut }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthUiState.Loading)

    // --- Sign in ---

    fun onEmailChange(value: String) {
        _signInScreenUiState.update { it.copy(email = value) }
    }

    fun onPasswordChange(value: String) {
        _signInScreenUiState.update { it.copy(password = value) }
    }

    fun submit() {
        _signInScreenUiState.update { it.copy(errorMessage = null, isSubmitting = true) }
        viewModelScope.launch {
            try {
                val state = _signInScreenUiState.value
                authUseCase.signInWithEmail(state.email, state.password)
            } catch (e: Exception) {
                _signInScreenUiState.update { it.copy(errorMessage = e.message ?: "Something went wrong") }
            } finally {
                _signInScreenUiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun signInWithGoogle(handler: GoogleSignInHandler) {
        _signInScreenUiState.update { it.copy(errorMessage = null, isSubmitting = true) }
        viewModelScope.launch {
            try {
                authUseCase.signInWithGoogle(handler)
            } catch (e: Exception) {
                _signInScreenUiState.update { it.copy(errorMessage = e.message ?: "Google sign-in failed") }
            } finally {
                _signInScreenUiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { authUseCase.signOut() }
    }

    // --- Create account ---

    fun onCreateAccountBrandChange(value: String) {
        _createAccountUiState.update { it.copy(brand = value) }
    }

    fun onCreateAccountOutletChange(value: String) {
        _createAccountUiState.update { it.copy(outlet = value) }
    }

    fun onCreateAccountEmailChange(value: String) {
        _createAccountUiState.update { it.copy(email = value) }
    }

    fun onCreateAccountPasswordChange(value: String) {
        _createAccountUiState.update { it.copy(password = value) }
    }

    fun onCreateAccountDisplayNameChange(value: String) {
        _createAccountUiState.update { it.copy(displayName = value) }
    }

    fun onCreateAccountRoleChange(value: UserRole) {
        _createAccountUiState.update { it.copy(role = value) }
    }

    fun createAccount() {
        _createAccountUiState.update { it.copy(errorMessage = null, isSubmitting = true) }
        viewModelScope.launch {
            try {
                val state = _createAccountUiState.value
                val result = authUseCase.signUpWithEmail(state.email, state.password)
                val userInfo = authUseCase.updateUserInfo(
                    authResult = result,
                    brand = state.brand,
                    outlet = state.outlet,
                    displayName = state.displayName,
                    role = state.role,
                )
                _createAccountUiState.update { it.copy(userInfo = userInfo) }
            } catch (e: Exception) {
                _createAccountUiState.update { it.copy(errorMessage = e.message ?: "Something went wrong") }
            } finally {
                _createAccountUiState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
