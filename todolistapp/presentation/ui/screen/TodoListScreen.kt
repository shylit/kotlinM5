package com.example.todolistapp.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.todolistapp.domain.model.Task
import com.example.todolistapp.presentation.ui.component.TaskDialog
import com.example.todolistapp.presentation.ui.component.TaskItem
import com.example.todolistapp.presentation.viewmodel.TaskUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    uiState: TaskUiState,
    onAddTask: (String, String) -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onToggleTaskCompleted: (Task) -> Unit,
    onColorEnabledChange: (Boolean) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todo List") },
                actions = {
                    Text(
                        text = "Цвет завершенных",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = uiState.completedTaskColorEnabled,
                        onCheckedChange = onColorEnabledChange
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Text("+")
            }
        }
    ) { paddingValues ->
        if (uiState.tasks.isEmpty()) {
            EmptyScreen(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.tasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        completedTaskColorEnabled = uiState.completedTaskColorEnabled,
                        onToggleCompleted = { onToggleTaskCompleted(task) },
                        onEditClick = { taskToEdit = task },
                        onDeleteClick = { onDeleteTask(task) }
                    )
                }
            }
        }

        if (showAddDialog) {
            TaskDialog(
                title = "Добавить задачу",
                initialTitle = "",
                initialDescription = "",
                onDismiss = { showAddDialog = false },
                onConfirm = { title, description ->
                    onAddTask(title, description)
                    showAddDialog = false
                }
            )
        }

        if (taskToEdit != null) {
            TaskDialog(
                title = "Изменить задачу",
                initialTitle = taskToEdit!!.title,
                initialDescription = taskToEdit!!.description,
                onDismiss = { taskToEdit = null },
                onConfirm = { title, description ->
                    onUpdateTask(
                        taskToEdit!!.copy(
                            title = title,
                            description = description
                        )
                    )
                    taskToEdit = null
                }
            )
        }
    }
}

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Список задач пуст",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Нажмите +, чтобы добавить первую задачу",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
