package com.palmah.cafe.amirtham.menu.list.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MenuItem(
    @SerialName("category_name")
    val categoryName: String = "",
    @SerialName("category_timings")
    val categoryTimings: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val timings: String = "",
    val description: String = "",
    /** Firestore document id; populated when read, never persisted as a field. */
    @Transient
    val id: String = "",
)