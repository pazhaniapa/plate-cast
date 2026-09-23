package com.palmah.cafe.amirtham.auth.usecase

import com.palmah.cafe.amirtham.auth.GoogleSignInHandler
import com.palmah.cafe.amirtham.auth.repository.AuthRepository
import com.palmah.cafe.amirtham.auth.repository.FirebaseAuthRepository
import com.palmah.cafe.amirtham.common.repository.FirebaseUserRepository
import com.palmah.cafe.amirtham.common.repository.UserRepository
import dev.gitlive.firebase.auth.AuthResult
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Starter shape for what a successful sign-in tells the rest of the app — adjust fields as needed. */
@Serializable
data class AuthUserInfo(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isNewUser: Boolean,
    val brand : String = "",
    val outlet : String = "",
    val role : UserRole = UserRole.USER
)

@Serializable
enum class UserRole {
    USER,
    ROOT,
    ADMIN
}

class AuthUseCase(
    private val repository: AuthRepository = FirebaseAuthRepository(),
    private val userRepository: UserRepository = FirebaseUserRepository(),
) {
    val authState: Flow<FirebaseUser?> get() = repository.authState

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        require(email.isNotBlank() && password.isNotBlank()) { "Enter both email and password" }
        return repository.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        require(email.isNotBlank() && password.isNotBlank()) { "Enter both email and password" }
        return repository.signUpWithEmail(email, password)
    }

    /** Returns null if the user cancelled the native Google credential flow. */
    suspend fun signInWithGoogle(handler: GoogleSignInHandler): AuthResult? {
        val idToken = handler.awaitIdToken() ?: return null
        return repository.signInWithGoogleIdToken(idToken)
    }

    suspend fun signOut() = repository.signOut()

    /** Derives [AuthUserInfo] from a sign-in result and persists it. Returns null if the result carries no user. */
    suspend fun updateUserInfo(
        authResult: AuthResult,
        brand: String = "",
        outlet: String = "",
        displayName: String? = null,
        role: UserRole = UserRole.USER,
    ): AuthUserInfo? {
        val user = authResult.user ?: return null
        val userInfo = AuthUserInfo(
            uid = user.uid,
            email = user.email,
            displayName = displayName?.takeIf { it.isNotBlank() } ?: user.displayName,
            photoUrl = user.photoURL,
            isNewUser = authResult.additionalUserInfo?.isNewUser ?: false,
            brand = brand,
            outlet = outlet,
            role = role,
        )
        userRepository.updateUserInfo(userInfo)
        return userInfo
    }

    private suspend fun GoogleSignInHandler.awaitIdToken(): String? =
        suspendCancellableCoroutine { continuation ->
            requestIdToken { idToken, errorMessage ->
                if (errorMessage != null) {
                    continuation.resumeWithException(Exception(errorMessage))
                } else {
                    continuation.resume(idToken)
                }
            }
        }
}
