package com.example.diaryapp

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            val viewModel: DiaryViewModel = viewModel()

            NavHost(
                navController = navController,
                startDestination = "list"
            ) {
                composable("list") {
                    DiaryListScreen(
                        entries = viewModel.entries,
                        onAddClick = {
                            navController.navigate("editor")
                        },
                        onEntryClick = { fileName ->
                            navController.navigate("editor/${Uri.encode(fileName)}")
                        },
                        onDeleteEntry = { fileName ->
                            viewModel.deleteEntry(fileName)
                        }
                    )
                }

                composable("editor") {
                    DiaryEditorScreen(
                        initialTitle = "",
                        initialText = "",
                        isEditing = false,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSaveClick = { title, text ->
                            viewModel.saveNewEntry(title, text)
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "editor/{fileName}",
                    arguments = listOf(
                        navArgument("fileName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val encodedFileName = backStackEntry.arguments?.getString("fileName") ?: ""
                    val fileName = Uri.decode(encodedFileName)
                    val entry = viewModel.getEntryByFileName(fileName)

                    DiaryEditorScreen(
                        initialTitle = entry?.title ?: "",
                        initialText = entry?.text ?: "",
                        isEditing = true,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSaveClick = { title, text ->
                            viewModel.updateEntry(fileName, title, text)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
