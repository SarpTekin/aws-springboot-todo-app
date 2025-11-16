package com.microtodo.android.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskRequest(
    val title: String,
    val description: String? = null,
    val userId: Long,
    val status: TaskStatus? = null
)

@Serializable
data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String? = null,
    val status: TaskStatus,
    val userId: Long,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
