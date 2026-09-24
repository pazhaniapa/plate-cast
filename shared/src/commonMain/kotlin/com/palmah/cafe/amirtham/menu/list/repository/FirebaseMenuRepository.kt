package com.palmah.cafe.amirtham.menu.list.repository

import com.palmah.cafe.amirtham.menu.list.model.MenuItem
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiClient
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiConfig
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiResponse
import com.palmah.cafe.amirtham.utils.MENU_EXTRACTION_PROMPT
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable

private const val BRAND_COLLECTION = "brand"
private const val OUTLET_COLLECTION = "outlet"
private const val ITEMS_COLLECTION = "items"
private const val MENU_METADATA_COLLECTION = "menuMetaData"

@Serializable
private data class NamedDocument(val name: String = "")

@Serializable
private data class MenuMetadataDocument(val menuImageUrl: String = "")

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
        val outletRef = resolveOrCreateOutletRef(brand, outlet)

        // Firestore has no explicit "create collection" step: a subcollection simply comes into
        // existence the moment the first document is written to it.
        val itemsCollection = outletRef.collection(ITEMS_COLLECTION)
        coroutineScope {
            items.map { item -> async { itemsCollection.add(item) } }.awaitAll()
        }
    }

    override suspend fun saveMenuImageUrl(brand: String, outlet: String, url: String) {
        val outletRef = resolveOrCreateOutletRef(brand, outlet)

        val metadataCollection = outletRef.collection(MENU_METADATA_COLLECTION)
        val existingDocRef = metadataCollection.get().documents.firstOrNull()?.reference

        if (existingDocRef != null) {
            existingDocRef.set(MenuMetadataDocument(menuImageUrl = url), merge = true)
        } else {
            metadataCollection.add(MenuMetadataDocument(menuImageUrl = url))
        }
    }

    override suspend fun getMenuImageUrl(brand: String, outlet: String): String? {
        val outletRef = findOutletRef(brand, outlet) ?: return null
        val doc = outletRef.collection(MENU_METADATA_COLLECTION).get().documents.firstOrNull() ?: return null
        return doc.data<MenuMetadataDocument>().menuImageUrl.takeIf { it.isNotBlank() }
    }

    override suspend fun getMenuItems(brand: String, outlet: String): List<DocumentSnapshot> {
        val outletRef = findOutletRef(brand, outlet) ?: return emptyList()
        return outletRef.collection(ITEMS_COLLECTION)
            .get()
            .documents
    }

    override suspend fun updateMenuItem(brand: String, outlet: String, item: MenuItem) {
        val outletRef = findOutletRef(brand, outlet)
            ?: error("No outlet '$outlet' found for brand '$brand'")
        outletRef.collection(ITEMS_COLLECTION).document(item.id).set(item, merge = true)
    }

    /** Looks up the outlet document for [brand]/[outlet] by name, or null if either doesn't exist yet. */
    private suspend fun findOutletRef(brand: String, outlet: String): DocumentReference? {
        val brandRef = firestore.collection(BRAND_COLLECTION)
            .where { "name" equalTo brand }
            .get()
            .documents
            .firstOrNull()
            ?.reference
            ?: return null

        return brandRef.collection(OUTLET_COLLECTION)
            .where { "name" equalTo outlet }
            .get()
            .documents
            .firstOrNull()
            ?.reference
    }

    /** Looks up the outlet document for [brand]/[outlet] by name, creating both (and the brand/outlet documents) if missing. */
    private suspend fun resolveOrCreateOutletRef(brand: String, outlet: String): DocumentReference {
        val brandRef = firestore.collection(BRAND_COLLECTION)
            .where { "name" equalTo brand }
            .get()
            .documents
            .firstOrNull()
            ?.reference
            ?: firestore.collection(BRAND_COLLECTION).add(NamedDocument(name = brand))

        return brandRef.collection(OUTLET_COLLECTION)
            .where { "name" equalTo outlet }
            .get()
            .documents
            .firstOrNull()
            ?.reference
            ?: brandRef.collection(OUTLET_COLLECTION).add(NamedDocument(name = outlet))
    }
}
