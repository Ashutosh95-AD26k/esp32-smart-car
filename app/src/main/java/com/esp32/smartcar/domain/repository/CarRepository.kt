package com.esp32.smartcar.domain.repository

import com.esp32.smartcar.data.bluetooth.BluetoothDeviceModel
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.MovementState
import com.esp32.smartcar.domain.model.TelemetryData
import kotlinx.coroutines.flow.StateFlow

interface CarRepository {
    val bluetoothState: StateFlow<BluetoothState>
    val telemetryData: StateFlow<TelemetryData>
    val pairedDevices: StateFlow<List<BluetoothDeviceModel>>

    fun refreshPairedDevices()
    suspend fun connect(deviceAddress: String): Boolean
    suspend fun disconnect()
    suspend fun sendCommand(command: CarCommand): Boolean
    fun setSpeed(speed: Int)
    fun setObstacleThresholds(warningCm: Float, blockCm: Float, emergencyCm: Float)
    fun updateLocalMovementState(state: MovementState)
}
