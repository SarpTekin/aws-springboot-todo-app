package com.microtodo.android.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: Long,
    val username: String
)

@Serializable
data class UserRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null
)

@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val createdAt: String,
    val updatedAt: String
)
