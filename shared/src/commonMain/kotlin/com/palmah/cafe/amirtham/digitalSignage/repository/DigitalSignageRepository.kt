package com.palmah.cafe.amirtham.digitalSignage.repository

import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageBoard
import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageInfo
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiResponse

interface DigitalSignageRepository {
    suspend fun generateDigitalSignageImage(promptText: String, images: List<DigitalSignageInfo>): PlateCastAiResponse

    /**
     * Uploads [imageBytes] to Firebase Storage at [path] with the given [mimeType] as its
     * content type.
     */
    suspend fun uploadImage(path: String, imageBytes: ByteArray, mimeType: String)

    /** Returns the public, token-bearing download URL for the object at [path]. */
    suspend fun getDownloadUrl(path: String): String

    /**
     * Saves [url] as a new document under `/brand/{brandId}/outlet/{outletId}/digitalsignage`,
     * resolving the brand/outlet document ids by [brand]/[outlet] name (creating them if they
     * don't exist yet).
     */
    suspend fun saveDigitalSignageUrl(brand: String, outlet: String, url: String, name: String)

    /**
     * Digital signage boards saved under `/brand/{brandId}/outlet/{outletId}/digitalsignage`,
     * resolving the brand/outlet document ids by [brand]/[outlet] name. Empty if [brand]/[outlet]
     * don't exist or none have been saved yet.
     */
    suspend fun getDigitalSignageBoards(brand: String, outlet: String): List<DigitalSignageBoard>

    /** Deletes the board document identified by [boardId] under the resolved [brand]/[outlet]. */
    suspend fun deleteDigitalSignageBoard(brand: String, outlet: String, boardId: String)
}
