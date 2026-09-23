package com.palmah.cafe.amirtham.menu.list.repository

import com.palmah.cafe.amirtham.menu.list.model.MenuItem
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiClient
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiConfig
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiResponse
import com.palmah.cafe.amirtham.utils.MENU_EXTRACTION_PROMPT
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable

private const val BRAND_COLLECTION = "brand"
private const val OUTLET_COLLECTION = "outlet"
private const val ITEMS_COLLECTION = "items"

@Serializable
private data class NamedDocument(val name: String = "")

class FirebaseMenuRepository(
    apiKey: String = PlateCastAiConfig.GEMINI_API_KEY,
) : MenuRepository {

    private val plateCastAiClient = PlateCastAiClient(apiKey = apiKey)
    private val firestore = Firebase.firestore

    override suspend fun extractMenuItems(imageBytes: ByteArray, imageFormat: String): PlateCastAiResponse =
        plateCastAiClient.extractMenuItems(
            promptText = MENU_EXTRACTION_PROMPT,
            imageBytes = imageBytes,
            imageFormat = imageFormat,
        )

    override suspend fun saveMenuItems(brand: String, outlet: String, items: List<MenuItem>) {
        val brandRef = firestore.collection(BRAND_COLLECTION)
            .where { "name" equalTo brand }
            .get()
            .documents
            .firstOrNull()
            ?.reference
            ?: firestore.collection(BRAND_COLLECTION).add(NamedDocument(name = brand))

        val outletRef = brandRef.collection(OUTLET_COLLECTION)
            .where { "name" equalTo outlet }
            .get()
            .documents
            .firstOrNull()
            ?.reference
            ?: brandRef.collection(OUTLET_COLLECTION).add(NamedDocument(name = outlet))

        // Firestore has no explicit "create collection" step: a subcollection simply comes into
        // existence the moment the first document is written to it.
        val itemsCollection = outletRef.collection(ITEMS_COLLECTION)
        coroutineScope {
            items.map { item -> async { itemsCollection.add(item) } }.awaitAll()
        }
    }

    override suspend fun getMenuItems(brand: String, outlet: String): List<DocumentSnapshot> {
        val brandRef = firestore.collection(BRAND_COLLECTION)
            .where { "name" equalTo brand }
            .get()
            .documents
            .firstOrNull()
            ?.reference
            ?: return emptyList()

        val outletRef = brandRef.collection(OUTLET_COLLECTION)
            .where { "name" equalTo outlet }
            .get()
            .documents
            .firstOrNull()
            ?.reference
            ?: return emptyList()

        return outletRef.collection(ITEMS_COLLECTION)
            .get()
            .documents
    }
}
