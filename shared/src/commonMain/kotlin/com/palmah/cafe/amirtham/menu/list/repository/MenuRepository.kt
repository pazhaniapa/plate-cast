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
}
