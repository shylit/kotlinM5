package com.example.todolistapp.presentation.viewmodel

import com.example.todolistapp.domain.model.Task

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val completedTaskColorEnabled: Boolean = true
)
