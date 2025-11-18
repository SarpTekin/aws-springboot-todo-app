package com.example.todoapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todoapp.data.model.TaskStatus
import org.koin.androidx.compose.koinViewModel

/**
 * Create Task Screen
 *
 * WHAT IS THIS?
 * A form screen for creating new tasks
 *
 * FEATURES:
 * - Title input (required)
 * - Description input (optional, multiline)
 * - Status selector (chips for PENDING, IN_PROGRESS, etc.)
 * - Real-time validation with error messages
 * - Create button (disabled while loading)
 * - Cancel button (go back)
 * - Loading state (progress indicator)
 * - Success state (auto-navigate back)
 * - Error state (show error, allow retry)
 *
 * JETPACK COMPOSE CONCEPTS:
 * - TextField: Material3 text input
 * - OutlinedTextField: Text field with outline
 * - FilterChip: Selectable chips for status
 * - Scaffold: Screen structure with top bar
 * - LaunchedEffect: Side effects (navigation)
 * - collectAsState(): Reactive state from ViewModel
 *
 * @param viewModel CreateTaskViewModel (injected by Koin)
 * @param onNavigateBack Callback to navigate back to task list
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    viewModel: CreateTaskViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    // Collect state from ViewModel
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val titleError by viewModel.titleError.collectAsState()
    val descriptionError by viewModel.descriptionError.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation on success
    LaunchedEffect(uiState) {
        if (uiState is CreateTaskUiState.Success) {
            // Task created! Navigate back to task list
            // Task list will automatically refresh and show new task
            onNavigateBack()
            // Clear state after navigation
            viewModel.clearUiState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Task") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    // Back button
                    TextButton(onClick = {
                        viewModel.resetForm()
                        onNavigateBack()
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Scrollable form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title *") },
                placeholder = { Text("Enter task title") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError != null,
                supportingText = {
                    // Show error or character count
                    if (titleError != null) {
                        Text(
                            text = titleError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("${title.length}/200")
                    }
                },
                enabled = uiState !is CreateTaskUiState.Loading,
                singleLine = true
            )

            // Description input (multiline)
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description (optional)") },
                placeholder = { Text("Add details about your task...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                isError = descriptionError != null,
                supportingText = {
                    // Show error or character count
                    if (descriptionError != null) {
                        Text(
                            text = descriptionError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("${description.length}/1000")
                    }
                },
                enabled = uiState !is CreateTaskUiState.Loading,
                maxLines = 6
            )

            // Status selector
            Column {
                Text(
                    text = "Initial Status",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                StatusChipsRow(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { viewModel.updateStatus(it) },
                    enabled = uiState !is CreateTaskUiState.Loading
                )

                Text(
                    text = "Most tasks start as Pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Error message (if create failed)
            if (uiState is CreateTaskUiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)  // Light red
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as CreateTaskUiState.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create button
            Button(
                onClick = { viewModel.createTask() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTaskUiState.Loading
            ) {
                if (uiState is CreateTaskUiState.Loading) {
                    // Show loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Text("Create Task")
                }
            }

            // Cancel button
            OutlinedButton(
                onClick = {
                    viewModel.resetForm()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is CreateTaskUiState.Loading
            ) {
                Text("Cancel")
            }
        }
    }
}

/**
 * Status Chips Row
 *
 * WHAT IS THIS?
 * Horizontal scrollable row of status chips
 * User can select initial status for the task
 *
 * DEFAULT:
 * PENDING is selected by default (most common)
 *
 * @param selectedStatus Currently selected status
 * @param onStatusSelected Callback when status chip is tapped
 * @param enabled Whether chips are clickable (disabled during loading)
 */
@Composable
fun StatusChipsRow(
    selectedStatus: TaskStatus,
    onStatusSelected: (TaskStatus) -> Unit,
    enabled: Boolean = true
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TaskStatus.entries.toList()) { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(status.displayName()) },
                enabled = enabled,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(status.colorCode()).copy(alpha = 0.2f),
                    selectedLabelColor = Color(status.colorCode())
                )
            )
        }
    }
}
