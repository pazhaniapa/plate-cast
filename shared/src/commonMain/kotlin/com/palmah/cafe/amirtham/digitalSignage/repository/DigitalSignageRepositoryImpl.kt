package com.palmah.cafe.amirtham.digitalSignage.repository

import co.touchlab.kermit.Logger
import com.palmah.cafe.amirtham.common.storage.toStorageData
import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageBoard
import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageInfo
import com.palmah.cafe.amirtham.platecase_ai.DigitalSignageImage
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiClient
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiConfig
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiResponse
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.toMilliseconds
import dev.gitlive.firebase.storage.storage
import dev.gitlive.firebase.storage.storageMetadata
import kotlinx.serialization.Serializable

private const val BRAND_COLLECTION = "brand"
private const val OUTLET_COLLECTION = "outlet"
private const val DIGITAL_SIGNAGE_COLLECTION = "digitalsignage"

@Serializable
private data class NamedDocument(val name: String = "")

@Serializable
private data class DigitalSignageDocument(
    val name: String = "",
    val url: String = "",
    val createdAt: Timestamp? = null,
)

class DigitalSignageRepositoryImpl : DigitalSignageRepository {

    private val plateCastAiClient = PlateCastAiClient(apiKey = PlateCastAiConfig.GEMINI_API_KEY)
    private val storage = Firebase.storage
    private val firestore = Firebase.firestore

    override suspend fun generateDigitalSignageImage(
        promptText: String,
        images: List<DigitalSignageInfo>,
    ): PlateCastAiResponse {
        val signageImages = images.map { info ->
            DigitalSignageImage(
                name = info.name,
                imageBytes = info.imageBytes,
                imageFormat = info.imageFormat,
            )
        }
        val response = plateCastAiClient.generateDigitalSignageImage(promptText, signageImages)
        Logger.d(tag = "DigitalSignageRepositoryImpl", messageString = "Generated Image: ${response.images.size}")
        return response
    }

    override suspend fun uploadImage(path: String, imageBytes: ByteArray, mimeType: String) {
        val metadata = storageMetadata { contentType = mimeType }
        storage.reference(path).putData(imageBytes.toStorageData(), metadata)
    }

    override suspend fun getDownloadUrl(path: String): String {
        return storage.reference(path).getDownloadUrl()
    }

    override suspend fun saveDigitalSignageUrl(brand: String, outlet: String, url: String, name: String) {
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
        outletRef.collection(DIGITAL_SIGNAGE_COLLECTION).add(
            DigitalSignageDocument(name = name, url = url, createdAt = Timestamp.now()),
        )
    }

    override suspend fun getDigitalSignageBoards(brand: String, outlet: String): List<DigitalSignageBoard> {
        val outletRef = resolveOutletRef(brand, outlet) ?: return emptyList()

        return outletRef.collection(DIGITAL_SIGNAGE_COLLECTION)
            .get()
            .documents
            .map { snapshot ->
                val doc = snapshot.data<DigitalSignageDocument>()
                DigitalSignageBoard(
                    id = snapshot.id,
                    name = doc.name,
                    imageUrl = doc.url,
                    createdAtMillis = doc.createdAt?.toMilliseconds()?.toLong() ?: 0L,
                )
            }
    }

    override suspend fun deleteDigitalSignageBoard(brand: String, outlet: String, boardId: String) {
        val outletRef = resolveOutletRef(brand, outlet) ?: return
        outletRef.collection(DIGITAL_SIGNAGE_COLLECTION).document(boardId).delete()
    }

    /** Resolves the outlet's [DocumentReference] by [brand]/[outlet] name, or null if either doesn't exist. */
    private suspend fun resolveOutletRef(brand: String, outlet: String): DocumentReference? {
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
}
