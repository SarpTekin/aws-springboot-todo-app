package com.example.todoapp

import android.app.Application
import com.example.todoapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application Class
 *
 * This class is created ONCE when the app starts.
 * Perfect place to initialize libraries like Koin.
 *
 * IMPORTANT: Must be declared in AndroidManifest.xml:
 * <application android:name=".TodoApp" ...>
 */
class TodoApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin dependency injection
        startKoin {
            // Enable logging (helps debug DI issues)
            androidLogger(Level.ERROR)

            // Provide Android context to Koin
            androidContext(this@TodoApp)

            // Load our modules (dependencies)
            modules(appModule)
        }
    }
}
