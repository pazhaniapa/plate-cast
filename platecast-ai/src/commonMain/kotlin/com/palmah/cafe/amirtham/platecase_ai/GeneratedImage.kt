package com.palmah.cafe.amirtham.platecase_ai

/**
 * An image Gemini generated/returned as part of a [PlateCastAiResponse].
 *
 * @property bytes Raw image bytes.
 * @property mimeType MIME type Gemini reported for the image (e.g. "image/png"), so callers
 * don't have to guess a content type when persisting it elsewhere.
 */
data class GeneratedImage(
    val bytes: ByteArray,
    val mimeType: String,
)
