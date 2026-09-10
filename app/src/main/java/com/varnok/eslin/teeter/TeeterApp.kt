package com.varnok.eslin.teeter

import android.app.Application
import com.varnok.eslin.teeter.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TeeterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TeeterApp)
            modules(appModule)
        }
    }
}
