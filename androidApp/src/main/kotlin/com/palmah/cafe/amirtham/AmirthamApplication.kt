package com.palmah.cafe.amirtham

import android.app.Application
import com.palmah.cafe.amirtham.di.initKoin
import org.koin.android.ext.koin.androidContext

class AmirthamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@AmirthamApplication) }
    }
}