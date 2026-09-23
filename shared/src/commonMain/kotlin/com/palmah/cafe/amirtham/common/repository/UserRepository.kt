package com.palmah.cafe.amirtham.common.repository

import com.palmah.cafe.amirtham.auth.usecase.AuthUserInfo

interface UserRepository {
    suspend fun updateUserInfo(userInfo: AuthUserInfo): Boolean
    suspend fun getUserInfo(email: String): AuthUserInfo?

    /** [AuthUserInfo] for the currently signed-in user, or null if no one is signed in. */
    suspend fun getCurrentUserInfo(): AuthUserInfo?
}
