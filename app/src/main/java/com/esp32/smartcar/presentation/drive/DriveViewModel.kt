package com.esp32.smartcar.presentation.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.TelemetryData
import com.esp32.smartcar.domain.repository.CarRepository
import com.esp32.smartcar.domain.usecase.SendCarCommandUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class DriveControlMode {
    JOYSTICK,
    D_PAD
}

class DriveViewModel(
    private val repository: CarRepository,
    private val sendCarCommandUseCase: SendCarCommandUseCase
) : ViewModel() {

    val bluetoothState: StateFlow<BluetoothState> = repository.bluetoothState
    val telemetryData: StateFlow<TelemetryData> = repository.telemetryData

    private val _controlMode = MutableStateFlow(DriveControlMode.JOYSTICK)
    val controlMode: StateFlow<DriveControlMode> = _controlMode.asStateFlow()

    fun setControlMode(mode: DriveControlMode) {
        _controlMode.value = mode
    }

    fun sendCommand(command: CarCommand) {
        viewModelScope.launch {
            sendCarCommandUseCase(command)
        }
    }

    fun setSpeed(speed: Int) {
        repository.setSpeed(speed)
    }

    fun emergencyStop() {
        viewModelScope.launch {
            sendCarCommandUseCase(CarCommand.EmergencyStop)
        }
    }
}
