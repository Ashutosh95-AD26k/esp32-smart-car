package com.esp32.smartcar.domain.model

/**
 * Commands sent from Android App to ESP32 over Bluetooth SPP.
 * Uses compact ASCII protocol format: F\n, B\n, L\n, R\n, S\n, X\n, V###\n, H\n
 */
sealed class CarCommand(val code: String) {
    object Forward : CarCommand("F\n")
    object Backward : CarCommand("B\n")
    object Left : CarCommand("L\n")
    object Right : CarCommand("R\n")
    object Stop : CarCommand("S\n")
    object EmergencyStop : CarCommand("X\n")
    data class SetSpeed(val speed: Int) : CarCommand("V${speed.coerceIn(0, 255)}\n")
    object Heartbeat : CarCommand("H\n")

    companion object {
        fun fromCode(raw: String): CarCommand? {
            val trimmed = raw.trim()
            return when {
                trimmed == "F" -> Forward
                trimmed == "B" -> Backward
                trimmed == "L" -> Left
                trimmed == "R" -> Right
                trimmed == "S" -> Stop
                trimmed == "X" -> EmergencyStop
                trimmed == "H" -> Heartbeat
                trimmed.startsWith("V") -> {
                    val speed = trimmed.substring(1).toIntOrNull()
                    if (speed != null) SetSpeed(speed) else null
                }
                else -> null
            }
        }
    }
}
