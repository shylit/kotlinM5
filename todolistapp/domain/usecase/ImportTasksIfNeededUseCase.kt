package com.example.todolistapp.domain.usecase

import com.example.todolistapp.data.repository.TaskJsonImporter
import com.example.todolistapp.domain.repository.TaskRepository

class ImportTasksIfNeededUseCase(
    private val taskRepository: TaskRepository,
    private val taskJsonImporter: TaskJsonImporter
) {
    suspend operator fun invoke() {
        val count = taskRepository.getTaskCount()

        if (count == 0) {
            val tasks = taskJsonImporter.readTasksFromJson()
            tasks.forEach { task ->
                taskRepository.insertTask(task)
            }
        }
    }
}
