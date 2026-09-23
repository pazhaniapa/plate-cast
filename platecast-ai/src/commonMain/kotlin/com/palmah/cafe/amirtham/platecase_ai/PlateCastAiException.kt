package com.palmah.cafe.amirtham.platecase_ai

/**
 * Thrown by [PlateCastAiClient] when a Gemini API call fails.
 *
 * Wraps whatever Koog threw internally so callers only need to depend on this type, not on
 * Koog's own exception hierarchy.
 *
 * @property statusCode HTTP status code of the failed request, if the failure happened at the
 * HTTP layer (e.g. 429 for rate limiting/quota, 5xx for server errors). Null if the failure
 * happened before an HTTP response was received (e.g. no network connection).
 * @property errorBody Raw error response body from the API, if available.
 */
class PlateCastAiException(
    message: String?,
    val statusCode: Int?,
    val errorBody: String?,
    cause: Throwable?,
) : Exception(message, cause) {
    /** True when this failure is a rate-limit/quota error (HTTP 429). */
    val isRateLimited: Boolean get() = statusCode == 429
}
