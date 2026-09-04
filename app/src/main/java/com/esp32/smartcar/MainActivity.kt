package com.esp32.smartcar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.esp32.smartcar.presentation.drive.DriveViewModel
import com.esp32.smartcar.presentation.home.HomeViewModel
import com.esp32.smartcar.presentation.navigation.AppNavigation
import com.esp32.smartcar.presentation.sensors.SensorsViewModel
import com.esp32.smartcar.presentation.settings.SettingsViewModel
import com.esp32.smartcar.presentation.theme.ESP32SmartCarTheme
import com.esp32.smartcar.presentation.utils.HapticFeedbackUtil
import com.esp32.smartcar.presentation.voice.VoiceViewModel

class MainActivity : ComponentActivity() {

    private val app by lazy { application as SmartCarApp }
    private lateinit var hapticFeedback: HapticFeedbackUtil

    private val homeViewModel: HomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    repository = app.carRepository,
                    connectDeviceUseCase = app.connectDeviceUseCase,
                    sendCarCommandUseCase = app.sendCarCommandUseCase
                ) as T
            }
        }
    }

    private val driveViewModel: DriveViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DriveViewModel(
                    repository = app.carRepository,
                    sendCarCommandUseCase = app.sendCarCommandUseCase
                ) as T
            }
        }
    }

    private val voiceViewModel: VoiceViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VoiceViewModel(
                    voiceRecognitionManager = app.voiceRecognitionManager,
                    repository = app.carRepository,
                    sendCarCommandUseCase = app.sendCarCommandUseCase
                ) as T
            }
        }
    }

    private val sensorsViewModel: SensorsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SensorsViewModel(
                    repository = app.carRepository,
                    sendCarCommandUseCase = app.sendCarCommandUseCase
                ) as T
            }
        }
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    repository = app.carRepository,
                    connectDeviceUseCase = app.connectDeviceUseCase
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hapticFeedback = HapticFeedbackUtil(this)

        setContent {
            ESP32SmartCarTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    driveViewModel = driveViewModel,
                    voiceViewModel = voiceViewModel,
                    sensorsViewModel = sensorsViewModel,
                    settingsViewModel = settingsViewModel,
                    hapticFeedback = hapticFeedback
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Stop movement fail-safe on activity stop
        driveViewModel.sendCommand(com.esp32.smartcar.domain.model.CarCommand.Stop)
    }
}
