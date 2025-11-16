package com.microtodo.android.domain.model

import com.microtodo.android.data.remote.dto.TaskStatus

/**
 * Domain model for Task
 * Clean, platform-agnostic representation
 */
data class Task(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val userId: Long,
    val createdAt: String,
    val updatedAt: String
) {
    val isCompleted: Boolean
        get() = status == TaskStatus.COMPLETED

    val isPending: Boolean
        get() = status == TaskStatus.PENDING

    val isInProgress: Boolean
        get() = status == TaskStatus.IN_PROGRESS
}
