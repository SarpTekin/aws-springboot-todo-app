package com.microtodo.android.domain.repository

import com.microtodo.android.domain.model.AuthResult
import com.microtodo.android.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 * Domain layer defines the contract, data layer implements it
 */
interface AuthRepository {

    /**
     * Authenticate user with username and password
     * Returns JWT token on success
     */
    suspend fun login(username: String, password: String): Result<AuthResult>

    /**
     * Register a new user
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String?,
        lastName: String?
    ): Result<User>

    /**
     * Logout current user (clear session)
     */
    suspend fun logout()

    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Flow<Boolean>

    /**
     * Get current user ID
     */
    suspend fun getCurrentUserId(): Long?

    /**
     * Get current username
     */
    suspend fun getCurrentUsername(): String?
}
