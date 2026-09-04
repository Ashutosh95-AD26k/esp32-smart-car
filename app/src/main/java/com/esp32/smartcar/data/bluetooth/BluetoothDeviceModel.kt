package com.esp32.smartcar.data.bluetooth

data class BluetoothDeviceModel(
    val name: String,
    val address: String,
    val isPaired: Boolean = true
)
