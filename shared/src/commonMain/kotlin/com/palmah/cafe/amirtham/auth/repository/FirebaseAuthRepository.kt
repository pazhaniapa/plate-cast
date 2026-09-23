package com.palmah.cafe.amirtham.auth.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthResult
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow

class FirebaseAuthRepository : AuthRepository {

    private val auth = Firebase.auth

    override val authState: Flow<FirebaseUser?> = auth.authStateChanged

    override suspend fun signInWithEmail(email: String, password: String): AuthResult =
        auth.signInWithEmailAndPassword(email, password)

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult =
        auth.createUserWithEmailAndPassword(email, password)

    override suspend fun signInWithGoogleIdToken(idToken: String): AuthResult =
        auth.signInWithCredential(GoogleAuthProvider.credential(idToken, null))

    override suspend fun signOut() = auth.signOut()
}
