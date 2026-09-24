package com.palmah.cafe.amirtham.menu.list.repository

import com.palmah.cafe.amirtham.menu.list.model.MenuItem
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiResponse
import dev.gitlive.firebase.firestore.DocumentSnapshot

interface MenuRepository {
    suspend fun extractMenuItems(imageBytes: ByteArray, imageFormat: String): PlateCastAiResponse

    /** Persists [items] under `/brand/{brandId}/outlet/{outletId}/items`, resolving the brand/outlet document ids by [brand]/[outlet] name. */
    suspend fun saveMenuItems(brand: String, outlet: String, items: List<MenuItem>)

    /** Raw snapshots from `/brand/{brandId}/outlet/{outletId}/items`. Empty if [brand]/[outlet] don't exist. */
    suspend fun getMenuItems(brand: String, outlet: String): List<DocumentSnapshot>

    /** Overwrites the existing `/brand/{brandId}/outlet/{outletId}/items/{item.id}` document with [item]'s fields. */
    suspend fun updateMenuItem(brand: String, outlet: String, item: MenuItem)

    /**
     * Creates/updates the `menuImageUrl` field of the first document under
     * `/brand/{brandId}/outlet/{outletId}/menuMetaData`, resolving the brand/outlet document ids
     * by [brand]/[outlet] name (creating them, the `menuMetaData` subcollection, and its first
     * document if they don't exist yet).
     */
    suspend fun saveMenuImageUrl(brand: String, outlet: String, url: String)

    /**
     * The `menuImageUrl` field of the first document under
     * `/brand/{brandId}/outlet/{outletId}/menuMetaData`, resolving the brand/outlet document ids
     * by [brand]/[outlet] name. Null if [brand]/[outlet] don't exist or no document has been
     * saved yet.
     */
    suspend fun getMenuImageUrl(brand: String, outlet: String): String?
}
