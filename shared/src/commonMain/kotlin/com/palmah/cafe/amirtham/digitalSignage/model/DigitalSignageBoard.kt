package com.palmah.cafe.amirtham.digitalSignage.model

/**
 * A saved digital signage board, read back from Firestore.
 *
 * @property createdAtMillis Epoch millis the board was created at, or 0 if unknown (e.g. a board
 * saved before this field existed).
 */
data class DigitalSignageBoard(
    val id: String,
    val name: String,
    val imageUrl: String,
    val createdAtMillis: Long,
)
