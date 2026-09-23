package com.palmah.cafe.amirtham.menu.list.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shape of the JSON [PlateCastAiClient][com.palmah.cafe.amirtham.platecase_ai.PlateCastAiClient]
 * returns for [MENU_EXTRACTION_PROMPT][com.palmah.cafe.amirtham.utils.MENU_EXTRACTION_PROMPT],
 * and what gets written to Firestore.
 */
@Serializable
data class Menu(
    val categories: List<MenuCategory> = emptyList(),
)

@Serializable
data class MenuCategory(
    @SerialName("category_name")
    val categoryName: String = "",
    val timings: String = "",
    val items: List<RestaurantMenuItem> = emptyList(),
)

@Serializable
data class RestaurantMenuItem(
    val name: String = "",
    val price: Double = 0.0,
    val timings: String = "",
    val description: String = "",
)
