package com.example.todoapp.data.model

import kotlinx.serialization.Serializable

/**
 * Task Status Enum
 *
 * WHY ENUM?
 * - Type safety: Can't accidentally use invalid status like "done" or "finished"
 * - Autocomplete: IDE shows all valid options
 * - Compile-time checking: Typos are caught before runtime
 * - Easy to change: Add new status in one place
 *
 * KOTLIN ENUM FEATURES:
 * - Can have properties and functions
 * - Can implement interfaces
 * - Each value is a singleton object
 *
 * @Serializable annotation:
 * - Tells kotlinx.serialization how to convert to/from JSON
 * - "PENDING" in JSON → TaskStatus.PENDING in Kotlin
 * - TaskStatus.COMPLETED in Kotlin → "COMPLETED" in JSON
 */
@Serializable
enum class TaskStatus {
    /**
     * Task is created but not started
     * This is the default status when a task is created
     */
    PENDING,

    /**
     * Task is currently being worked on
     * User has started the task but not completed it
     */
    IN_PROGRESS,

    /**
     * Task has been finished
     * The work is done!
     */
    COMPLETED;

    /**
     * Helper function to get display text
     *
     * KOTLIN EXTENSION:
     * You can add functions to enums just like classes
     */
    fun displayName(): String {
        return when (this) {
            PENDING -> "Pending"
            IN_PROGRESS -> "In Progress"
            COMPLETED -> "Completed"
        }
    }

    /**
     * Helper function to get color for UI
     *
     * Returns a color code that can be used in Compose
     */
    fun colorCode(): Long {
        return when (this) {
            PENDING -> 0xFFFFB74D      // Orange
            IN_PROGRESS -> 0xFF42A5F5   // Blue
            COMPLETED -> 0xFF66BB6A     // Green
        }
    }
}
