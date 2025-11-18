package com.example.todoapp.data.model

import kotlinx.serialization.Serializable

/**
 * User Response Model
 *
 * Represents user profile data from the backend.
 * Used when getting user info from /api/users/me
 *
 * Example JSON:
 * {
 *   "id": 1,
 *   "username": "john_doe",
 *   "email": "john@example.com",
 *   "firstName": "John",
 *   "lastName": "Doe"
 * }
 */
@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null
)
