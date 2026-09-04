package com.esp32.smartcar.domain.usecase

import com.esp32.smartcar.domain.model.TelemetryData
import com.esp32.smartcar.domain.repository.CarRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveTelemetryUseCase(private val repository: CarRepository) {
    operator fun invoke(): StateFlow<TelemetryData> = repository.telemetryData
}
