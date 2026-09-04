package com.esp32.smartcar.data.repository

import android.util.Log
import com.esp32.smartcar.data.bluetooth.BluetoothDeviceModel
import com.esp32.smartcar.data.bluetooth.BluetoothSPPManager
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.MovementState
import com.esp32.smartcar.domain.model.ObstacleStatus
import com.esp32.smartcar.domain.model.TelemetryData
import com.esp32.smartcar.domain.repository.CarRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CarRepositoryImpl(
    private val bluetoothManager: BluetoothSPPManager
) : CarRepository {

    companion object {
        private const val TAG = "CarRepositoryImpl"
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val bluetoothState: StateFlow<BluetoothState> = bluetoothManager.connectionState
    override val pairedDevices: StateFlow<List<BluetoothDeviceModel>> = bluetoothManager.pairedDevices

    private val _telemetryData = MutableStateFlow(TelemetryData())
    override val telemetryData: StateFlow<TelemetryData> = _telemetryData.asStateFlow()

    private var warningThresholdCm: Float = 30f
    private var blockThresholdCm: Float = 20f
    private var emergencyThresholdCm: Float = 10f

    init {
        scope.launch {
            bluetoothManager.incomingTelemetry.collect { rawLine ->
                parseIncomingTelemetry(rawLine)
            }
        }
    }

    override fun refreshPairedDevices() {
        bluetoothManager.refreshPairedDevices()
    }

    override suspend fun connect(deviceAddress: String): Boolean {
        val success = bluetoothManager.connect(deviceAddress)
        if (success) {
            // Send default speed upon initial connection
            sendCommand(CarCommand.SetSpeed(_telemetryData.value.speed))
        }
        return success
    }

    override suspend fun disconnect() {
        bluetoothManager.disconnect()
        _telemetryData.value = _telemetryData.value.copy(
            movementState = MovementState.STOPPED
        )
    }

    override suspend fun sendCommand(command: CarCommand): Boolean {
        val sent = bluetoothManager.sendRaw(command.code)
        if (sent) {
            _telemetryData.value = _telemetryData.value.copy(
                lastReceivedCommand = command.code.trim()
            )
        }
        return sent
    }

    override fun setSpeed(speed: Int) {
        val clamped = speed.coerceIn(0, 255)
        val percentage = ((clamped / 255f) * 100).toInt()
        _telemetryData.value = _telemetryData.value.copy(
            speed = clamped,
            speedPercentage = percentage
        )
        scope.launch {
            sendCommand(CarCommand.SetSpeed(clamped))
        }
    }

    override fun setObstacleThresholds(warningCm: Float, blockCm: Float, emergencyCm: Float) {
        warningThresholdCm = warningCm
        blockThresholdCm = blockCm
        emergencyThresholdCm = emergencyCm
        // Re-evaluate current distance with new thresholds
        val currentDist = _telemetryData.value.distanceCm
        val newStatus = ObstacleStatus.evaluate(currentDist, warningThresholdCm, blockThresholdCm, emergencyThresholdCm)
        _telemetryData.value = _telemetryData.value.copy(obstacleStatus = newStatus)
    }

    override fun updateLocalMovementState(state: MovementState) {
        _telemetryData.value = _telemetryData.value.copy(movementState = state)
    }

    private fun parseIncomingTelemetry(line: String) {
        try {
            when {
                // Distance telemetry: D### (e.g. D35 or D12.5)
                line.startsWith("D") -> {
                    val rawNum = line.substring(1).trim()
                    val dist = rawNum.toFloatOrNull()
                    if (dist != null) {
                        val status = ObstacleStatus.evaluate(dist, warningThresholdCm, blockThresholdCm, emergencyThresholdCm)
                        val currentMove = _telemetryData.value.movementState
                        val updatedMove = if (status == ObstacleStatus.BLOCKED && currentMove == MovementState.FORWARD) {
                            MovementState.BLOCKED
                        } else if (status == ObstacleStatus.EMERGENCY && currentMove != MovementState.STOPPED) {
                            MovementState.EMERGENCY_STOPPED
                        } else {
                            currentMove
                        }

                        _telemetryData.value = _telemetryData.value.copy(
                            distanceCm = dist,
                            obstacleStatus = status,
                            movementState = updatedMove,
                            lastTelemetryTimestamp = System.currentTimeMillis()
                        )
                    }
                }
                // Movement state telemetry: S:STATE (e.g. S:FWD, S:STOP)
                line.startsWith("S:") -> {
                    val stateCode = line.substring(2).trim()
                    val parsedState = when (stateCode) {
                        "FWD" -> MovementState.FORWARD
                        "REV" -> MovementState.BACKWARD
                        "LEFT" -> MovementState.LEFT
                        "RIGHT" -> MovementState.RIGHT
                        "BLOCKED" -> MovementState.BLOCKED
                        "EMERGENCY" -> MovementState.EMERGENCY_STOPPED
                        else -> MovementState.STOPPED
                    }
                    _telemetryData.value = _telemetryData.value.copy(movementState = parsedState)
                }
                // Heartbeat
                line == "H" -> {
                    _telemetryData.value = _telemetryData.value.copy(
                        lastTelemetryTimestamp = System.currentTimeMillis()
                    )
                }
                else -> {
                    Log.d(TAG, "Unhandled telemetry line: $line")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing telemetry line '$line': ${e.message}")
        }
    }
}
