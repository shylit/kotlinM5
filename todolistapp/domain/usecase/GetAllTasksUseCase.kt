package com.example.todolistapp.domain.usecase

import com.example.todolistapp.domain.model.Task
import com.example.todolistapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetAllTasksUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return repository.getAllTasks()
    }
}
