package com.palmah.cafe.amirtham.auth.ui

data class AuthScreenUiState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)
