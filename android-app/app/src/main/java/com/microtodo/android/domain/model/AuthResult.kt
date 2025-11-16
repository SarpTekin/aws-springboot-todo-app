package com.microtodo.android.domain.model

/**
 * Domain model for authentication result
 */
data class AuthResult(
    val token: String,
    val userId: Long,
    val username: String
)
