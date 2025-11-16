package com.microtodo.android.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microtodo.android.data.remote.dto.TaskStatus
import com.microtodo.android.domain.model.Task
import com.microtodo.android.domain.repository.AuthRepository
import com.microtodo.android.domain.usecase.CreateTaskUseCase
import com.microtodo.android.domain.usecase.DeleteTaskUseCase
import com.microtodo.android.domain.usecase.GetTasksUseCase
import com.microtodo.android.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Tasks Screen
 * Handles task CRUD operations and UI state
 */
class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    val username: Flow<String> = authRepository.getCurrentUsername()
        .map { it ?: "User" }

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getTasksUseCase().fold(
                onSuccess = { tasks ->
                    _uiState.update {
                        it.copy(
                            tasks = tasks,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load tasks"
                        )
                    }
                }
            )
        }
    }

    fun onNewTaskTitleChanged(title: String) {
        _uiState.update { it.copy(newTaskTitle = title) }
    }

    fun onNewTaskDescriptionChanged(description: String) {
        _uiState.update { it.copy(newTaskDescription = description) }
    }

    fun createTask() {
        val title = _uiState.value.newTaskTitle
        val description = _uiState.value.newTaskDescription

        viewModelScope.launch {
            createTaskUseCase(title, description.takeIf { it.isNotBlank() }).fold(
                onSuccess = { newTask ->
                    _uiState.update {
                        it.copy(
                            tasks = it.tasks + newTask,
                            newTaskTitle = "",
                            newTaskDescription = "",
                            showCreateDialog = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to create task")
                    }
                }
            )
        }
    }

    fun updateTaskStatus(taskId: Long, newStatus: TaskStatus) {
        viewModelScope.launch {
            val task = _uiState.value.tasks.find { it.id == taskId } ?: return@launch

            updateTaskUseCase(taskId, task.title, task.description, newStatus).fold(
                onSuccess = { updatedTask ->
                    _uiState.update {
                        it.copy(
                            tasks = it.tasks.map { t ->
                                if (t.id == taskId) updatedTask else t
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to update task")
                    }
                }
            )
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(tasks = it.tasks.filter { t -> t.id != taskId })
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to delete task")
                    }
                }
            )
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = false,
                newTaskTitle = "",
                newTaskDescription = ""
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val newTaskTitle: String = "",
    val newTaskDescription: String = "",
    val showCreateDialog: Boolean = false
)
