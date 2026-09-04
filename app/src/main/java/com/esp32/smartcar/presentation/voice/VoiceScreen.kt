package com.esp32.smartcar.presentation.voice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esp32.smartcar.domain.model.VoiceCommandResult
import com.esp32.smartcar.presentation.components.EmergencyStopButton
import com.esp32.smartcar.presentation.components.GlassCard
import com.esp32.smartcar.presentation.components.StatusHeader
import com.esp32.smartcar.presentation.theme.*
import com.esp32.smartcar.presentation.utils.HapticFeedbackUtil
import com.esp32.smartcar.presentation.utils.PermissionHelper

@Composable
fun VoiceScreen(
    viewModel: VoiceViewModel,
    onNavigateToSettings: () -> Unit,
    hapticFeedback: HapticFeedbackUtil
) {
    val context = LocalContext.current
    val bluetoothState by viewModel.bluetoothState.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(PermissionHelper.hasRecordAudioPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            viewModel.startListening()
        }
    }

    val isListening = voiceState is VoiceCommandResult.Listening

    // Pulsing animation for microphone button
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

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

        // Language Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    FilterChip(
                        selected = selectedLanguage == "en-US",
                        onClick = {
                            hapticFeedback.click()
                            viewModel.setLanguage("en-US")
                        },
                        label = { Text("English (EN)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = selectedLanguage == "hi-IN",
                        onClick = {
                            hapticFeedback.click()
                            viewModel.setLanguage("hi-IN")
                        },
                        label = { Text("हिंदी (Hindi)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = TextSecondary
                        ),
                        border = null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Large Animated Microphone Control
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple background ring when listening
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(micScale)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.2f))
                )
            }

            val micBgColor by animateColorAsState(
                targetValue = when (voiceState) {
                    is VoiceCommandResult.Listening -> AccentPurple
                    is VoiceCommandResult.Processing -> WarningAmber
                    is VoiceCommandResult.Success -> SafeGreen
                    is VoiceCommandResult.Error -> DangerRed
                    is VoiceCommandResult.Unrecognized -> WarningAmber
                    is VoiceCommandResult.Idle -> DarkSurfaceElevated
                },
                label = "micBg"
            )

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(micBgColor, DarkSurface)
                        )
                    )
                    .border(3.dp, micBgColor, CircleShape)
                    .clickable {
                        hapticFeedback.click()
                        if (hasAudioPermission) {
                            if (isListening) {
                                viewModel.stopListening()
                            } else {
                                viewModel.startListening()
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Live Voice Status Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val (statusTitle, statusColor) = when (voiceState) {
                    is VoiceCommandResult.Idle -> "TAP MIC TO SPEAK" to TextSecondary
                    is VoiceCommandResult.Listening -> "LISTENING... SPEAK NOW" to AccentPurple
                    is VoiceCommandResult.Processing -> "PROCESSING AUDIO..." to WarningAmber
                    is VoiceCommandResult.Success -> "COMMAND ACCEPTED" to SafeGreen
                    is VoiceCommandResult.Unrecognized -> "COMMAND NOT RECOGNIZED" to WarningAmber
                    is VoiceCommandResult.Error -> "ERROR" to DangerRed
                }

                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                when (val state = voiceState) {
                    is VoiceCommandResult.Success -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You said: \"${state.spokenText}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Action: ${state.actionName}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = SafeGreen
                                )
                            )
                        }
                    }
                    is VoiceCommandResult.Unrecognized -> {
                        Text(
                            text = "Heard: \"${state.spokenText}\"",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                        Text(
                            text = "Try saying \"Forward\", \"Aage Jao\", or \"Stop\"",
                            style = MaterialTheme.typography.bodySmall.copy(color = WarningAmber)
                        )
                    }
                    is VoiceCommandResult.Error -> {
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodySmall.copy(color = DangerRed)
                        )
                    }
                    else -> {
                        Text(
                            text = if (selectedLanguage == "hi-IN") "हिंदी आदेश: आगे जाओ, पीछे जाओ, बाएं, दाएं, रोको" else "English commands: Forward, Backward, Left, Right, Stop",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Supported Voice Commands Cheatsheet
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SUPPORTED VOICE COMMANDS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = PrimaryCyan
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                CommandCheatRow(action = "FORWARD", english = "Forward, Go Forward, Move Ahead", hindi = "आगे जाओ, आगे चलो, सीधे")
                CommandCheatRow(action = "BACKWARD", english = "Back, Go Backward, Reverse", hindi = "पीछे जाओ, पीछे चलो, रिवर्स")
                CommandCheatRow(action = "TURN LEFT", english = "Left, Turn Left, Go Left", hindi = "बाएं मुड़ो, बाएं जाओ")
                CommandCheatRow(action = "TURN RIGHT", english = "Right, Turn Right, Go Right", hindi = "दाएं मुड़ो, दाएं जाओ")
                CommandCheatRow(action = "STOP", english = "Stop, Halt, Freeze, Brake", hindi = "रुको, गाड़ी रोको, ठहर जाओ")
                CommandCheatRow(action = "EMERGENCY", english = "Emergency Stop, Danger", hindi = "आपातकाल, तुरंत रोको")
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
private fun CommandCheatRow(
    action: String,
    english: String,
    hindi: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = action,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryCyan
            ),
            modifier = Modifier.width(85.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = english,
                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 11.sp)
            )
            Text(
                text = hindi,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp)
            )
        }
    }
    HorizontalDivider(color = DarkSurfaceBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
}
