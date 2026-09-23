package com.palmah.cafe.amirtham

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform