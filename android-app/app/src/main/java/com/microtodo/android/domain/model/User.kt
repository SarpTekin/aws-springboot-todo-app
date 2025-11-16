package com.microtodo.android.domain.model

/**
 * Domain model for User
 * Clean, platform-agnostic representation
 */
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String?,
    val lastName: String?
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" ")
            ?: username
}
