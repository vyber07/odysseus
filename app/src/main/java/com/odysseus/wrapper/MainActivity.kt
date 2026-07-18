package com.odysseus.wrapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.odysseus.wrapper.core.NetworkClient
import com.odysseus.wrapper.core.UserPreferences
import com.odysseus.wrapper.ui.screens.auth.AuthViewModel
import com.odysseus.wrapper.ui.screens.auth.FirstRunScreen
import com.odysseus.wrapper.ui.screens.auth.LoginScreen
import com.odysseus.wrapper.ui.screens.calendar.CalendarScreen
import com.odysseus.wrapper.ui.screens.chat.ChatScreen
import com.odysseus.wrapper.ui.screens.documents.DocumentsScreen
import com.odysseus.wrapper.ui.screens.email.EmailScreen
import com.odysseus.wrapper.ui.screens.gallery.GalleryScreen
import com.odysseus.wrapper.ui.screens.memory.MemoryScreen
import com.odysseus.wrapper.ui.screens.notes.NotesScreen
import com.odysseus.wrapper.ui.screens.research.ResearchScreen
import com.odysseus.wrapper.ui.screens.settings.SettingsScreen
import com.odysseus.wrapper.ui.screens.tasks.TasksScreen
import com.odysseus.wrapper.ui.theme.OdysseusTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Restore saved server URL before any network call
        val prefs = UserPreferences(this)
        val savedUrl = runBlocking { prefs.serverUrl.first() }
        if (savedUrl.isNotEmpty()) NetworkClient.baseUrl = savedUrl

        setContent {
            val authVm: AuthViewModel = viewModel()
            val darkMode by authVm.darkMode.collectAsStateWithLifecycle()
            OdysseusTheme(darkTheme = darkMode) {
                OdysseusApp(authVm)
            }
        }
    }
}

// ── Route definitions ─────────────────────────────────────────────────────────

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object FirstRun  : Screen("first_run",  "Setup",    Icons.Default.Settings)
    object Login     : Screen("login",      "Login",    Icons.Default.Login)
    object Chat      : Screen("chat",       "Chat",     Icons.Default.Chat)
    object Notes     : Screen("notes",      "Notes",    Icons.Default.Notes)
    object Tasks     : Screen("tasks",      "Tasks",    Icons.Default.Schedule)
    object Calendar  : Screen("calendar",   "Calendar", Icons.Default.CalendarMonth)
    object Email     : Screen("email",      "Email",    Icons.Default.Email)
    object Documents : Screen("documents",  "Docs",     Icons.Default.Description)
    object Gallery   : Screen("gallery",    "Gallery",  Icons.Default.PhotoLibrary)
    object Research  : Screen("research",   "Research", Icons.Default.TravelExplore)
    object Memory    : Screen("memory",     "Memory",   Icons.Default.Psychology)
    object Settings  : Screen("settings",   "Settings", Icons.Default.Settings)
}

// First 5 shown in bottom nav always; rest reachable via "More"
private val bottomNavMain = listOf(
    Screen.Chat, Screen.Notes, Screen.Tasks, Screen.Calendar, Screen.Email
)
private val bottomNavMore = listOf(
    Screen.Documents, Screen.Gallery, Screen.Research, Screen.Memory, Screen.Settings
)

// ── Root composable ───────────────────────────────────────────────────────────

@Composable
fun OdysseusApp(authVm: AuthViewModel) {
    val navController = rememberNavController()
    val savedUrl      by authVm.savedServerUrl.collectAsStateWithLifecycle()
    val isLoggedIn    by authVm.prefs.isLoggedIn.collectAsStateWithLifecycle(false)
    val firstRunDone  by authVm.firstRunDone.collectAsStateWithLifecycle()
    val currentEntry  by navController.currentBackStackEntryAsState()
    val currentRoute  = currentEntry?.destination?.route

    val authRoutes    = setOf(Screen.FirstRun.route, Screen.Login.route)
    val showBottomBar = currentRoute != null && currentRoute !in authRoutes

    // Determine start destination
    val startDest = when {
        !firstRunDone || savedUrl.isEmpty() -> Screen.FirstRun.route
        !isLoggedIn                          -> Screen.Login.route
        else                                 -> Screen.Chat.route
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomNavMain.forEach { screen ->
                        NavigationBarItem(
                            icon     = { Icon(screen.icon, screen.label) },
                            label    = { Text(screen.label) },
                            selected = currentRoute == screen.route,
                            onClick  = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                    // "More" tab — highlights when any extra screen is active
                    NavigationBarItem(
                        icon     = { Icon(Icons.Default.MoreHoriz, "More") },
                        label    = { Text("More") },
                        selected = bottomNavMore.any { it.route == currentRoute },
                        onClick  = {
                            // Navigate to first "more" screen, or cycle through them
                            val idx  = bottomNavMore.indexOfFirst { it.route == currentRoute }
                            val next = bottomNavMore.getOrElse((idx + 1) % bottomNavMore.size) { Screen.Documents }
                            navController.navigate(next.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = startDest,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.FirstRun.route) {
                FirstRunScreen(vm = authVm, onDone = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.FirstRun.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Login.route) {
                LoginScreen(vm = authVm, onLoggedIn = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Chat.route)      { ChatScreen() }
            composable(Screen.Notes.route)     { NotesScreen() }
            composable(Screen.Tasks.route)     { TasksScreen() }
            composable(Screen.Calendar.route)  { CalendarScreen() }
            composable(Screen.Email.route)     { EmailScreen() }
            composable(Screen.Documents.route) { DocumentsScreen() }
            composable(Screen.Gallery.route)   { GalleryScreen() }
            composable(Screen.Research.route)  { ResearchScreen() }
            composable(Screen.Memory.route)    { MemoryScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(authVm = authVm, onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
        }
    }
}
