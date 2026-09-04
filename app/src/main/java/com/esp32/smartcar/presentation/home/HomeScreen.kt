package com.esp32.smartcar.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.domain.model.ObstacleStatus
import com.esp32.smartcar.presentation.components.DistanceRadarGauge
import com.esp32.smartcar.presentation.components.EmergencyStopButton
import com.esp32.smartcar.presentation.components.GlassCard
import com.esp32.smartcar.presentation.components.StatusHeader
import com.esp32.smartcar.presentation.navigation.Screen
import com.esp32.smartcar.presentation.theme.*
import com.esp32.smartcar.presentation.utils.HapticFeedbackUtil

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateTo: (String) -> Unit,
    hapticFeedback: HapticFeedbackUtil
) {
    val bluetoothState by viewModel.bluetoothState.collectAsState()
    val telemetry by viewModel.telemetryData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        StatusHeader(
            bluetoothState = bluetoothState,
            onConnectClick = { onNavigateTo(Screen.Settings.route) },
            onDisconnectClick = { viewModel.disconnect() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main HUD Overview
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE TELEMETRY HUD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = PrimaryCyan
                        )
                    )
                    Text(
                        text = "CMD: ${telemetry.lastReceivedCommand}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                DistanceRadarGauge(
                    distanceCm = telemetry.distanceCm,
                    obstacleStatus = telemetry.obstacleStatus,
                    size = 140.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HudMetricItem(
                        label = "STATUS",
                        value = telemetry.movementState.displayName,
                        valueColor = if (telemetry.obstacleStatus == ObstacleStatus.BLOCKED) DangerRed else TextPrimary
                    )
                    HudMetricItem(
                        label = "SPEED",
                        value = "${telemetry.speedPercentage}%",
                        valueColor = PrimaryCyan
                    )
                    HudMetricItem(
                        label = "OBSTACLE",
                        value = telemetry.obstacleStatus.label,
                        valueColor = when (telemetry.obstacleStatus) {
                            ObstacleStatus.SAFE -> SafeGreen
                            ObstacleStatus.WARNING -> WarningAmber
                            else -> DangerRed
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation Grid
        Text(
            text = "CONTROL MODULES",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = TextSecondary
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModuleCard(
                title = "DRIVE",
                subtitle = "Joystick & D-Pad",
                icon = Icons.Default.SportsEsports,
                iconColor = PrimaryCyan,
                modifier = Modifier.weight(1f),
                onClick = {
                    hapticFeedback.click()
                    onNavigateTo(Screen.Drive.route)
                }
            )
            ModuleCard(
                title = "VOICE",
                subtitle = "EN & HI Commands",
                icon = Icons.Default.Mic,
                iconColor = AccentPurple,
                modifier = Modifier.weight(1f),
                onClick = {
                    hapticFeedback.click()
                    onNavigateTo(Screen.Voice.route)
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModuleCard(
                title = "SENSORS",
                subtitle = "Distance & Radar",
                icon = Icons.Default.Sensors,
                iconColor = SecondaryBlue,
                modifier = Modifier.weight(1f),
                onClick = {
                    hapticFeedback.click()
                    onNavigateTo(Screen.Sensors.route)
                }
            )
            ModuleCard(
                title = "SETTINGS",
                subtitle = "Bluetooth & Safety",
                icon = Icons.Default.Settings,
                iconColor = TextSecondary,
                modifier = Modifier.weight(1f),
                onClick = {
                    hapticFeedback.click()
                    onNavigateTo(Screen.Settings.route)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Emergency Stop Button
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            EmergencyStopButton(
                onEmergencyStop = {
                    hapticFeedback.emergency()
                    viewModel.emergencyStop()
                }
            )
        }
    }
}

@Composable
private fun HudMetricItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        )
    }
}

@Composable
private fun ModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}
