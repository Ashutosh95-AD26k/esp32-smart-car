package com.esp32.smartcar.presentation.drive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esp32.smartcar.domain.model.MovementState
import com.esp32.smartcar.domain.model.ObstacleStatus
import com.esp32.smartcar.presentation.components.*
import com.esp32.smartcar.presentation.navigation.Screen
import com.esp32.smartcar.presentation.theme.*
import com.esp32.smartcar.presentation.utils.HapticFeedbackUtil

@Composable
fun DriveScreen(
    viewModel: DriveViewModel,
    onNavigateToSettings: () -> Unit,
    hapticFeedback: HapticFeedbackUtil
) {
    val bluetoothState by viewModel.bluetoothState.collectAsState()
    val telemetry by viewModel.telemetryData.collectAsState()
    val controlMode by viewModel.controlMode.collectAsState()

    val isBlocked = telemetry.obstacleStatus == ObstacleStatus.BLOCKED ||
            telemetry.obstacleStatus == ObstacleStatus.EMERGENCY

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        StatusHeader(
            bluetoothState = bluetoothState,
            onConnectClick = onNavigateToSettings,
            onDisconnectClick = {}
        )

        // Obstacle Alert Banner
        AnimatedVisibility(
            visible = isBlocked,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmergencyRed.copy(alpha = 0.2f))
                    .border(1.dp, EmergencyRed, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Obstacle Alert",
                        tint = EmergencyRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "OBSTACLE DETECTED: ${"%.0f".format(telemetry.distanceCm)} CM - FORWARD LOCKED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EmergencyRed,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }
        }

        // Live Movement & Speed Status Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "STATE:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                    Text(
                        text = telemetry.movementState.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.movementState == MovementState.STOPPED) TextSecondary else PrimaryCyan
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "DIST:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                    Text(
                        text = if (telemetry.distanceCm >= 400f || telemetry.distanceCm <= 0f) "--" else "${"%.0f".format(telemetry.distanceCm)} cm",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (telemetry.obstacleStatus) {
                                ObstacleStatus.SAFE -> SafeGreen
                                ObstacleStatus.WARNING -> WarningAmber
                                else -> DangerRed
                            }
                        )
                    )
                }
            }
        }

        // Control Mode Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    FilterChip(
                        selected = controlMode == DriveControlMode.JOYSTICK,
                        onClick = {
                            hapticFeedback.click()
                            viewModel.setControlMode(DriveControlMode.JOYSTICK)
                        },
                        label = { Text("Virtual Joystick") },
                        leadingIcon = {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryCyan,
                            selectedLabelColor = DarkBackground,
                            selectedLeadingIconColor = DarkBackground,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary,
                            iconColor = TextSecondary
                        ),
                        border = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = controlMode == DriveControlMode.D_PAD,
                        onClick = {
                            hapticFeedback.click()
                            viewModel.setControlMode(DriveControlMode.D_PAD)
                        },
                        label = { Text("D-Pad") },
                        leadingIcon = {
                            Icon(Icons.Default.Gamepad, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryCyan,
                            selectedLabelColor = DarkBackground,
                            selectedLeadingIconColor = DarkBackground,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary,
                            iconColor = TextSecondary
                        ),
                        border = null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Control Area (Centered)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (controlMode == DriveControlMode.JOYSTICK) {
                VirtualJoystick(
                    size = 220.dp,
                    isObstacleBlocked = isBlocked,
                    onDirectionChanged = { cmd ->
                        hapticFeedback.click()
                        viewModel.sendCommand(cmd)
                    }
                )
            } else {
                DirectionalDPad(
                    currentMovementState = telemetry.movementState,
                    isObstacleBlocked = isBlocked,
                    onSendCommand = { cmd ->
                        hapticFeedback.click()
                        viewModel.sendCommand(cmd)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Speed Controller Slider & Quick Presets
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
                        text = "MOTOR SPEED (PWM)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    )
                    Text(
                        text = "${telemetry.speedPercentage}% (${telemetry.speed} PWM)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCyan
                        )
                    )
                }

                Slider(
                    value = telemetry.speed.toFloat(),
                    onValueChange = { newVal ->
                        viewModel.setSpeed(newVal.toInt())
                    },
                    valueRange = 80f..255f, // Realistic minimum torque PWM to 255
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryCyan,
                        activeTrackColor = PrimaryCyan,
                        inactiveTrackColor = DarkSurfaceBorder
                    )
                )

                // Speed Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeedPresetButton(label = "SLOW (40%)", targetSpeed = 102, currentSpeed = telemetry.speed, modifier = Modifier.weight(1f)) {
                        hapticFeedback.click()
                        viewModel.setSpeed(102)
                    }
                    SpeedPresetButton(label = "CRUISE (70%)", targetSpeed = 178, currentSpeed = telemetry.speed, modifier = Modifier.weight(1f)) {
                        hapticFeedback.click()
                        viewModel.setSpeed(178)
                    }
                    SpeedPresetButton(label = "TURBO (100%)", targetSpeed = 255, currentSpeed = telemetry.speed, modifier = Modifier.weight(1f)) {
                        hapticFeedback.click()
                        viewModel.setSpeed(255)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
private fun SpeedPresetButton(
    label: String,
    targetSpeed: Int,
    currentSpeed: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isSelected = kotlin.math.abs(currentSpeed - targetSpeed) < 15
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) PrimaryCyan.copy(alpha = 0.15f) else Color.Transparent,
            contentColor = if (isSelected) PrimaryCyan else TextSecondary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryCyan else DarkSurfaceBorder),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
