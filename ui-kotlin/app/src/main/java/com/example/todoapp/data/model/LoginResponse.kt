package com.example.todoapp.data.model

import kotlinx.serialization.Serializable

/**
 * Login Response Model
 *
 * This is what we RECEIVE from the backend after successful login.
 *
 * Example JSON from backend:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "userId": 1,
 *   "username": "john"
 * }
 *
 * Kotlin automatically converts it to this data class!
 */
@Serializable
data class LoginResponse(
    val token: String,      // JWT token for authentication
    val userId: Long,       // User's ID
    val username: String    // Username
)
