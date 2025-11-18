package com.example.todoapp.data.model

import kotlinx.serialization.Serializable

/**
 * Availability Check Response Model
 *
 * Used for checking if a username or email is available.
 *
 * Backend returns: {"available": true} or {"available": false}
 * Kotlin automatically converts this JSON to this data class.
 *
 * @property available true if username/email is available, false if already taken
 */
@Serializable
data class AvailabilityResponse(
    val available: Boolean
)
