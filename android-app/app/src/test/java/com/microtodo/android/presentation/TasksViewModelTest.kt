package com.microtodo.android.presentation

import app.cash.turbine.test
import com.microtodo.android.data.remote.dto.TaskStatus
import com.microtodo.android.domain.model.Task
import com.microtodo.android.domain.repository.AuthRepository
import com.microtodo.android.domain.usecase.CreateTaskUseCase
import com.microtodo.android.domain.usecase.DeleteTaskUseCase
import com.microtodo.android.domain.usecase.GetTasksUseCase
import com.microtodo.android.domain.usecase.UpdateTaskUseCase
import com.microtodo.android.presentation.tasks.TasksViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private lateinit var viewModel: TasksViewModel
    private lateinit var getTasksUseCase: GetTasksUseCase
    private lateinit var createTaskUseCase: CreateTaskUseCase
    private lateinit var updateTaskUseCase: UpdateTaskUseCase
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    private val mockTask = Task(
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
        Dispatchers.setMain(testDispatcher)
        getTasksUseCase = mockk()
        createTaskUseCase = mockk()
        updateTaskUseCase = mockk()
        deleteTaskUseCase = mockk()
        authRepository = mockk()

        // Default mock for username
        every { authRepository.getCurrentUsername() } returns flowOf("testuser")

        // Mock initial load
        coEvery { getTasksUseCase() } returns Result.success(emptyList())

        viewModel = TasksViewModel(
            getTasksUseCase,
            createTaskUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            authRepository
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load should fetch tasks`() = runTest {
        // Given
        val tasks = listOf(mockTask)
        coEvery { getTasksUseCase() } returns Result.success(tasks)

        // When
        viewModel.loadTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(tasks, state.tasks)
            assertFalse(state.isLoading)
        }

        coVerify { getTasksUseCase() }
    }

    @Test
    fun `loadTasks failure should set error state`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getTasksUseCase() } returns Result.failure(Exception(errorMessage))

        // When
        viewModel.loadTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `createTask success should add task to list`() = runTest {
        // Given
        val newTask = mockTask.copy(id = 2L, title = "New Task")
        coEvery { createTaskUseCase("New Task", null) } returns Result.success(newTask)

        // When
        viewModel.onNewTaskTitleChanged("New Task")
        viewModel.createTask()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.tasks.contains(newTask))
            assertEquals("", state.newTaskTitle)
            assertFalse(state.showCreateDialog)
        }

        coVerify { createTaskUseCase("New Task", null) }
    }

    @Test
    fun `updateTaskStatus should update task in list`() = runTest {
        // Given
        val tasks = listOf(mockTask)
        coEvery { getTasksUseCase() } returns Result.success(tasks)
        viewModel.loadTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedTask = mockTask.copy(status = TaskStatus.COMPLETED)
        coEvery {
            updateTaskUseCase(1L, "Test Task", "Test Description", TaskStatus.COMPLETED)
        } returns Result.success(updatedTask)

        // When
        viewModel.updateTaskStatus(1L, TaskStatus.COMPLETED)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            val task = state.tasks.find { it.id == 1L }
            assertEquals(TaskStatus.COMPLETED, task?.status)
        }
    }

    @Test
    fun `deleteTask should remove task from list`() = runTest {
        // Given
        val tasks = listOf(mockTask)
        coEvery { getTasksUseCase() } returns Result.success(tasks)
        viewModel.loadTasks()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { deleteTaskUseCase(1L) } returns Result.success(Unit)

        // When
        viewModel.deleteTask(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.tasks.any { it.id == 1L })
        }

        coVerify { deleteTaskUseCase(1L) }
    }

    @Test
    fun `showCreateDialog should set dialog state`() = runTest {
        viewModel.showCreateDialog()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.showCreateDialog)
        }
    }

    @Test
    fun `hideCreateDialog should clear dialog and inputs`() = runTest {
        viewModel.onNewTaskTitleChanged("Some title")
        viewModel.showCreateDialog()
        viewModel.hideCreateDialog()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.showCreateDialog)
            assertEquals("", state.newTaskTitle)
            assertEquals("", state.newTaskDescription)
        }
    }
}
