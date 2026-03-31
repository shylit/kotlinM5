package com.example.todolistapp

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.todolistapp.data.local.AppDatabase
import com.example.todolistapp.data.preferences.TaskPreferencesRepository
import com.example.todolistapp.data.repository.TaskRepositoryImpl
import com.example.todolistapp.domain.repository.TaskRepository

private const val PREFERENCES_NAME = "task_preferences"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

class TodoListApplication : Application() {

    lateinit var taskRepository: TaskRepository
    lateinit var taskPreferencesRepository: TaskPreferencesRepository

    override fun onCreate() {
        super.onCreate()

        val database = AppDatabase.getDatabase(this)

        taskRepository = TaskRepositoryImpl(database.taskDao())
        taskPreferencesRepository = TaskPreferencesRepository(dataStore)
    }
}
