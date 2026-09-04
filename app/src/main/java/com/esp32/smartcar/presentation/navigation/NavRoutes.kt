package com.esp32.smartcar.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Dashboard", Icons.Default.Dashboard)
    object Drive : Screen("drive", "Drive", Icons.Default.SportsEsports)
    object Voice : Screen("voice", "Voice", Icons.Default.Mic)
    object Sensors : Screen("sensors", "Sensors", Icons.Default.Sensors)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Drive,
    Screen.Voice,
    Screen.Sensors,
    Screen.Settings
)
