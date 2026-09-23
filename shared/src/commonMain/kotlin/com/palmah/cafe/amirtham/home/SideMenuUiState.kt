package com.palmah.cafe.amirtham.home

import com.palmah.cafe.amirtham.auth.usecase.AuthUserInfo

data class SideMenuUiState(
    val userInfo: AuthUserInfo? = null,
)
