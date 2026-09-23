package com.palmah.cafe.amirtham.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object UnsupportedGoogleSignInHandler : GoogleSignInHandler {
    override fun requestIdToken(onResult: (idToken: String?, errorMessage: String?) -> Unit) {
        onResult(null, "Google sign-in is not implemented on desktop yet")
    }
}

@Composable
actual fun rememberGoogleSignInHandler(): GoogleSignInHandler = remember { UnsupportedGoogleSignInHandler }
