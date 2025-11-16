package com.microtodo.android.data

import com.microtodo.android.data.remote.api.TaskApiService
import com.microtodo.android.data.remote.dto.TaskRequest
import com.microtodo.android.data.remote.dto.TaskResponse
import com.microtodo.android.data.remote.dto.TaskStatus
import com.microtodo.android.data.repository.TaskRepositoryImpl
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TaskRepositoryTest {

    private lateinit var repository: TaskRepositoryImpl
    private lateinit var taskApiService: TaskApiService

    private val mockTaskResponse = TaskResponse(
        id = 1L,
        title = "Test Task",
        description = "Test Description",
        status = TaskStatus.PENDING,
        userId = 1L,
        createdAt = "2024-01-01T00:00:00",
        updatedAt = "2024-01-01T00:00:00"
    )

    @Before
    fun setup() {
        taskApiService = mockk()
        repository = TaskRepositoryImpl(taskApiService)
    }

    @Test
    fun `createTask success should return Task`() = runTest {
        // Given
        val request = TaskRequest(
            title = "Test Task",
            description = "Test Description",
            userId = 1L,
            status = TaskStatus.PENDING
        )
        coEvery { taskApiService.createTask(request) } returns mockTaskResponse

        // When
        val result = repository.createTask(
            title = "Test Task",
            description = "Test Description",
            userId = 1L,
            status = TaskStatus.PENDING
        )

        // Then
        assertTrue(result.isSuccess)
        val task = result.getOrNull()!!
        assertEquals("Test Task", task.title)
        assertEquals(TaskStatus.PENDING, task.status)

        coVerify { taskApiService.createTask(request) }
    }

    @Test
    fun `getAllTasks success should return list of tasks`() = runTest {
        // Given
        val tasksList = listOf(mockTaskResponse)
        coEvery { taskApiService.getAllTasks(1L) } returns tasksList

        // When
        val result = repository.getAllTasks(userId = 1L)

        // Then
        assertTrue(result.isSuccess)
        val tasks = result.getOrNull()!!
        assertEquals(1, tasks.size)
        assertEquals("Test Task", tasks[0].title)

        coVerify { taskApiService.getAllTasks(1L) }
    }

    @Test
    fun `updateTask success should return updated Task`() = runTest {
        // Given
        val updatedResponse = mockTaskResponse.copy(status = TaskStatus.COMPLETED)
        val request = TaskRequest(
            title = "Test Task",
            description = "Test Description",
            userId = 1L,
            status = TaskStatus.COMPLETED
        )
        coEvery { taskApiService.updateTask(1L, request) } returns updatedResponse

        // When
        val result = repository.updateTask(
            id = 1L,
            title = "Test Task",
            description = "Test Description",
            userId = 1L,
            status = TaskStatus.COMPLETED
        )

        // Then
        assertTrue(result.isSuccess)
        val task = result.getOrNull()!!
        assertEquals(TaskStatus.COMPLETED, task.status)

        coVerify { taskApiService.updateTask(1L, request) }
    }

    @Test
    fun `deleteTask success should return Unit`() = runTest {
        // Given
        coEvery { taskApiService.deleteTask(1L) } just Runs

        // When
        val result = repository.deleteTask(1L)

        // Then
        assertTrue(result.isSuccess)
        coVerify { taskApiService.deleteTask(1L) }
    }

    @Test
    fun `createTask failure should return error`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { taskApiService.createTask(any()) } throws exception

        // When
        val result = repository.createTask("Task", null, 1L, null)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
