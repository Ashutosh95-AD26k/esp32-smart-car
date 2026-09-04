package com.esp32.smartcar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.MovementState
import com.esp32.smartcar.presentation.theme.*

@Composable
fun DirectionalDPad(
    currentMovementState: MovementState,
    isObstacleBlocked: Boolean,
    onSendCommand: (CarCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(240.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(DarkSurfaceElevated)
            .border(2.dp, DarkSurfaceBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Top: FORWARD
        DPadButton(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            icon = Icons.Default.KeyboardArrowUp,
            label = "FWD",
            isActive = currentMovementState == MovementState.FORWARD,
            isDisabled = isObstacleBlocked,
            activeColor = if (isObstacleBlocked) DangerRed else PrimaryCyan,
            onPress = { onSendCommand(CarCommand.Forward) },
            onRelease = { onSendCommand(CarCommand.Stop) }
        )

        // Bottom: BACKWARD
        DPadButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            icon = Icons.Default.KeyboardArrowDown,
            label = "REV",
            isActive = currentMovementState == MovementState.BACKWARD,
            activeColor = SecondaryBlue,
            onPress = { onSendCommand(CarCommand.Backward) },
            onRelease = { onSendCommand(CarCommand.Stop) }
        )

        // Left: LEFT
        DPadButton(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp),
            icon = Icons.Default.KeyboardArrowLeft,
            label = "LEFT",
            isActive = currentMovementState == MovementState.LEFT,
            activeColor = PrimaryCyan,
            onPress = { onSendCommand(CarCommand.Left) },
            onRelease = { onSendCommand(CarCommand.Stop) }
        )

        // Right: RIGHT
        DPadButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            icon = Icons.Default.KeyboardArrowRight,
            label = "RIGHT",
            isActive = currentMovementState == MovementState.RIGHT,
            activeColor = PrimaryCyan,
            onPress = { onSendCommand(CarCommand.Right) },
            onRelease = { onSendCommand(CarCommand.Stop) }
        )

        // Center: STOP
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(if (currentMovementState == MovementState.STOPPED) DarkSurfaceBorder else DangerRed)
                .border(2.dp, if (currentMovementState == MovementState.STOPPED) PrimaryCyan.copy(alpha = 0.5f) else Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onSendCommand(CarCommand.Stop)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "STOP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = TextPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun DPadButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    isDisabled: Boolean = false,
    activeColor: Color = PrimaryCyan,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    val bg = when {
        isDisabled -> DarkSurface.copy(alpha = 0.5f)
        isActive -> activeColor.copy(alpha = 0.25f)
        else -> DarkSurface
    }
    val tint = when {
        isDisabled -> TextMuted
        isActive -> activeColor
        else -> TextPrimary
    }

    Surface(
        modifier = modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isActive) activeColor else DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .pointerInput(isDisabled) {
                if (!isDisabled) {
                    detectTapGestures(
                        onPress = {
                            onPress()
                            tryAwaitRelease()
                            onRelease()
                        }
                    )
                }
            },
        shape = RoundedCornerShape(16.dp),
        color = bg
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = tint
                )
            )
        }
    }
}
