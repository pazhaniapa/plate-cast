package com.palmah.cafe.amirtham.platecase_ai

/**
 * Result of a [PlateCastAiClient] call.
 *
 * Gemini can return generated/edited images as attachment parts rather than text, so a plain
 * `String` return type can't carry that back — this exposes both.
 *
 * @property text Text content of the model's response. Empty if it produced only image(s).
 * @property images Any images the model generated/returned, each with its reported MIME type.
 * Empty if it produced only text.
 */
data class PlateCastAiResponse(
    val text: String,
    val images: List<GeneratedImage>,
)
