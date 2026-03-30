package com.example.diaryapp

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import java.io.File

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    val entries = mutableStateListOf<DiaryEntry>()

    init {
        loadEntriesOnce()
    }

    private fun loadEntriesOnce() {
        val filesDir = getApplication<Application>().filesDir
        val files = filesDir.listFiles()?.filter { it.isFile && it.extension == "txt" } ?: emptyList()

        val loadedEntries = files.mapNotNull { file ->
            try {
                val lines = file.readLines()
                val title = lines.firstOrNull() ?: ""
                val text = if (lines.size > 1) {
                    lines.drop(1).joinToString("\n")
                } else {
                    ""
                }

                val timePart = file.nameWithoutExtension.substringBefore("_")
                val timestamp = timePart.toLongOrNull() ?: file.lastModified()

                DiaryEntry(
                    fileName = file.name,
                    title = title,
                    text = text,
                    timestamp = timestamp
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.timestamp }

        entries.clear()
        entries.addAll(loadedEntries)
    }

    fun saveNewEntry(title: String, text: String) {
        val cleanTitle = title.trim().replace(" ", "_")
        val timestamp = System.currentTimeMillis()

        val fileName = if (cleanTitle.isNotEmpty()) {
            "${timestamp}_${cleanTitle}.txt"
        } else {
            "${timestamp}.txt"
        }

        val file = File(getApplication<Application>().filesDir, fileName)
        file.writeText(title.trim() + "\n" + text)

        val newEntry = DiaryEntry(
            fileName = fileName,
            title = title.trim(),
            text = text,
            timestamp = timestamp
        )

        entries.add(0, newEntry)
    }

    fun updateEntry(fileName: String, newTitle: String, newText: String) {
        val oldFile = File(getApplication<Application>().filesDir, fileName)
        if (!oldFile.exists()) return

        val oldTimestamp = fileName.substringBefore("_").substringBefore(".").toLongOrNull()
            ?: System.currentTimeMillis()

        val cleanTitle = newTitle.trim().replace(" ", "_")
        val newFileName = if (cleanTitle.isNotEmpty()) {
            "${oldTimestamp}_${cleanTitle}.txt"
        } else {
            "${oldTimestamp}.txt"
        }

        val newFile = File(getApplication<Application>().filesDir, newFileName)
        newFile.writeText(newTitle.trim() + "\n" + newText)

        if (newFileName != fileName && oldFile.exists()) {
            oldFile.delete()
        }

        val index = entries.indexOfFirst { it.fileName == fileName }
        if (index != -1) {
            entries[index] = DiaryEntry(
                fileName = newFileName,
                title = newTitle.trim(),
                text = newText,
                timestamp = oldTimestamp
            )
        }
    }

    fun deleteEntry(fileName: String) {
        val file = File(getApplication<Application>().filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }

        val index = entries.indexOfFirst { it.fileName == fileName }
        if (index != -1) {
            entries.removeAt(index)
        }
    }

    fun getEntryByFileName(fileName: String): DiaryEntry? {
        return entries.find { it.fileName == fileName }
    }
}
