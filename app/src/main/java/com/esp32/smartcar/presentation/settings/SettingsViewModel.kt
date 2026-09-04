package com.esp32.smartcar.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esp32.smartcar.data.bluetooth.BluetoothDeviceModel
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.domain.model.TelemetryData
import com.esp32.smartcar.domain.repository.CarRepository
import com.esp32.smartcar.domain.usecase.ConnectDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: CarRepository,
    private val connectDeviceUseCase: ConnectDeviceUseCase
) : ViewModel() {

    val bluetoothState: StateFlow<BluetoothState> = repository.bluetoothState
    val pairedDevices: StateFlow<List<BluetoothDeviceModel>> = repository.pairedDevices
    val telemetryData: StateFlow<TelemetryData> = repository.telemetryData

    private val _warningThreshold = MutableStateFlow(30f)
    val warningThreshold: StateFlow<Float> = _warningThreshold.asStateFlow()

    private val _blockThreshold = MutableStateFlow(20f)
    val blockThreshold: StateFlow<Float> = _blockThreshold.asStateFlow()

    private val _emergencyThreshold = MutableStateFlow(10f)
    val emergencyThreshold: StateFlow<Float> = _emergencyThreshold.asStateFlow()

    private val _isHapticsEnabled = MutableStateFlow(true)
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()

    fun refreshPairedDevices() {
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

    fun setWarningThreshold(value: Float) {
        _warningThreshold.value = value
        updateThresholds()
    }

    fun setBlockThreshold(value: Float) {
        _blockThreshold.value = value
        updateThresholds()
    }

    fun setEmergencyThreshold(value: Float) {
        _emergencyThreshold.value = value
        updateThresholds()
    }

    fun toggleHaptics(enabled: Boolean) {
        _isHapticsEnabled.value = enabled
    }

    private fun updateThresholds() {
        repository.setObstacleThresholds(
            _warningThreshold.value,
            _blockThreshold.value,
            _emergencyThreshold.value
        )
    }
}
