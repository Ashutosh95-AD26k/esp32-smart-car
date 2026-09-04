package com.esp32.smartcar.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esp32.smartcar.data.bluetooth.BluetoothDeviceModel
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.MovementState
import com.esp32.smartcar.domain.model.TelemetryData
import com.esp32.smartcar.domain.repository.CarRepository
import com.esp32.smartcar.domain.usecase.ConnectDeviceUseCase
import com.esp32.smartcar.domain.usecase.SendCarCommandUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: CarRepository,
    private val connectDeviceUseCase: ConnectDeviceUseCase,
    private val sendCarCommandUseCase: SendCarCommandUseCase
) : ViewModel() {

    val bluetoothState: StateFlow<BluetoothState> = repository.bluetoothState
    val telemetryData: StateFlow<TelemetryData> = repository.telemetryData
    val pairedDevices: StateFlow<List<BluetoothDeviceModel>> = repository.pairedDevices

    fun refreshDevices() {
        repository.refreshPairedDevices()
    }

    fun connectDevice(address: String) {
        viewModelScope.launch {
            connectDeviceUseCase(address)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    fun emergencyStop() {
        viewModelScope.launch {
            sendCarCommandUseCase(CarCommand.EmergencyStop)
        }
    }
}
