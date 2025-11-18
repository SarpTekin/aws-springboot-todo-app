package com.example.todoapp.data.model

import kotlinx.serialization.Serializable

/**
 * Error Response Model
 *
 * When something goes wrong, the backend sends error details.
 * This helps us show meaningful error messages to users.
 *
 * Example JSON:
 * {
 *   "error": "Invalid credentials",
 *   "details": [
 *     {"field": "username", "message": "Username is required"}
 *   ]
 * }
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val details: List<FieldError>? = null
)

@Serializable
data class FieldError(
    val field: String,
    val message: String
)
