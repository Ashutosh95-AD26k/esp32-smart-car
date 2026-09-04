package com.esp32.smartcar.domain.model

enum class ObstacleStatus(val label: String) {
    SAFE("SAFE"),
    WARNING("WARNING"),
    BLOCKED("BLOCKED"),
    EMERGENCY("EMERGENCY STOP");

    companion object {
        fun evaluate(distanceCm: Float, warningThreshold: Float = 30f, blockThreshold: Float = 20f, emergencyThreshold: Float = 10f): ObstacleStatus {
            return when {
                distanceCm <= 0f -> SAFE // Out of range or invalid reading
                distanceCm <= emergencyThreshold -> EMERGENCY
                distanceCm <= blockThreshold -> BLOCKED
                distanceCm <= warningThreshold -> WARNING
                else -> SAFE
            }
        }
    }
}
