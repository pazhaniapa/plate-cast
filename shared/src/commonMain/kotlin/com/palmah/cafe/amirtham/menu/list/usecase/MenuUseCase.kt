package com.palmah.cafe.amirtham.menu.list.usecase

import com.palmah.cafe.amirtham.common.repository.FirebaseUserRepository
import com.palmah.cafe.amirtham.common.repository.UserRepository
import com.palmah.cafe.amirtham.digitalSignage.repository.DigitalSignageRepository
import com.palmah.cafe.amirtham.digitalSignage.repository.DigitalSignageRepositoryImpl
import com.palmah.cafe.amirtham.menu.list.model.Menu
import com.palmah.cafe.amirtham.menu.list.model.MenuItem
import com.palmah.cafe.amirtham.menu.list.repository.FirebaseMenuRepository
import com.palmah.cafe.amirtham.menu.list.repository.MenuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class MenuUseCase(
    private val repository: MenuRepository = FirebaseMenuRepository(),
    private val userRepository: UserRepository = FirebaseUserRepository(),
    private val digitalSignageRepository: DigitalSignageRepository = DigitalSignageRepositoryImpl(),
) {
    suspend fun extractMenuItems(imageBytes: ByteArray, imageFormat: String): List<MenuItem> {
        val response = repository.extractMenuItems(imageBytes, imageFormat)
        val menu = json.decodeFromString<Menu>(response.text)
        return menu.toMenuItems()
    }

    /** Saves [items] under the signed-in user's brand/outlet. */
    suspend fun saveMenuItems(items: List<MenuItem>) {
        withContext(Dispatchers.Default){
            val userInfo = userRepository.getCurrentUserInfo() ?: error("No signed-in user found")
            repository.saveMenuItems(brand = userInfo.brand, outlet = userInfo.outlet, items = items)
        }
    }

    /** Reads items saved under the signed-in user's brand/outlet. */
    suspend fun getMenuItems(): List<MenuItem> {
        val userInfo = userRepository.getCurrentUserInfo() ?: error("No signed-in user found")
        return repository.getMenuItems(brand = userInfo.brand, outlet = userInfo.outlet)
            .map { snapshot -> snapshot.data<MenuItem>().copy(id = snapshot.id) }
    }

    /** Updates [item] (identified by [MenuItem.id]) under the signed-in user's brand/outlet. */
    suspend fun updateMenuItem(item: MenuItem) {
        withContext(Dispatchers.Default) {
            val userInfo = userRepository.getCurrentUserInfo() ?: error("No signed-in user found")
            repository.updateMenuItem(brand = userInfo.brand, outlet = userInfo.outlet, item = item)
        }
    }

    /**
     * Uploads [imageBytes] to Firebase Storage (via [DigitalSignageRepository.uploadImage]) under
     * the signed-in user's brand/outlet, then persists its download URL as `menuImageUrl` under
     * `/brand/{brandId}/outlet/{outletId}/menuMetaData`.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun saveMenuImage(imageBytes: ByteArray, mimeType: String) {
        withContext(Dispatchers.Default) {
            val userInfo = userRepository.getCurrentUserInfo() ?: error("No signed-in user found")
            val path = "${userInfo.brand}/${userInfo.outlet}/menu/${Uuid.random()}"
            digitalSignageRepository.uploadImage(path, imageBytes, mimeType)
            val downloadUrl = digitalSignageRepository.getDownloadUrl(path)
            repository.saveMenuImageUrl(brand = userInfo.brand, outlet = userInfo.outlet, url = downloadUrl)
        }
    }

    /** The reference menu image URL saved under the signed-in user's brand/outlet, or null if none exists yet. */
    suspend fun getMenuImageUrl(): String? {
        val userInfo = userRepository.getCurrentUserInfo() ?: error("No signed-in user found")
        return repository.getMenuImageUrl(brand = userInfo.brand, outlet = userInfo.outlet)
    }

    private fun Menu.toMenuItems(): List<MenuItem> =
        categories.flatMap { category ->
            category.items.map { item ->
                MenuItem(
                    categoryName = category.categoryName,
                    categoryTimings = category.timings,
                    name = item.name,
                    price = item.price,
                    timings = item.timings,
                    description = item.description,
                )
            }
        }
}
