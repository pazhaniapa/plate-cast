package com.palmah.cafe.amirtham.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.contentType

/** Downloads the bytes at [url], returning them alongside the image format inferred from the response's content type (e.g. "jpeg", "png"), defaulting to "jpeg" if absent. */
suspend fun downloadImageBytes(url: String): Pair<ByteArray, String> {
    val client = HttpClient()
    try {
        val response = client.get(url)
        val bytes: ByteArray = response.body()
        val format = response.contentType()?.contentSubtype ?: "jpeg"
        return bytes to format
    } finally {
        client.close()
    }
}
