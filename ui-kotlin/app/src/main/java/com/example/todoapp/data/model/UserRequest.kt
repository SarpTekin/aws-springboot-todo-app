package com.example.todoapp.data.model

import kotlinx.serialization.Serializable

/**
 * User Registration Request Model
 *
 * This is what we SEND to the backend when registering a new user.
 *
 * Endpoint: POST /api/users
 *
 * Validation rules (enforced by backend):
 * - username: 3-50 characters, required
 * - email: valid email format, required
 * - password: minimum 8 characters, required
 * - firstName: max 50 characters, optional
 * - lastName: max 50 characters, optional
 */
@Serializable
data class UserRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null
)
