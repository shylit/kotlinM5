package com.example.todolistapp.data.repository

import android.content.Context
import com.example.todolistapp.data.model.TaskJsonModel
import com.example.todolistapp.domain.model.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskJsonImporter(
    private val context: Context
) {
    fun readTasksFromJson(): List<Task> {
        return try {
            val json = context.assets.open("tasks.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<TaskJsonModel>>() {}.type
            val jsonTasks: List<TaskJsonModel> = Gson().fromJson(json, type)

            jsonTasks.map {
                Task(
                    title = it.title,
                    description = it.description,
                    isCompleted = it.isCompleted
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
