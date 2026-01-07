package com.aaditx23.phtask

import android.app.Application
import com.aaditx23.phtask.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PhTaskApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PhTaskApplication)
            modules(databaseModule)
        }
    }
}