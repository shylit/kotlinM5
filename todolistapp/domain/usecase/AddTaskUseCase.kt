package com.example.todolistapp.domain.usecase

import com.example.todolistapp.domain.model.Task
import com.example.todolistapp.domain.repository.TaskRepository

class AddTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.insertTask(task)
    }
}
