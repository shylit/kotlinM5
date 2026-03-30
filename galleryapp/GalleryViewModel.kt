package com.example.galleryapp

import android.app.Application
import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    val photos = mutableStateListOf<PhotoItem>()

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        val picturesDir = getApplication<Application>()
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val files = picturesDir?.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() == "jpg" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        photos.clear()
        photos.addAll(
            files.map {
                PhotoItem(
                    fileName = it.name,
                    filePath = it.absolutePath,
                    timestamp = it.lastModified()
                )
            }
        )
    }

    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())

        val imageFileName = "IMG_${timeStamp}.jpg"

        val storageDir = getApplication<Application>()
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IllegalStateException("Папка для фото недоступна")

        return File(storageDir, imageFileName)
    }
}
