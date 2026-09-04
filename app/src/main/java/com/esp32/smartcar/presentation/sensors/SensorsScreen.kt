package com.esp32.smartcar.presentation.sensors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esp32.smartcar.domain.model.MovementState
import com.esp32.smartcar.domain.model.ObstacleStatus
import com.esp32.smartcar.presentation.components.DistanceRadarGauge
import com.esp32.smartcar.presentation.components.EmergencyStopButton
import com.esp32.smartcar.presentation.components.GlassCard
import com.esp32.smartcar.presentation.components.StatusHeader
import com.esp32.smartcar.presentation.theme.*
import com.esp32.smartcar.presentation.utils.HapticFeedbackUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SensorsScreen(
    viewModel: SensorsViewModel,
    onNavigateToSettings: () -> Unit,
    hapticFeedback: HapticFeedbackUtil
) {
    val bluetoothState by viewModel.bluetoothState.collectAsState()
    val telemetry by viewModel.telemetryData.collectAsState()

    val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val lastPingTime = timeFormatter.format(Date(telemetry.lastTelemetryTimestamp))

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

        Spacer(modifier = Modifier.height(8.dp))

        // Distance Radar Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ULTRASONIC DISTANCE SENSOR (HC-SR04)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = PrimaryCyan
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                DistanceRadarGauge(
                    distanceCm = telemetry.distanceCm,
                    obstacleStatus = telemetry.obstacleStatus,
                    size = 170.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Thresholds Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SensorZonePill(label = "SAFE (>30cm)", color = SafeGreen, isActive = telemetry.obstacleStatus == ObstacleStatus.SAFE)
                    SensorZonePill(label = "WARN (20-30)", color = WarningAmber, isActive = telemetry.obstacleStatus == ObstacleStatus.WARNING)
                    SensorZonePill(label = "BLOCK (<=20)", color = DangerRed, isActive = telemetry.obstacleStatus == ObstacleStatus.BLOCKED)
                    SensorZonePill(label = "EMERG (<=10)", color = EmergencyRed, isActive = telemetry.obstacleStatus == ObstacleStatus.EMERGENCY)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Motor Subsystem Telemetry Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "MOTOR DRIVER TELEMETRY (L298N DUAL H-BRIDGE)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = SecondaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MotorSideCard(
                        side = "LEFT MOTORS (CH A)",
                        pinInfo = "ENA: 22, IN1: 16, IN2: 17",
                        state = when (telemetry.movementState) {
                            MovementState.FORWARD -> "FORWARD"
                            MovementState.BACKWARD -> "REVERSE"
                            MovementState.LEFT -> "REVERSE (PIVOT)"
                            MovementState.RIGHT -> "FORWARD"
                            else -> "STOPPED"
                        },
                        pwm = if (telemetry.movementState == MovementState.STOPPED) 0 else telemetry.speed,
                        modifier = Modifier.weight(1f)
                    )

                    MotorSideCard(
                        side = "RIGHT MOTORS (CH B)",
                        pinInfo = "ENB: 23, IN3: 18, IN4: 19",
                        state = when (telemetry.movementState) {
                            MovementState.FORWARD -> "FORWARD"
                            MovementState.BACKWARD -> "REVERSE"
                            MovementState.LEFT -> "FORWARD"
                            MovementState.RIGHT -> "REVERSE (PIVOT)"
                            else -> "STOPPED"
                        },
                        pwm = if (telemetry.movementState == MovementState.STOPPED) 0 else telemetry.speed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Communication Telemetry Log Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "COMMUNICATION PROTOCOL TELEMETRY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                TelemetryRow(label = "Transport Protocol", value = "Bluetooth Classic SPP (RFCOMM)")
                TelemetryRow(label = "Last Command Ack", value = "${telemetry.lastReceivedCommand}\\n")
                TelemetryRow(label = "Last Telemetry Timestamp", value = lastPingTime)
                TelemetryRow(label = "Watchdog Timeout", value = "1000 ms (Dead-man safety)")
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
private fun SensorZonePill(
    label: String,
    color: Color,
    isActive: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) color.copy(alpha = 0.25f) else DarkSurfaceElevated)
            .border(1.dp, if (isActive) color else Color.Transparent, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) color else TextMuted
            )
        )
    }
}

@Composable
private fun MotorSideCard(
    side: String,
    pinInfo: String,
    state: String,
    pwm: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = side,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = PrimaryCyan
                )
            )
            Text(
                text = pinInfo,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 8.sp,
                    color = TextMuted
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = state,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = if (state == "STOPPED") TextSecondary else SafeGreen
                )
            )
            Text(
                text = "PWM: $pwm/255",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = SecondaryBlue
                )
            )
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp))
    }
    HorizontalDivider(color = DarkSurfaceBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
}
