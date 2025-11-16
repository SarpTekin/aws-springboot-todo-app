package com.microtodo.android.data.repository

import com.microtodo.android.data.local.TokenManager
import com.microtodo.android.data.remote.api.UserApiService
import com.microtodo.android.data.remote.dto.LoginRequest
import com.microtodo.android.data.remote.dto.UserRequest
import com.microtodo.android.domain.model.AuthResult
import com.microtodo.android.domain.model.User
import com.microtodo.android.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Implementation of AuthRepository
 * Handles authentication, registration, and session management
 */
class AuthRepositoryImpl(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<AuthResult> {
        return try {
            val response = userApiService.login(LoginRequest(username, password))

            // Save authentication data
            tokenManager.saveAuthData(
                token = response.token,
                userId = response.userId,
                username = response.username
            )

            Result.success(
                AuthResult(
                    token = response.token,
                    userId = response.userId,
                    username = response.username
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String?,
        lastName: String?
    ): Result<User> {
        return try {
            val response = userApiService.registerUser(
                UserRequest(
                    username = username,
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )
            )

            Result.success(
                User(
                    id = response.id,
                    username = response.username,
                    email = response.email,
                    firstName = response.firstName,
                    lastName = response.lastName
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        tokenManager.clearAuthData()
    }

    override fun isAuthenticated(): Flow<Boolean> {
        return tokenManager.isAuthenticated()
    }

    override suspend fun getCurrentUserId(): Long? {
        return tokenManager.getUserId().first()
    }

    override suspend fun getCurrentUsername(): String? {
        return tokenManager.getUsername().first()
    }
}
