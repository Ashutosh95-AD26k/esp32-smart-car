package com.esp32.smartcar.presentation.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.TelemetryData
import com.esp32.smartcar.domain.repository.CarRepository
import com.esp32.smartcar.domain.usecase.SendCarCommandUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SensorsViewModel(
    private val repository: CarRepository,
    private val sendCarCommandUseCase: SendCarCommandUseCase
) : ViewModel() {

    val bluetoothState: StateFlow<BluetoothState> = repository.bluetoothState
    val telemetryData: StateFlow<TelemetryData> = repository.telemetryData

    fun emergencyStop() {
        viewModelScope.launch {
            sendCarCommandUseCase(CarCommand.EmergencyStop)
        }
    }
}
