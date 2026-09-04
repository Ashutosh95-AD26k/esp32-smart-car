package com.esp32.smartcar.presentation.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esp32.smartcar.data.bluetooth.BluetoothDeviceModel
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.presentation.components.GlassCard
import com.esp32.smartcar.presentation.components.StatusHeader
import com.esp32.smartcar.presentation.theme.*
import com.esp32.smartcar.presentation.utils.HapticFeedbackUtil
import com.esp32.smartcar.presentation.utils.PermissionHelper

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    hapticFeedback: HapticFeedbackUtil
) {
    val context = LocalContext.current
    val bluetoothState by viewModel.bluetoothState.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val warningThreshold by viewModel.warningThreshold.collectAsState()
    val blockThreshold by viewModel.blockThreshold.collectAsState()
    val emergencyThreshold by viewModel.emergencyThreshold.collectAsState()
    val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsState()

    var hasBtPermissions by remember {
        mutableStateOf(PermissionHelper.hasBluetoothPermissions(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasBtPermissions = results.values.all { it }
        if (hasBtPermissions) {
            viewModel.refreshPairedDevices()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        StatusHeader(
            bluetoothState = bluetoothState,
            onConnectClick = {
                if (hasBtPermissions) {
                    viewModel.refreshPairedDevices()
                } else {
                    permissionLauncher.launch(PermissionHelper.getRequiredBluetoothPermissions())
                }
            },
            onDisconnectClick = { viewModel.disconnect() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Paired Devices Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BLUETOOTH CLASSIC SPP DEVICES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = PrimaryCyan
                        )
                    )
                    IconButton(
                        onClick = {
                            hapticFeedback.click()
                            if (hasBtPermissions) {
                                viewModel.refreshPairedDevices()
                            } else {
                                permissionLauncher.launch(PermissionHelper.getRequiredBluetoothPermissions())
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Devices",
                            tint = PrimaryCyan
                        )
                    }
                }

                if (!hasBtPermissions) {
                    Button(
                        onClick = {
                            permissionLauncher.launch(PermissionHelper.getRequiredBluetoothPermissions())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                    ) {
                        Text("Grant Bluetooth Permissions", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                } else if (pairedDevices.isEmpty()) {
                    Text(
                        text = "No paired Bluetooth devices found.\nPlease pair 'ESP32_SMART_CAR' in Android Settings first.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pairedDevices.forEach { device ->
                            val isCurrentConnected = bluetoothState is BluetoothState.Connected &&
                                    (bluetoothState as BluetoothState.Connected).address == device.address
                            val isConnecting = bluetoothState is BluetoothState.Connecting

                            DeviceItemCard(
                                device = device,
                                isConnected = isCurrentConnected,
                                isConnecting = isConnecting,
                                onConnect = {
                                    hapticFeedback.click()
                                    viewModel.connectDevice(device.address)
                                },
                                onDisconnect = {
                                    hapticFeedback.click()
                                    viewModel.disconnect()
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Obstacle Safety Thresholds Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "OBSTACLE SAFETY THRESHOLDS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = WarningAmber
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Warning distance
                Text(
                    text = "Warning Zone: ${warningThreshold.toInt()} cm",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )
                Slider(
                    value = warningThreshold,
                    onValueChange = { viewModel.setWarningThreshold(it) },
                    valueRange = 25f..60f,
                    colors = SliderDefaults.colors(
                        thumbColor = WarningAmber,
                        activeTrackColor = WarningAmber,
                        inactiveTrackColor = DarkSurfaceBorder
                    )
                )

                // Block forward distance
                Text(
                    text = "Block Forward Movement: ${blockThreshold.toInt()} cm",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )
                Slider(
                    value = blockThreshold,
                    onValueChange = { viewModel.setBlockThreshold(it) },
                    valueRange = 15f..30f,
                    colors = SliderDefaults.colors(
                        thumbColor = DangerRed,
                        activeTrackColor = DangerRed,
                        inactiveTrackColor = DarkSurfaceBorder
                    )
                )

                // Emergency stop distance
                Text(
                    text = "Emergency Stop Cutoff: ${emergencyThreshold.toInt()} cm",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )
                Slider(
                    value = emergencyThreshold,
                    onValueChange = { viewModel.setEmergencyThreshold(it) },
                    valueRange = 5f..15f,
                    colors = SliderDefaults.colors(
                        thumbColor = EmergencyRed,
                        activeTrackColor = EmergencyRed,
                        inactiveTrackColor = DarkSurfaceBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tactile Haptics Toggle Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Haptic Vibration Feedback",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = "Vibrate on touch controls & obstacle alerts",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                Switch(
                    checked = isHapticsEnabled,
                    onCheckedChange = {
                        viewModel.toggleHaptics(it)
                        hapticFeedback.isHapticsEnabled = it
                        if (it) hapticFeedback.click()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryCyan,
                        checkedTrackColor = PrimaryCyan.copy(alpha = 0.5f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hardware & Architecture Reference Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ESP32 PINOUT REFERENCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = SecondaryBlue
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                PinoutRow("Left Motors PWM (ENA)", "GPIO 22")
                PinoutRow("Left Motors IN1 / IN2", "GPIO 16 / GPIO 17")
                PinoutRow("Right Motors PWM (ENB)", "GPIO 23")
                PinoutRow("Right Motors IN3 / IN4", "GPIO 18 / GPIO 19")
                PinoutRow("HC-SR04 TRIG", "GPIO 32")
                PinoutRow("HC-SR04 ECHO (Level-Shifted)", "GPIO 33 (1k/2k divider)")
            }
        }
    }
}

@Composable
private fun DeviceItemCard(
    device: BluetoothDeviceModel,
    isConnected: Boolean,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isTarget = device.name.contains("ESP32_SMART_CAR", ignoreCase = true)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (isConnected) PrimaryCyan.copy(alpha = 0.12f) else DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isConnected) PrimaryCyan else if (isTarget) PrimaryCyan.copy(alpha = 0.4f) else DarkSurfaceBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) SafeGreen.copy(alpha = 0.2f) else DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (isConnected) SafeGreen else if (isTarget) PrimaryCyan else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) PrimaryCyan else TextPrimary
                        )
                    )
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }
            }

            if (isConnected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed)
                ) {
                    Text("Disconnect", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            } else {
                Button(
                    onClick = onConnect,
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                ) {
                    Text("Connect", color = DarkBackground, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun PinoutRow(label: String, pin: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
        Text(text = pin, style = MaterialTheme.typography.bodySmall.copy(color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp))
    }
}
