package com.example.notesapp.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.example.notesapp.presentation.trash.TrashScreen

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
                        Triple("vault", stringResource(R.string.vault), Icons.Default.Lock),
                        Triple("tasks", stringResource(R.string.tasks), Icons.AutoMirrored.Filled.List),
                        Triple("focus", stringResource(R.string.focus), Icons.Default.Timer),
                        Triple("search", stringResource(R.string.search), Icons.Default.Search)
                    )

                    items.forEach { (route, label, icon) ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
                        
                        NavigationBarItem(
                            icon = { 
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(if (isSelected) 56.dp else 48.dp)
                                        .then(
                                            if (isSelected) Modifier.background(
                                                MaterialTheme.colorScheme.secondaryContainer,
                                                CircleShape
                                            ) else Modifier
                                        )
                                ) {
                                    Icon(
                                        imageVector = icon, 
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer 
                                               else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            label = { 
                                Text(
                                    text = label, 
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            ),
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
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn() + slideInHorizontally { it } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it } },
            popEnterTransition = { fadeIn() + slideInHorizontally { -it } },
            popExitTransition = { fadeOut() + slideOutHorizontally { it } }
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToEditor = { id ->
                        val route = if (id != null) "editor?noteId=$id" else "editor"
                        navController.navigate(route)
                    },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToArchive = { navController.navigate("archive") },
                    onNavigateToTrash = { navController.navigate("trash") }
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
                    },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("focus") {
                FocusScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("search") {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditor = { id -> navController.navigate("editor?noteId=$id") },
                    onNavigateToTaskEditor = { id -> navController.navigate("task_editor?taskId=$id") }
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
            composable("trash") {
                TrashScreen(onNavigateBack = { navController.popBackStack() })
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
