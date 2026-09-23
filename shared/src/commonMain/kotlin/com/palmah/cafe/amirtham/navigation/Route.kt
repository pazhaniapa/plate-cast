package com.palmah.cafe.amirtham.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object SignIn : Route

    @Serializable
    data object CreateAccount : Route

    @Serializable
    data object Home : Route
}
