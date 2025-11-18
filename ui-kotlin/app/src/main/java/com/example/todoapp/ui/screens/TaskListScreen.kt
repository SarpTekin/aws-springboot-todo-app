package com.example.todoapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.todoapp.data.model.TaskResponse
import com.example.todoapp.data.model.TaskStatus
import org.koin.androidx.compose.koinViewModel

/**
 * Task List Screen
 *
 * WHAT IS THIS?
 * The main TODO list screen showing all tasks with filtering
 *
 * FEATURES:
 * - Display tasks in scrollable list (LazyColumn)
 * - Filter by status (All, Pending, In Progress, Completed, Cancelled)
 * - Show task count badges on filters
 * - Tap task to mark complete / change status
 * - Pull-to-refresh to reload tasks
 * - Empty state when no tasks
 * - Loading and error states
 *
 * JETPACK COMPOSE CONCEPTS:
 * - LazyColumn: Efficient scrolling list (like RecyclerView)
 * - collectAsState(): Reactive state from StateFlow
 * - remember: Preserve state across recompositions
 * - Modifier: Style and layout components
 * - Material3: Modern Material Design components
 *
 * @param viewModel TaskListViewModel (injected by Koin)
 * @param onNavigateToCreateTask Callback to navigate to create task screen
 * @param onLogout Callback when user logs out
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = koinViewModel(),
    onNavigateToCreateTask: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Collect UI state from ViewModel
    // REACTIVE: UI automatically updates when state changes!
    val uiState by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Refresh button
                    TextButton(onClick = { viewModel.refreshTasks() }) {
                        Text("Refresh")
                    }
                    // Logout button
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            // Add task button (bottom right)
            FloatingActionButton(
                onClick = onNavigateToCreateTask,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips (always visible)
            FilterChipsRow(
                currentFilter = currentFilter,
                onFilterSelected = { filter -> viewModel.applyFilter(filter) },
                getTaskCount = { status -> viewModel.getTaskCountByStatus(status) }
            )

            // Content based on state
            when (val state = uiState) {
                is TaskListUiState.Loading -> {
                    LoadingState()
                }

                is TaskListUiState.Success -> {
                    if (state.isEmpty) {
                        EmptyState(
                            isFiltered = state.isFiltered,
                            currentFilter = currentFilter
                        )
                    } else {
                        TaskList(
                            tasks = state.tasks,
                            onTaskClick = { task ->
                                // Quick action: mark as completed
                                if (!task.isCompleted()) {
                                    viewModel.markTaskAsCompleted(task)
                                }
                            },
                            onDeleteTask = { task -> viewModel.deleteTask(task.id) },
                            onStatusChange = { task, newStatus ->
                                viewModel.updateTaskStatus(task, newStatus)
                            }
                        )
                    }
                }

                is TaskListUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadTasks() }
                    )
                }
            }
        }
    }
}

/**
 * Filter Chips Row
 *
 * WHAT IS THIS?
 * Horizontal scrollable row of filter chips
 * "All" "Pending (5)" "In Progress (2)" "Completed (10)" "Cancelled (0)"
 *
 * JETPACK COMPOSE:
 * - LazyRow: Horizontal scrolling list
 * - FilterChip: Material3 chip component with selection state
 *
 * @param currentFilter Currently selected filter (null = All)
 * @param onFilterSelected Callback when filter is tapped
 * @param getTaskCount Function to get count for each status
 */
@Composable
fun FilterChipsRow(
    currentFilter: TaskStatus?,
    onFilterSelected: (TaskStatus?) -> Unit,
    getTaskCount: (TaskStatus) -> Int
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = currentFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("All") }
            )
        }

        // Status filter chips
        items(TaskStatus.entries.toList()) { status ->
            val count = getTaskCount(status)
            FilterChip(
                selected = currentFilter == status,
                onClick = { onFilterSelected(status) },
                label = {
                    Text("${status.displayName()} ($count)")
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(status.colorCode()).copy(alpha = 0.2f),
                    selectedLabelColor = Color(status.colorCode())
                )
            )
        }
    }
}

