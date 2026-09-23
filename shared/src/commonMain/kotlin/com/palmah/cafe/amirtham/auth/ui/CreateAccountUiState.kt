package com.palmah.cafe.amirtham.auth.ui

import com.palmah.cafe.amirtham.auth.usecase.AuthUserInfo
import com.palmah.cafe.amirtham.auth.usecase.UserRole

data class CreateAccountUiState(
    val brand: String = "",
    val outlet: String = "",
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.USER,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val userInfo: AuthUserInfo? = null,
)
