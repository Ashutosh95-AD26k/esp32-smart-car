package com.esp32.smartcar

import android.app.Application
import com.esp32.smartcar.data.bluetooth.BluetoothSPPManager
import com.esp32.smartcar.data.repository.CarRepositoryImpl
import com.esp32.smartcar.data.voice.VoiceRecognitionManager
import com.esp32.smartcar.domain.repository.CarRepository
import com.esp32.smartcar.domain.usecase.ConnectDeviceUseCase
import com.esp32.smartcar.domain.usecase.ObserveTelemetryUseCase
import com.esp32.smartcar.domain.usecase.ParseVoiceCommandUseCase
import com.esp32.smartcar.domain.usecase.SendCarCommandUseCase

class SmartCarApp : Application() {

    lateinit var bluetoothSPPManager: BluetoothSPPManager
        private set
    lateinit var carRepository: CarRepository
        private set
    lateinit var voiceRecognitionManager: VoiceRecognitionManager
        private set

    // Use cases
    lateinit var connectDeviceUseCase: ConnectDeviceUseCase
        private set
    lateinit var sendCarCommandUseCase: SendCarCommandUseCase
        private set
    lateinit var parseVoiceCommandUseCase: ParseVoiceCommandUseCase
        private set
    lateinit var observeTelemetryUseCase: ObserveTelemetryUseCase
        private set

    override fun onCreate() {
        super.onCreate()
        appInstance = this

        bluetoothSPPManager = BluetoothSPPManager(this)
        carRepository = CarRepositoryImpl(bluetoothSPPManager)
        parseVoiceCommandUseCase = ParseVoiceCommandUseCase()
        voiceRecognitionManager = VoiceRecognitionManager(this, parseVoiceCommandUseCase)

        connectDeviceUseCase = ConnectDeviceUseCase(carRepository)
        sendCarCommandUseCase = SendCarCommandUseCase(carRepository)
        observeTelemetryUseCase = ObserveTelemetryUseCase(carRepository)
    }

    companion object {
        lateinit var appInstance: SmartCarApp
            private set
    }
}
