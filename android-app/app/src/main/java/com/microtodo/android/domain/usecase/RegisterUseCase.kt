package com.microtodo.android.domain.usecase

import com.microtodo.android.domain.model.User
import com.microtodo.android.domain.repository.AuthRepository

/**
 * Use Case for user registration
 * Encapsulates business logic and validation
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        firstName: String?,
        lastName: String?
    ): Result<User> {
        // Validate inputs
        if (username.isBlank() || username.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters"))
        }
        if (!email.contains("@")) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        if (password.length < 8) {
            return Result.failure(IllegalArgumentException("Password must be at least 8 characters"))
        }

        // Delegate to repository
        return authRepository.register(username, email, password, firstName, lastName)
    }
}
