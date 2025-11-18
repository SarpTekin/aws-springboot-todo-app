package com.example.todoapp.di

import com.example.todoapp.data.local.TokenManager
import com.example.todoapp.data.network.AuthInterceptor
import com.example.todoapp.data.remote.TaskApiService
import com.example.todoapp.data.remote.UserApiService
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.data.repository.UserRepository
import com.example.todoapp.ui.screens.CreateTaskViewModel
import com.example.todoapp.ui.screens.HomeViewModel
import com.example.todoapp.ui.screens.LoginViewModel
import com.example.todoapp.ui.screens.TaskListViewModel
import com.example.todoapp.ui.screens.UsernameCheckViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Koin Dependency Injection Module
 *
 * WHAT IS DEPENDENCY INJECTION?
 * Instead of creating objects yourself:
 *   val repo = UserRepository(UserApiService(...))
 *
 * You "ask" for them and Koin provides them:
 *   class MyViewModel(private val repo: UserRepository)
 *
 * BENEFITS:
 * - Easy to swap implementations (testing, different environments)
 * - Single source of truth for object creation
 * - No need to pass dependencies manually through constructors
 * - Automatic lifecycle management
 *
 * This module defines HOW to create each dependency.
 */
val appModule = module {

    /**
     * JSON Configuration
     *
     * single = Create ONE instance and reuse it (singleton)
     */
    single {
        Json {
            ignoreUnknownKeys = true  // Ignore extra fields in JSON
            isLenient = true            // Allow lenient parsing
            prettyPrint = false         // Compact JSON (smaller size)
        }
    }

    /**
     * HTTP Logging Interceptor
     *
     * Logs all HTTP requests/responses for debugging
     * Only enabled in DEBUG builds (remove in production)
     */
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Auth Interceptor
     *
     * Automatically adds JWT token to all requests
     * Handles 401 responses (auto-logout)
     */
    single {
        AuthInterceptor(tokenManager = get())
    }

    /**
     * OkHttpClient
     *
     * Handles HTTP connections, timeouts, interceptors
     *
     * Interceptor order matters:
     * 1. AuthInterceptor - adds token FIRST
     * 2. HttpLoggingInterceptor - logs the request WITH token
     */
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())        // Add token first
            .addInterceptor(get<HttpLoggingInterceptor>()) // Then log
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Retrofit Instance for User Service
     *
     * Creates the Retrofit client for http://localhost:8081
     *
     * NOTE: Currently configured for PHYSICAL DEVICE on local network
     * - Physical device: "http://192.168.0.129:8081/" (your computer's IP)
     * - Android Emulator: "http://10.0.2.2:8081/"
     */
    single {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.129:8081/")  // Physical Device
            // For emulator, change to: "http://10.0.2.2:8081/"
            .client(get<OkHttpClient>())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    /**
     * User API Service
     *
     * Retrofit creates the implementation of UserApiService interface
     */
    single {
        get<Retrofit>().create(UserApiService::class.java)
    }

    /**
     * Task API Service
     *
     * Retrofit creates the implementation of TaskApiService interface
     * Uses the same Retrofit instance (same base URL, same interceptors)
     *
     * SHARING RETROFIT:
     * Both UserApiService and TaskApiService use the same Retrofit
     * This means:
     * - Same base URL (http://192.168.0.129:8081/)
     * - Same AuthInterceptor (JWT automatically added)
     * - Same timeouts and configuration
     */
    single {
        get<Retrofit>().create(TaskApiService::class.java)
    }

    /**
     * User Repository
     *
     * Creates UserRepository with UserApiService injected
     *
     * single = One instance shared across the app
     */
    single {
        UserRepository(apiService = get())
    }

    /**
     * Task Repository
     *
     * Creates TaskRepository with TaskApiService injected
     *
     * single = One instance shared across the app
     * Handles all task-related data operations (CRUD)
     */
    single {
        TaskRepository(apiService = get())
    }

    /**
     * Token Manager
     *
     * Manages secure storage of JWT token and user data
     *
     * single = One instance for the whole app
     * get() = Koin provides Android Context
     */
    single {
        TokenManager(context = get())
    }

    /**
     * Username Check ViewModel
     *
     * viewModel = Special Koin function for ViewModels
     * - Creates new instance for each screen
     * - Automatically cleared when screen is destroyed
     *
     * get() = Ask Koin to provide UserRepository
     */
    viewModel {
        UsernameCheckViewModel(repository = get())
    }

    /**
     * Login ViewModel
     *
     * Manages login screen state and logic
     *
     * viewModel = Creates new instance per screen
     * get() = Koin provides dependencies automatically
     */
    viewModel {
        LoginViewModel(
            repository = get(),      // UserRepository
            tokenManager = get()     // TokenManager
        )
    }

    /**
     * Home ViewModel
     *
     * Manages home screen and tests JWT interceptor
     */
    viewModel {
        HomeViewModel(
            repository = get(),      // UserRepository
            tokenManager = get()     // TokenManager
        )
    }

    /**
     * Task List ViewModel
     *
     * Manages task list screen state and operations
     *
     * DEPENDENCIES:
     * - TaskRepository: For CRUD operations on tasks
     * - TokenManager: To get current user ID
     *
     * viewModel = Creates new instance per screen
     * Automatically cleared when screen is destroyed
     */
    viewModel {
        TaskListViewModel(
            repository = get(),      // TaskRepository
            tokenManager = get()     // TokenManager
        )
    }

    /**
     * Create Task ViewModel
     *
     * Manages create task form state and validation
     *
     * DEPENDENCIES:
     * - TaskRepository: To create new task
     * - TokenManager: To get current user ID
     *
     * viewModel = New instance for each create task session
     * Form state is reset when screen is destroyed
     */
    viewModel {
        CreateTaskViewModel(
            repository = get(),      // TaskRepository
            tokenManager = get()     // TokenManager
        )
    }
}
