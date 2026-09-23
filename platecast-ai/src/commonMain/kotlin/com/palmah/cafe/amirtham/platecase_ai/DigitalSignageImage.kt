package com.palmah.cafe.amirtham.platecase_ai

/**
 * One image plus its metadata to send to Gemini as part of a multi-image prompt (e.g. digital
 * signage generation), so the model can be told what each image actually depicts.
 *
 * @param name What this image depicts (e.g. a dish name), sent to the model as a text label
 * immediately preceding the image.
 * @param imageBytes Raw bytes of the image (e.g. JPEG/PNG).
 * @param imageFormat Image format/extension, e.g. "jpeg" or "png".
 */
data class DigitalSignageImage(
    val name: String,
    val imageBytes: ByteArray,
    val imageFormat: String = "jpeg",
)
