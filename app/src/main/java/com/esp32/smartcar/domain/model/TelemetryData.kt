package com.esp32.smartcar.domain.model

data class TelemetryData(
    val distanceCm: Float = 100f,
    val obstacleStatus: ObstacleStatus = ObstacleStatus.SAFE,
    val movementState: MovementState = MovementState.STOPPED,
    val speed: Int = 200, // 0 - 255
    val speedPercentage: Int = 78,
    val lastReceivedCommand: String = "S",
    val lastTelemetryTimestamp: Long = System.currentTimeMillis()
)
