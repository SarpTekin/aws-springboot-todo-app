package com.microtodo.android.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.microtodo.android.BuildConfig
import com.microtodo.android.data.local.TokenManager
import com.microtodo.android.data.remote.api.TaskApiService
import com.microtodo.android.data.remote.api.UserApiService
import com.microtodo.android.data.remote.interceptor.AuthInterceptor
import com.microtodo.android.data.repository.AuthRepositoryImpl
import com.microtodo.android.data.repository.TaskRepositoryImpl
import com.microtodo.android.domain.repository.AuthRepository
import com.microtodo.android.domain.repository.TaskRepository
import com.microtodo.android.domain.usecase.*
import com.microtodo.android.presentation.login.LoginViewModel
import com.microtodo.android.presentation.register.RegisterViewModel
import com.microtodo.android.presentation.tasks.TasksViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Koin Dependency Injection Modules
 * Organized by layers: Network, Data, Domain, Presentation
 */

val networkModule = module {
    // JSON Configuration
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    // Logging Interceptor
    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // Auth Interceptor
    single { AuthInterceptor(get()) }

    // OkHttp Client
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .addInterceptor(get<AuthInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // User Service Retrofit
    single<Retrofit>(qualifier = org.koin.core.qualifier.named("userService")) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.USER_SERVICE_BASE_URL)
            .client(get())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    // Task Service Retrofit
    single<Retrofit>(qualifier = org.koin.core.qualifier.named("taskService")) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.TASK_SERVICE_BASE_URL)
            .client(get())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    // API Services
    single {
        get<Retrofit>(qualifier = org.koin.core.qualifier.named("userService"))
            .create(UserApiService::class.java)
    }

    single {
        get<Retrofit>(qualifier = org.koin.core.qualifier.named("taskService"))
            .create(TaskApiService::class.java)
    }
}

val dataModule = module {
    // Token Manager
    single { TokenManager(androidContext()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<TaskRepository> { TaskRepositoryImpl(get()) }
}

val domainModule = module {
    // Use Cases
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { GetTasksUseCase(get(), get()) }
    factory { CreateTaskUseCase(get(), get()) }
    factory { UpdateTaskUseCase(get(), get()) }
    factory { DeleteTaskUseCase(get()) }
}

val presentationModule = module {
    // ViewModels
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { TasksViewModel(get(), get(), get(), get(), get()) }
}

// Combine all modules
val appModules = listOf(
    networkModule,
    dataModule,
    domainModule,
    presentationModule
)
