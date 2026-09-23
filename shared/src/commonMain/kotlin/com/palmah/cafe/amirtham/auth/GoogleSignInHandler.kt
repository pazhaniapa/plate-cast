package com.palmah.cafe.amirtham.auth

import androidx.compose.runtime.Composable

/**
 * Bridges to the platform-native Google credential flow.
 *
 * Android implements this purely in Kotlin (Credential Manager). iOS has no
 * Kotlin-callable Google Sign-In SDK (it's Swift-only), so the iOS actual
 * receives its implementation injected from Swift at [com.palmah.cafe.amirtham.MainViewController].
 */
interface GoogleSignInHandler {
    fun requestIdToken(onResult: (idToken: String?, errorMessage: String?) -> Unit)
}

@Composable
expect fun rememberGoogleSignInHandler(): GoogleSignInHandler
