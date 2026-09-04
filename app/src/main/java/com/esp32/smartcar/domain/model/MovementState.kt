package com.esp32.smartcar.domain.model

enum class MovementState(val displayName: String) {
    STOPPED("Stopped"),
    FORWARD("Forward"),
    BACKWARD("Backward"),
    LEFT("Turning Left"),
    RIGHT("Turning Right"),
    BLOCKED("Blocked by Obstacle"),
    EMERGENCY_STOPPED("Emergency Stopped")
}
