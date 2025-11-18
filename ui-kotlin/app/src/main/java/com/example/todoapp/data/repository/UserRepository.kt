package com.example.todoapp.data.repository

import com.example.todoapp.data.model.AvailabilityResponse
import com.example.todoapp.data.model.LoginRequest
import com.example.todoapp.data.model.LoginResponse
import com.example.todoapp.data.model.UserRequest
import com.example.todoapp.data.model.UserResponse
import com.example.todoapp.data.remote.UserApiService

/**
 * User Repository
 *
 * The "single source of truth" for user-related data.
 *
 * WHY DO WE NEED THIS?
 * - Separation of concerns: ViewModel doesn't know about network details
 * - Easy to test: Can create a fake repository for testing
 * - Easy to change: Want to add caching? Just modify the repository
 * - Business logic: Can add validation, transformation, etc.
 *
 * ARCHITECTURE:
 * UI → ViewModel → Repository → API Service → Backend
 *
 * The Repository is the "middle manager" that:
 * 1. Calls the API service
 * 2. Handles errors gracefully
 * 3. Could add caching (future enhancement)
 * 4. Returns data in a format the ViewModel expects
 */
class UserRepository(
    private val apiService: UserApiService
) {
    /**
     * Check if a username is available
     *
     * @param username The username to check
     * @return Result with AvailabilityResponse or error
     */
    suspend fun checkUsername(username: String): Result<AvailabilityResponse> {
        return try {
            // Call the API service (suspend function - runs in background)
            val response = apiService.checkUsername(username)

            // Wrap successful response in Result.success
            Result.success(response)
        } catch (e: Exception) {
            // If anything goes wrong (network error, server error, etc.)
            // Wrap the error in Result.failure
            Result.failure(e)
        }
    }

    /**
     * Check if an email is available
     *
     * @param email The email to check
     * @return Result with AvailabilityResponse or error
     */
    suspend fun checkEmail(email: String): Result<AvailabilityResponse> {
        return try {
            val response = apiService.checkEmail(email)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new user
     *
     * @param request UserRequest with registration details
     * @return Result with UserResponse or error
     */
    suspend fun register(request: UserRequest): Result<UserResponse> {
        return try {
            val response = apiService.register(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login with username and password
     *
     * @param username Username
     * @param password Password
     * @return Result with LoginResponse (token, userId, username) or error
     */
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(username, password)
            val response = apiService.login(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current user profile
     *
     * @return Result with UserResponse or error
     *
     * NOTE: AuthInterceptor automatically adds JWT token to this request!
     */
    suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            val response = apiService.getCurrentUser()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // TODO: Add methods for updateProfile, changePassword, etc.
}
