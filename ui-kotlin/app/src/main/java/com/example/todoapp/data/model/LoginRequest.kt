package com.example.todoapp.data.model

import kotlinx.serialization.Serializable

/**
 * Login Request Model
 *
 * This is what we SEND to the backend when logging in.
 *
 * @Serializable annotation tells Kotlin how to convert this class to JSON:
 * LoginRequest("john", "pass123") -> {"username":"john","password":"pass123"}
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)
