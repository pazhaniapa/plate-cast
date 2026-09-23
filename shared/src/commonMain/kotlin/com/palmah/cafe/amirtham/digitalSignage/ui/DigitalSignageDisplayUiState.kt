package com.palmah.cafe.amirtham.digitalSignage.ui

data class DigitalSignageDisplayUiState(
    val isLoading: Boolean = false,
    val imageUrls: List<String> = emptyList(),
    val error: String? = null,
)
