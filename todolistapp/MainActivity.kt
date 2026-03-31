package com.example.todolistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolistapp.presentation.ui.screen.TodoListScreen
import com.example.todolistapp.presentation.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: TaskViewModel = viewModel(factory = TaskViewModel.Factory)
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()

            TodoListScreen(
                uiState = uiState.value,
                onAddTask = { title, description ->
                    viewModel.addTask(title, description)
                },
                onUpdateTask = { task ->
                    viewModel.updateTask(task)
                },
                onDeleteTask = { task ->
                    viewModel.deleteTask(task)
                },
                onToggleTaskCompleted = { task ->
                    viewModel.toggleTaskCompleted(task)
                },
                onColorEnabledChange = { enabled ->
                    viewModel.setCompletedTaskColorEnabled(enabled)
                }
            )
        }
    }
}