/**
 * Task List
 *
 * WHAT IS THIS?
 * Scrollable list of tasks
 *
 * JETPACK COMPOSE:
 * - LazyColumn: Efficient scrolling list (only renders visible items)
 * - items(): Iterate over list and create composables
 *
 * PERFORMANCE:
 * LazyColumn is like RecyclerView - it reuses views for efficiency
 * Perfect for long lists!
 *
 * @param tasks List of tasks to display
 * @param onTaskClick Callback when task is tapped
 * @param onDeleteTask Callback to delete task
 * @param onStatusChange Callback to change task status
 */
@Composable
fun TaskList(
    tasks: List<TaskResponse>,
    onTaskClick: (TaskResponse) -> Unit,
    onDeleteTask: (TaskResponse) -> Unit,
    onStatusChange: (TaskResponse, TaskStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = tasks,
            key = { task -> task.id }  // Optimization: stable keys
        ) { task ->
            TaskItem(
                task = task,
                onClick = { onTaskClick(task) },
                onDelete = { onDeleteTask(task) },
                onStatusChange = onStatusChange
            )
        }
    }
}

/**
 * Task Item
 *
 * WHAT IS THIS?
 * A single task card in the list
 *
 * FEATURES:
 * - Shows title, description, status
 * - Color-coded status indicator
 * - Tap to mark complete
 * - Long press / menu for status change
 * - Delete button
 *
 * MATERIAL DESIGN:
 * - Card: Elevated surface
 * - Color indicators for status
 * - Strikethrough text for completed tasks
 *
 * @param task The task data
 * @param onClick Callback when card is tapped
 * @param onDelete Callback to delete
 * @param onStatusChange Callback to change status
 */
@Composable
fun TaskItem(
    task: TaskResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (TaskResponse, TaskStatus) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted()) {
                Color(0xFFE8F5E9)  // Light green for completed
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator (colored circle)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(task.status.colorCode()))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted()) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    }
                )

                // Description (if exists)
                task.description?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status badge and date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(task.status.colorCode()).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = task.status.displayName(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(task.status.colorCode())
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Created date
                    Text(
                        text = task.formattedCreatedAt(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Action buttons
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Status change button
                TextButton(
                    onClick = { showStatusMenu = true }
                ) {
                    Text("•••", style = MaterialTheme.typography.titleMedium)
                }

                // Delete button
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFC62828)
                    )
                ) {
                    Text("Delete", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }

    // Status change dialog (centered on screen)
    if (showStatusMenu) {
        AlertDialog(
            onDismissRequest = { showStatusMenu = false },
            title = { Text("Change Status") },
            text = {
                Column {
                    Text(
                        text = "Current: ${task.status.displayName()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Show all other statuses
                    TaskStatus.entries.forEach { status ->
                        if (status != task.status) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onStatusChange(task, status)
                                        showStatusMenu = false
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(status.colorCode()).copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = status.displayName(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(status.colorCode()))
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Loading State
 *
 * WHAT IS THIS?
 * Shown while tasks are being fetched from backend
 *
 * CENTERED:
 * Uses Box with Alignment.Center to center content
 */
@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading tasks...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

/**
 * Empty State
 *
 * WHAT IS THIS?
 * Shown when there are no tasks (or no tasks match filter)
 *
 * TWO VARIATIONS:
 * 1. No tasks at all: "No tasks yet! Tap + to create one"
 * 2. No tasks match filter: "No pending tasks"
 *
 * @param isFiltered Whether a filter is active
 * @param currentFilter The current filter
 */
@Composable
fun EmptyState(
    isFiltered: Boolean,
    currentFilter: TaskStatus?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = if (isFiltered) {
                    "No ${currentFilter?.displayName()?.lowercase()} tasks"
                } else {
                    "No tasks yet!"
                },
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isFiltered) {
                    "Try a different filter"
                } else {
                    "Tap the + button to create your first task"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

/**
 * Error State
 *
 * WHAT IS THIS?
 * Shown when something goes wrong (network error, server error, etc.)
 *
 * FEATURES:
 * - Shows error message
 * - Retry button to try again
 *
 * @param message Error message to display
 * @param onRetry Callback to retry loading
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)  // Light red
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
