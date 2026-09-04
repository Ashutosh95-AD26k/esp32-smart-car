package com.esp32.smartcar.domain.usecase

import com.esp32.smartcar.domain.repository.CarRepository

class ConnectDeviceUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(address: String): Boolean {
        return repository.connect(address)
    }
}
