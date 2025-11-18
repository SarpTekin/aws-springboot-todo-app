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
import com.example.todoapp.ui.screens.RegisterViewModel
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
     * Retrofit Instance for User Service (Port 8081)
     *
     * Creates the Retrofit client for User Service
     *
     * NOTE: Currently configured for PHYSICAL DEVICE on local network
     * - Physical device: "http://192.168.0.129:8081/" (your computer's IP)
     * - Android Emulator: "http://10.0.2.2:8081/"
     *
     * MICROSERVICES ARCHITECTURE:
     * User Service and Task Service run on separate ports
     */
    single(qualifier = org.koin.core.qualifier.named("userRetrofit")) {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.129:8081/")  // Physical Device - User Service
            // For emulator, change to: "http://10.0.2.2:8081/"
            .client(get<OkHttpClient>())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    /**
     * Retrofit Instance for Task Service (Port 8082)
     *
     * ⚠️ IMPORTANT:
     * Task Service runs on a DIFFERENT PORT (8082)
     * This is a microservices architecture - each service has its own port
     *
     * NOTE: Currently configured for PHYSICAL DEVICE on local network
     * - Physical device: "http://192.168.0.129:8082/" (your computer's IP)
     * - Android Emulator: "http://10.0.2.2:8082/"
     */
    single(qualifier = org.koin.core.qualifier.named("taskRetrofit")) {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.129:8082/")  // Physical Device - Task Service
            // For emulator, change to: "http://10.0.2.2:8082/"
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
     * Uses userRetrofit instance (port 8081)
     */
    single {
        get<Retrofit>(qualifier = org.koin.core.qualifier.named("userRetrofit"))
            .create(UserApiService::class.java)
    }

    /**
     * Task API Service
     *
     * Retrofit creates the implementation of TaskApiService interface
     * Uses taskRetrofit instance (port 8082)
     *
     * ⚠️ KEY DIFFERENCE:
     * Uses separate Retrofit instance pointing to port 8082
     * Both services share the same:
     * - OkHttpClient (with AuthInterceptor)
     * - JSON configuration
     * - Timeouts
     *
     * But have different base URLs (8081 vs 8082)
     */
    single {
        get<Retrofit>(qualifier = org.koin.core.qualifier.named("taskRetrofit"))
            .create(TaskApiService::class.java)
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
     * Register ViewModel
     *
     * Manages registration screen state and form validation
     *
     * FEATURES:
     * - Real-time username/email availability checking
     * - Form validation with inline errors
     * - Debounced API calls (500ms delay)
     *
     * viewModel = Creates new instance per screen
     * Form state is reset when screen is destroyed
     */
    viewModel {
        RegisterViewModel(
            repository = get()       // UserRepository
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
