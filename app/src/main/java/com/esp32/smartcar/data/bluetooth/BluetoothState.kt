package com.esp32.smartcar.data.bluetooth

sealed class BluetoothState {
    object Disconnected : BluetoothState()
    object Connecting : BluetoothState()
    data class Connected(val deviceName: String, val address: String) : BluetoothState()
    object Reconnecting : BluetoothState()
    data class ConnectionFailed(val reason: String) : BluetoothState()
    data class Error(val message: String) : BluetoothState()
}
