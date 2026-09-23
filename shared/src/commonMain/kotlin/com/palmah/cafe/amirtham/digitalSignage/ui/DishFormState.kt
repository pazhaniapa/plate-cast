package com.palmah.cafe.amirtham.digitalSignage.ui

data class DishFormState(
    val imageBytes: ByteArray? = null,
    val imageFormat: String = "jpeg",
    val name: String = "",
    val price: String = "",
    val extraInfo: String = "",
)
