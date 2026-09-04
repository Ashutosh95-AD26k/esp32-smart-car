package com.esp32.smartcar.presentation.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esp32.smartcar.data.bluetooth.BluetoothState
import com.esp32.smartcar.data.voice.VoiceRecognitionManager
import com.esp32.smartcar.domain.model.CarCommand
import com.esp32.smartcar.domain.model.TelemetryData
import com.esp32.smartcar.domain.model.VoiceCommandResult
import com.esp32.smartcar.domain.repository.CarRepository
import com.esp32.smartcar.domain.usecase.SendCarCommandUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(
    private val voiceRecognitionManager: VoiceRecognitionManager,
    private val repository: CarRepository,
    private val sendCarCommandUseCase: SendCarCommandUseCase
) : ViewModel() {

    val voiceState: StateFlow<VoiceCommandResult> = voiceRecognitionManager.voiceState
    val bluetoothState: StateFlow<BluetoothState> = repository.bluetoothState
    val telemetryData: StateFlow<TelemetryData> = repository.telemetryData

    private val _selectedLanguage = MutableStateFlow("en-US") // "en-US" or "hi-IN"
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    init {
        viewModelScope.launch {
            voiceRecognitionManager.voiceState.collect { result ->
                if (result is VoiceCommandResult.Success) {
                    sendCarCommandUseCase(result.command)
                }
            }
        }
    }

    fun setLanguage(languageTag: String) {
        _selectedLanguage.value = languageTag
        voiceRecognitionManager.setLanguage(languageTag)
    }

    fun startListening() {
        voiceRecognitionManager.startListening()
    }

    fun stopListening() {
        voiceRecognitionManager.stopListening()
    }

    fun resetVoiceState() {
        voiceRecognitionManager.resetState()
    }

    fun emergencyStop() {
        viewModelScope.launch {
            sendCarCommandUseCase(CarCommand.EmergencyStop)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognitionManager.stopListening()
    }
}
