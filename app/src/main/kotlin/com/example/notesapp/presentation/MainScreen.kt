package com.example.notesapp.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.notesapp.presentation.archive.ArchiveScreen
import com.example.notesapp.presentation.editor.EditorScreen
import com.example.notesapp.presentation.home.HomeScreen
import com.example.notesapp.presentation.search.SearchScreen
import com.example.notesapp.presentation.settings.SettingsScreen
import com.example.notesapp.presentation.task.AnalyticsScreen
import com.example.notesapp.presentation.task.TaskEditorScreen
import com.example.notesapp.presentation.task.TaskScreen

/**
 * Root scaffold that holds the BottomNavigationBar and hosts the main NavGraph.
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide BottomBar when inside the Editor
    val showBottomBar = currentDestination?.route?.startsWith("editor") != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        Triple("home", "Home", Icons.Default.Home),
                        Triple("tasks", "Tasks", Icons.AutoMirrored.Filled.List),
                        Triple("search", "Search", Icons.Default.Search),
                        Triple("archive", "Archive", Icons.Default.Archive),
                        Triple("settings", "Settings", Icons.Default.Settings)
                    )

                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
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
            composable("task_analytics") {
                AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
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
        }
    }
}
