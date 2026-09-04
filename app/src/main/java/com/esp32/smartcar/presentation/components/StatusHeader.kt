package com.esp32.smartcar.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.presentation.theme.*

@Composable
fun StatusHeader(
    bluetoothState: BluetoothState,
    onConnectClick: () -> Unit = {},
    onDisconnectClick: () -> Unit = {}
) {
    val (statusText, statusColor, icon) = when (bluetoothState) {
        is BluetoothState.Connected -> Triple("CONNECTED", SafeGreen, Icons.Default.BluetoothConnected)
        is BluetoothState.Connecting -> Triple("CONNECTING...", WarningAmber, Icons.Default.Bluetooth)
        is BluetoothState.Reconnecting -> Triple("RECONNECTING...", WarningAmber, Icons.Default.Refresh)
        is BluetoothState.ConnectionFailed -> Triple("FAILED", DangerRed, Icons.Default.BluetoothDisabled)
        is BluetoothState.Disconnected -> Triple("DISCONNECTED", TextMuted, Icons.Default.BluetoothDisabled)
        is BluetoothState.Error -> Triple("ERROR", DangerRed, Icons.Default.BluetoothDisabled)
    }

    val animatedIndicatorColor by animateColorAsState(targetValue = statusColor, label = "statusColor")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ESP32 SMART CAR",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    color = PrimaryCyan
                )
            )
            if (bluetoothState is BluetoothState.Connected) {
                Text(
                    text = bluetoothState.deviceName,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, animatedIndicatorColor.copy(alpha = 0.5f)),
            onClick = {
                if (bluetoothState is BluetoothState.Connected) {
                    onDisconnectClick()
                } else {
                    onConnectClick()
                }
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(animatedIndicatorColor)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = animatedIndicatorColor
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = statusText,
                    tint = animatedIndicatorColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
