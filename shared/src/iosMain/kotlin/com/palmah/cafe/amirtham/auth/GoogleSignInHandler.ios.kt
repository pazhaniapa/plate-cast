package com.palmah.cafe.amirtham.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private var injectedHandler: GoogleSignInHandler? = null

/** Called once from [com.palmah.cafe.amirtham.MainViewController] with the Swift-provided implementation. */
fun setGoogleSignInHandler(handler: GoogleSignInHandler) {
    injectedHandler = handler
}

private object UnconfiguredGoogleSignInHandler : GoogleSignInHandler {
    override fun requestIdToken(onResult: (idToken: String?, errorMessage: String?) -> Unit) {
        onResult(null, "Google sign-in is not wired up on iOS yet")
    }
}

@Composable
actual fun rememberGoogleSignInHandler(): GoogleSignInHandler =
    remember { injectedHandler ?: UnconfiguredGoogleSignInHandler }
