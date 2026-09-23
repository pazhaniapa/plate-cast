package com.palmah.cafe.amirtham.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
actual fun rememberGoogleSignInHandler(): GoogleSignInHandler {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember { AndroidGoogleSignInHandler(context, scope) }
}

private class AndroidGoogleSignInHandler(
    private val context: Context,
    private val scope: CoroutineScope,
) : GoogleSignInHandler {

    override fun requestIdToken(onResult: (idToken: String?, errorMessage: String?) -> Unit) {
        scope.launch {
            // The google-services plugin only generates this string resource once the Google
            // sign-in provider has been enabled in the Firebase console and google-services.json
            // has been re-downloaded. It's looked up by name here because it's generated into
            // the androidApp module's R class, not this (shared) module's.
            val webClientId = context.resources
                .getIdentifier("default_web_client_id", "string", context.packageName)
                .takeIf { it != 0 }
                ?.let { context.getString(it) }

            if (webClientId == null) {
                onResult(
                    null,
                    "Google sign-in isn't configured: enable it in the Firebase console and re-download google-services.json",
                )
                return@launch
            }

            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val response = CredentialManager.create(context).getCredential(context, request)
                val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
                onResult(credential.idToken, null)
            } catch (e: GetCredentialCancellationException) {
                onResult(null, null)
            } catch (e: GetCredentialException) {
                onResult(null, e.message ?: "Google sign-in failed")
            }
        }
    }
}
