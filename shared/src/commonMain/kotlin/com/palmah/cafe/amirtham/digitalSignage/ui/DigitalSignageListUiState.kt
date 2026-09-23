package com.palmah.cafe.amirtham.digitalSignage.ui

import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageBoard

data class DigitalSignageListUiState(
    val isLoading: Boolean = false,
    val boards: List<DigitalSignageBoard> = emptyList(),
    val error: String? = null,
)
