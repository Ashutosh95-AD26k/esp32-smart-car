package com.esp32.smartcar.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.presentation.theme.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isObstacleBlocked: Boolean = false,
    onDirectionChanged: (CarCommand) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    var currentCommand by remember { mutableStateOf<CarCommand>(CarCommand.Stop) }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(isObstacleBlocked) {
                val radius = (size.toPx() / 2f)
                val maxThumbRadius = radius * 0.65f
                val deadZone = radius * 0.2f

                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(radius, radius)
                        val dragVector = offset - center
                        val dist = dragVector.getDistance()
                        val clampedDist = dist.coerceAtMost(maxThumbRadius)
                        val angle = atan2(dragVector.y, dragVector.x)

                        thumbOffset = Offset(
                            x = cos(angle) * clampedDist,
                            y = sin(angle) * clampedDist
                        )
                        val cmd = calculateCommand(thumbOffset, deadZone, isObstacleBlocked)
                        if (cmd != currentCommand) {
                            currentCommand = cmd
                            onDirectionChanged(cmd)
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(radius, radius)
                        val dragVector = change.position - center
                        val dist = dragVector.getDistance()
                        val clampedDist = dist.coerceAtMost(maxThumbRadius)
                        val angle = atan2(dragVector.y, dragVector.x)

                        thumbOffset = Offset(
                            x = cos(angle) * clampedDist,
                            y = sin(angle) * clampedDist
                        )
                        val cmd = calculateCommand(thumbOffset, deadZone, isObstacleBlocked)
                        if (cmd != currentCommand) {
                            currentCommand = cmd
                            onDirectionChanged(cmd)
                        }
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        currentCommand = CarCommand.Stop
                        onDirectionChanged(CarCommand.Stop)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        currentCommand = CarCommand.Stop
                        onDirectionChanged(CarCommand.Stop)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = this.size.width / 2f
            val knobRadius = outerRadius * 0.32f

            // Outer ring base
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(DarkSurfaceElevated, DarkBackground),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = DarkSurfaceBorder,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // Crosshair guide lines
            drawLine(
                color = DarkSurfaceBorder.copy(alpha = 0.5f),
                start = Offset(center.x - outerRadius * 0.8f, center.y),
                end = Offset(center.x + outerRadius * 0.8f, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = DarkSurfaceBorder.copy(alpha = 0.5f),
                start = Offset(center.x, center.y - outerRadius * 0.8f),
                end = Offset(center.x, center.y + outerRadius * 0.8f),
                strokeWidth = 1.dp.toPx()
            )

            // Inner boundary guide ring
            drawCircle(
                color = PrimaryCyan.copy(alpha = 0.15f),
                radius = outerRadius * 0.65f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Dynamic Knob
            val knobCenter = center + thumbOffset
            val knobColor = when {
                isObstacleBlocked && currentCommand is CarCommand.Forward -> DangerRed
                thumbOffset != Offset.Zero -> PrimaryCyan
                else -> TextSecondary
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(knobColor.copy(alpha = 0.9f), DarkSurfaceElevated),
                    center = knobCenter,
                    radius = knobRadius
                ),
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = knobColor,
                radius = knobRadius,
                center = knobCenter,
                style = Stroke(width = 2.dp.toPx())
            )
            // Central knob dot
            drawCircle(
                color = Color.White,
                radius = knobRadius * 0.25f,
                center = knobCenter
            )
        }
    }
}

private fun calculateCommand(offset: Offset, deadZone: Float, isObstacleBlocked: Boolean): CarCommand {
    val distance = sqrt(offset.x * offset.x + offset.y * offset.y)
    if (distance < deadZone) {
        return CarCommand.Stop
    }

    // Angle in degrees: 0 = Right, 90 = Down, 180 = Left, -90 = Up
    val angleRad = atan2(offset.y, offset.x)
    val angleDeg = Math.toDegrees(angleRad.toDouble())

    return when {
        // UP: between -135 and -45
        angleDeg in -135.0..-45.0 -> {
            if (isObstacleBlocked) CarCommand.Stop else CarCommand.Forward
        }
        // DOWN: between 45 and 135
        angleDeg in 45.0..135.0 -> CarCommand.Backward
        // RIGHT: between -45 and 45
        angleDeg in -45.0..45.0 -> CarCommand.Right
        // LEFT: else
        else -> CarCommand.Left
    }
}
