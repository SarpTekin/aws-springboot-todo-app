package com.microtodo.android.domain.usecase

import com.microtodo.android.domain.model.AuthResult
import com.microtodo.android.domain.repository.AuthRepository

/**
 * Use Case for user login
 * Encapsulates business logic for authentication
 */
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<AuthResult> {
        // Validate inputs
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be empty"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }

        // Delegate to repository
        return authRepository.login(username, password)
    }
}
