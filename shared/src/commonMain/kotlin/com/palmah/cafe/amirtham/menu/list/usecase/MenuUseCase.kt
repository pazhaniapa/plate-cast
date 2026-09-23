package com.palmah.cafe.amirtham.menu.list.usecase

import com.palmah.cafe.amirtham.common.repository.FirebaseUserRepository
import com.palmah.cafe.amirtham.common.repository.UserRepository
import com.palmah.cafe.amirtham.menu.list.model.Menu
import com.palmah.cafe.amirtham.menu.list.model.MenuItem
import com.palmah.cafe.amirtham.menu.list.repository.FirebaseMenuRepository
import com.palmah.cafe.amirtham.menu.list.repository.MenuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class MenuUseCase(
    private val repository: MenuRepository = FirebaseMenuRepository(),
    private val userRepository: UserRepository = FirebaseUserRepository(),
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
            .map { it.data<MenuItem>() }
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
