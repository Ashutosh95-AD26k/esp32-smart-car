package com.esp32.smartcar.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.esp32.smartcar.presentation.drive.DriveScreen
import com.esp32.smartcar.presentation.drive.DriveViewModel
import com.esp32.smartcar.presentation.home.HomeScreen
import com.esp32.smartcar.presentation.home.HomeViewModel
import com.esp32.smartcar.presentation.sensors.SensorsScreen
import com.esp32.smartcar.presentation.sensors.SensorsViewModel
import com.esp32.smartcar.presentation.settings.SettingsScreen
import com.esp32.smartcar.presentation.settings.SettingsViewModel
import com.esp32.smartcar.presentation.theme.DarkBackground
import com.esp32.smartcar.presentation.theme.DarkSurfaceElevated
import com.esp32.smartcar.presentation.theme.PrimaryCyan
import com.esp32.smartcar.presentation.theme.TextMuted
import com.esp32.smartcar.presentation.utils.HapticFeedbackUtil
import com.esp32.smartcar.presentation.voice.VoiceScreen
import com.esp32.smartcar.presentation.voice.VoiceViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    driveViewModel: DriveViewModel,
    voiceViewModel: VoiceViewModel,
    sensorsViewModel: SensorsViewModel,
    settingsViewModel: SettingsViewModel,
    hapticFeedback: HapticFeedbackUtil
) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurfaceElevated,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            hapticFeedback.click()
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryCyan,
                            indicatorColor = PrimaryCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateTo = { route -> navController.navigate(route) },
                    hapticFeedback = hapticFeedback
                )
            }
            composable(Screen.Drive.route) {
                DriveScreen(
                    viewModel = driveViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    hapticFeedback = hapticFeedback
                )
            }
            composable(Screen.Voice.route) {
                VoiceScreen(
                    viewModel = voiceViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    hapticFeedback = hapticFeedback
                )
            }
            composable(Screen.Sensors.route) {
                SensorsScreen(
                    viewModel = sensorsViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    hapticFeedback = hapticFeedback
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    hapticFeedback = hapticFeedback
                )
            }
        }
    }
}
