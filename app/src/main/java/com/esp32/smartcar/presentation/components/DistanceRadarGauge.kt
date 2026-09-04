package com.esp32.smartcar.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esp32.smartcar.domain.model.ObstacleStatus
import com.esp32.smartcar.presentation.theme.*

@Composable
fun DistanceRadarGauge(
    distanceCm: Float,
    obstacleStatus: ObstacleStatus,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val statusColor = when (obstacleStatus) {
        ObstacleStatus.SAFE -> SafeGreen
        ObstacleStatus.WARNING -> WarningAmber
        ObstacleStatus.BLOCKED -> DangerRed
        ObstacleStatus.EMERGENCY -> EmergencyRed
    }

    val animatedColor by animateColorAsState(targetValue = statusColor, label = "gaugeColor")

    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val maxRadius = this.size.width / 2f - 8.dp.toPx()

                // Background radar rings
                val ringCount = 4
                for (i in 1..ringCount) {
                    val r = (maxRadius / ringCount) * i
                    drawCircle(
                        color = DarkSurfaceBorder.copy(alpha = 0.4f),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Sweeping radar line
                val lineLength = maxRadius * 0.95f
                val sweepRad = Math.toRadians(sweepAngle.toDouble())
                val endX = (center.x + lineLength * kotlin.math.cos(sweepRad)).toFloat()
                val endY = (center.y + lineLength * kotlin.math.sin(sweepRad)).toFloat()

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, animatedColor.copy(alpha = 0.6f)),
                        start = center,
                        end = Offset(endX, endY)
                    ),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Outer progress arc based on distance (0 - 100cm)
                val clampedDistance = distanceCm.coerceIn(0f, 100f)
                val sweepProgress = (clampedDistance / 100f) * 270f

                drawArc(
                    color = animatedColor,
                    startAngle = 135f,
                    sweepAngle = sweepProgress,
                    useCenter = false,
                    topLeft = Offset(8.dp.toPx(), 8.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(this.size.width - 16.dp.toPx(), this.size.height - 16.dp.toPx()),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (distanceCm >= 400f || distanceCm <= 0f) "--" else "%.0f".format(distanceCm),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "CM",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                )
            }
        }

        // Status badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(animatedColor.copy(alpha = 0.2f))
                .border(1.dp, animatedColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = obstacleStatus.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = animatedColor
                )
            )
        }
    }
}
