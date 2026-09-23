package com.palmah.cafe.amirtham.digitalSignage.model

const val MAX_SIGNAGE_IMAGES = 3

data class DigitalSignageInfo(
    val name: String,
    val imageBytes: ByteArray,
    val imageFormat: String = "jpeg",
    val price: Double = 0.0,
    val extraInfo: String = "",
)
