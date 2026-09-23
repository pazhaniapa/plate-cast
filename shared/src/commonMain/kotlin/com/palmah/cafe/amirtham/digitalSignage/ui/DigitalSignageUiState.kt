package com.palmah.cafe.amirtham.digitalSignage.ui

import com.palmah.cafe.amirtham.digitalSignage.model.MAX_SIGNAGE_IMAGES

const val BOARD_NAME_MAX_LENGTH = 25

data class DigitalSignageUiState(
    val isLoading: Boolean = false,
    val generatedImageBytes: ByteArray? = null,
    val boardName: String = "",
    val dishForms: List<DishFormState> = List(MAX_SIGNAGE_IMAGES) { DishFormState() },
    val activeDishIndex: Int? = null,
)
