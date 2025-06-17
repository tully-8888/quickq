package com.tully.quickq

import android.app.Application
import com.tully.quickq.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class QuickQApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@QuickQApplication)
            modules(appModule)
        }
    }
} 