package com.example.todolistapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.todolistapp.TodoListApplication
import com.example.todolistapp.data.preferences.TaskPreferencesRepository
import com.example.todolistapp.data.repository.TaskJsonImporter
import com.example.todolistapp.domain.model.Task
import com.example.todolistapp.domain.repository.TaskRepository
import com.example.todolistapp.domain.usecase.AddTaskUseCase
import com.example.todolistapp.domain.usecase.DeleteTaskUseCase
import com.example.todolistapp.domain.usecase.GetAllTasksUseCase
import com.example.todolistapp.domain.usecase.ImportTasksIfNeededUseCase
import com.example.todolistapp.domain.usecase.UpdateTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val preferencesRepository: TaskPreferencesRepository,
    private val importTasksIfNeededUseCase: ImportTasksIfNeededUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            importTasksIfNeededUseCase()
        }

        viewModelScope.launch {
            getAllTasksUseCase().collect { tasks ->
                _uiState.value = _uiState.value.copy(tasks = tasks)
            }
        }

        viewModelScope.launch {
            preferencesRepository.completedTaskColorEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(
                    completedTaskColorEnabled = enabled
                )
            }
        }
    }

    fun addTask(title: String, description: String) {
        if (title.isBlank()) return

        viewModelScope.launch {
            addTaskUseCase(
                Task(
                    title = title.trim(),
                    description = description.trim(),
                    isCompleted = false
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            deleteTaskUseCase(task)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            updateTaskUseCase(
                task.copy(isCompleted = !task.isCompleted)
            )
        }
    }

    fun setCompletedTaskColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveCompletedTaskColorEnabled(enabled)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TodoListApplication

                val taskRepository = application.taskRepository
                val preferencesRepository = application.taskPreferencesRepository
                val taskJsonImporter = TaskJsonImporter(application)

                TaskViewModel(
                    taskRepository = taskRepository,
                    preferencesRepository = preferencesRepository,
                    importTasksIfNeededUseCase = ImportTasksIfNeededUseCase(
                        taskRepository,
                        taskJsonImporter
                    ),
                    getAllTasksUseCase = GetAllTasksUseCase(taskRepository),
                    addTaskUseCase = AddTaskUseCase(taskRepository),
                    updateTaskUseCase = UpdateTaskUseCase(taskRepository),
                    deleteTaskUseCase = DeleteTaskUseCase(taskRepository)
                )
            }
        }
    }
}
