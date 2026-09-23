package com.palmah.cafe.amirtham.common.repository

import com.palmah.cafe.amirtham.auth.usecase.AuthUserInfo
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

private const val USERS_COLLECTION = "users"

class FirebaseUserRepository : UserRepository {

    private val firestore = Firebase.firestore

    override suspend fun updateUserInfo(userInfo: AuthUserInfo): Boolean {
        val email = userInfo.email ?: return false
        return try {
            // Keying the document by email doubles as the existence check: `merge = true`
            // creates the document if it's not there yet, or merges into it if it already is.
            firestore.collection(USERS_COLLECTION)
                .document(email)
                .set(userInfo, merge = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserInfo(email: String): AuthUserInfo? {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).document(email).get()
            if (snapshot.exists) snapshot.data<AuthUserInfo>() else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCurrentUserInfo(): AuthUserInfo? {
        val email = Firebase.auth.currentUser?.email ?: return null
        return getUserInfo(email)
    }
}
