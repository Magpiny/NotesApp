package com.example.notesapp.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.notesapp.R
import com.example.notesapp.core.BiometricAuthManager
import com.example.notesapp.presentation.archive.ArchiveScreen
import com.example.notesapp.presentation.editor.EditorScreen
import com.example.notesapp.presentation.home.HomeScreen
import com.example.notesapp.presentation.search.SearchScreen
import com.example.notesapp.presentation.settings.SettingsScreen
import com.example.notesapp.presentation.task.AnalyticsScreen
import com.example.notesapp.presentation.task.TaskEditorScreen
import com.example.notesapp.presentation.task.TaskScreen
import com.example.notesapp.presentation.vault.VaultScreen
import com.example.notesapp.presentation.focus.FocusScreen

/**
 * Root scaffold that holds the BottomNavigationBar and hosts the main NavGraph.
 */
@Composable
fun MainScreen(biometricManager: BiometricAuthManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide BottomBar when inside the Editor
    val showBottomBar = currentDestination?.route?.let { 
        it.startsWith("editor") || it.startsWith("task_editor") || it.startsWith("task_analytics") || it.startsWith("focus")
    } != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        Triple("home", stringResource(R.string.home), Icons.Default.Home),
                        Triple("vault", "Vault", Icons.Default.Lock),
                        Triple("tasks", stringResource(R.string.tasks), Icons.AutoMirrored.Filled.List),
                        Triple("focus", "Focus", Icons.Default.Timer),
                        Triple("search", stringResource(R.string.search), Icons.Default.Search),
                        Triple("settings", stringResource(R.string.settings), Icons.Default.Settings)
                    )

                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, maxLines = 1) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToEditor = { id ->
                        val route = if (id != null) "editor?noteId=$id" else "editor"
                        navController.navigate(route)
                    }
                )
            }
            composable("vault") {
                VaultScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditor = { id ->
                        val route = if (id != null) "editor?noteId=$id" else "editor"
                        navController.navigate(route)
                    },
                    biometricManager = biometricManager
                )
            }
            composable("tasks") {
                TaskScreen(
                    onNavigateToTaskEditor = { id ->
                        val route = if (id != null) "task_editor?taskId=$id" else "task_editor"
                        navController.navigate(route)
                    },
                    onNavigateToAnalytics = {
                        navController.navigate("task_analytics")
                    }
                )
            }
            composable("focus") {
                FocusScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("search") {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditor = { id -> navController.navigate("editor?noteId=$id") }
                )
            }
            composable("archive") {
                ArchiveScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditor = { id -> navController.navigate("editor?noteId=$id") }
                )
            }
            composable("settings") {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = "task_editor?taskId={taskId}",
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                TaskEditorScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = "editor?noteId={noteId}",
                arguments = listOf(
                    navArgument("noteId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                EditorScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("task_analytics") {
                AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
