package com.esp32.smartcar.domain.usecase

import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.MovementState
import com.esp32.smartcar.domain.model.ObstacleStatus
import com.esp32.smartcar.domain.repository.CarRepository

class SendCarCommandUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(command: CarCommand): Boolean {
        val telemetry = repository.telemetryData.value

        // Safety enforcement: If obstacle is blocked or emergency, prevent forward movement
        if (command is CarCommand.Forward) {
            if (telemetry.obstacleStatus == ObstacleStatus.BLOCKED ||
                telemetry.obstacleStatus == ObstacleStatus.EMERGENCY) {
                // Reject forward command for safety
                repository.updateLocalMovementState(MovementState.BLOCKED)
                return false
            }
        }

        // Update local expected movement state
        when (command) {
            is CarCommand.Forward -> repository.updateLocalMovementState(MovementState.FORWARD)
            is CarCommand.Backward -> repository.updateLocalMovementState(MovementState.BACKWARD)
            is CarCommand.Left -> repository.updateLocalMovementState(MovementState.LEFT)
            is CarCommand.Right -> repository.updateLocalMovementState(MovementState.RIGHT)
            is CarCommand.Stop -> repository.updateLocalMovementState(MovementState.STOPPED)
            is CarCommand.EmergencyStop -> repository.updateLocalMovementState(MovementState.EMERGENCY_STOPPED)
            else -> {}
        }

        return repository.sendCommand(command)
    }
}
