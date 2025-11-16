package com.microtodo.android

import android.app.Application
import com.microtodo.android.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class
 * Initializes Koin dependency injection
 */
class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Koin Android logger
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)

            // Android context
            androidContext(this@TodoApp)

            // Load modules
            modules(appModules)
        }
    }
}
