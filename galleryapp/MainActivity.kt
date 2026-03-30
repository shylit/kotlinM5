package com.example.galleryapp

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: GalleryViewModel by viewModels()

    private var latestPhotoFile: File? = null

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                viewModel.loadPhotos()
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = androidx.compose.runtime.rememberCoroutineScope()

            GalleryScreen(
                photos = viewModel.photos,
                snackbarHostState = snackbarHostState,
                onTakePhotoClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onExportClick = { photo ->
                    val file = File(photo.filePath)
                    if (file.exists()) {
                        val ok = exportImageToGallery(file)
                        scope.launch {
                            if (ok) {
                                snackbarHostState.showSnackbar("Фото добавлено в галерею")
                            } else {
                                snackbarHostState.showSnackbar("Не удалось экспортировать фото")
                            }
                        }
                    }
                }
            )
        }
    }

    private fun openCamera() {
        val photoFile = viewModel.createImageFile()
        latestPhotoFile = photoFile

        val photoUri: Uri = FileProvider.getUriForFile(
            this,
            "com.example.galleryapp.fileprovider",
            photoFile
        )

        takePictureLauncher.launch(photoUri)
    }

    private fun exportImageToGallery(sourceFile: File): Boolean {
        return try {
            val contentValues = android.content.ContentValues().apply {
                put(
                    android.provider.MediaStore.Images.Media.DISPLAY_NAME,
                    sourceFile.name
                )
                put(
                    android.provider.MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )
                put(
                    android.provider.MediaStore.Images.Media.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_PICTURES + "/GalleryApp"
                )
                put(
                    android.provider.MediaStore.Images.Media.IS_PENDING,
                    1
                )
            }

            val uri = contentResolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return false

            contentResolver.openOutputStream(uri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            contentValues.clear()
            contentValues.put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(uri, contentValues, null, null)

            true
        } catch (e: Exception) {
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun GalleryScreen(
    photos: List<PhotoItem>,
    snackbarHostState: SnackbarHostState,
    onTakePhotoClick: () -> Unit,
    onExportClick: (PhotoItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Галерея фото") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onTakePhotoClick) {
                Text("📷")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (photos.isEmpty()) {
            EmptyGalleryScreen(
                modifier = Modifier.padding(paddingValues),
                onTakePhotoClick = onTakePhotoClick
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos, key = { it.filePath }) { photo ->
                    PhotoGridItem(
                        photo = photo,
                        onExportClick = { onExportClick(photo) }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun EmptyGalleryScreen(
    modifier: Modifier = Modifier,
    onTakePhotoClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "У вас пока нет фото",
            style = MaterialTheme.typography.headlineSmall
        )

        Button(
            onClick = onTakePhotoClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Сделать первое фото")
        }
    }
}

@androidx.compose.runtime.Composable
fun PhotoGridItem(
    photo: PhotoItem,
    onExportClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onExportClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = photo.filePath),
            contentDescription = photo.fileName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
