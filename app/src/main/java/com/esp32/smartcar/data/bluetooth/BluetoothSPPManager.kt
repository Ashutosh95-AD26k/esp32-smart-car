package com.esp32.smartcar.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothSPPManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
) {
    companion object {
        private const val TAG = "BluetoothSPPManager"
        // Standard SerialPortProfile (SPP) UUID
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val TARGET_DEVICE_NAME = "ESP32_SMART_CAR"
        private const val MAX_RECONNECT_ATTEMPTS = 3
    }

    private val _connectionState = MutableStateFlow<BluetoothState>(BluetoothState.Disconnected)
    val connectionState: StateFlow<BluetoothState> = _connectionState.asStateFlow()

    private val _incomingTelemetry = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingTelemetry: SharedFlow<String> = _incomingTelemetry.asSharedFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceModel>> = _pairedDevices.asStateFlow()

    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var readerScope: CoroutineScope? = null
    private var lastConnectedAddress: String? = null
    private var isManualDisconnect = false

    init {
        refreshPairedDevices()
    }

    fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun refreshPairedDevices() {
        try {
            if (bluetoothAdapter?.isEnabled == true) {
                val bonded = bluetoothAdapter.bondedDevices ?: emptySet()
                val list = bonded.map {
                    BluetoothDeviceModel(
                        name = it.name ?: "Unknown Device",
                        address = it.address,
                        isPaired = true
                    )
                }.sortedByDescending { it.name.contains(TARGET_DEVICE_NAME, ignoreCase = true) }
                _pairedDevices.value = list
            } else {
                _pairedDevices.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing paired devices: ${e.message}")
            _pairedDevices.value = emptyList()
        }
    }

    suspend fun connect(deviceAddress: String): Boolean = withContext(Dispatchers.IO) {
        isManualDisconnect = false
        lastConnectedAddress = deviceAddress

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = BluetoothState.ConnectionFailed("Bluetooth is disabled or unavailable")
            return@withContext false
        }

        disconnectInternal()

        _connectionState.value = BluetoothState.Connecting
        Log.d(TAG, "Attempting Bluetooth connection to $deviceAddress...")

        try {
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
            bluetoothAdapter.cancelDiscovery() // Cancel discovery to save bandwidth and speed up connection

            // Create RFCOMM socket using standard SPP UUID
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()

            bluetoothSocket = socket
            outputStream = socket.outputStream

            val deviceName = device.name ?: "ESP32_SMART_CAR"
            _connectionState.value = BluetoothState.Connected(deviceName, deviceAddress)
            Log.i(TAG, "Connected successfully to $deviceName ($deviceAddress)")

            startReadingLoop(socket)
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}")
            disconnectInternal()
            _connectionState.value = BluetoothState.ConnectionFailed(e.message ?: "Connection refused")
            return@withContext false
        }
    }

    private fun startReadingLoop(socket: BluetoothSocket) {
        readerScope?.cancel()
        readerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        readerScope?.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                while (isActive && socket.isConnected) {
                    val line = reader.readLine()
                    if (line != null) {
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty()) {
                            _incomingTelemetry.emit(trimmed)
                        }
                    } else {
                        // End of stream - socket closed
                        break
                    }
                }
            } catch (e: Exception) {
                if (!isManualDisconnect) {
                    Log.w(TAG, "Socket read exception: ${e.message}")
                }
            } finally {
                if (!isManualDisconnect) {
                    handleUnexpectedDisconnect()
                }
            }
        }
    }

    private fun handleUnexpectedDisconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.w(TAG, "Unexpected disconnection detected. Attempting reconnect...")
            _connectionState.value = BluetoothState.Reconnecting
            disconnectInternal()

            val targetAddress = lastConnectedAddress
            if (targetAddress != null && !isManualDisconnect) {
                var reconnected = false
                for (attempt in 1..MAX_RECONNECT_ATTEMPTS) {
                    delay(1500)
                    Log.d(TAG, "Reconnection attempt $attempt/$MAX_RECONNECT_ATTEMPTS...")
                    if (connect(targetAddress)) {
                        reconnected = true
                        break
                    }
                }
                if (!reconnected) {
                    _connectionState.value = BluetoothState.Disconnected
                }
            } else {
                _connectionState.value = BluetoothState.Disconnected
            }
        }
    }

    suspend fun sendRaw(data: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val stream = outputStream
            val socket = bluetoothSocket
            if (socket != null && socket.isConnected && stream != null) {
                stream.write(data.toByteArray(Charsets.UTF_8))
                stream.flush()
                return@withContext true
            } else {
                Log.w(TAG, "Cannot send command: Socket is not connected")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error transmitting command: ${e.message}")
            handleUnexpectedDisconnect()
            return@withContext false
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        isManualDisconnect = true
        disconnectInternal()
        _connectionState.value = BluetoothState.Disconnected
    }

    private fun disconnectInternal() {
        try {
            readerScope?.cancel()
            readerScope = null
            outputStream?.close()
            outputStream = null
            bluetoothSocket?.close()
            bluetoothSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing socket: ${e.message}")
        }
    }
}
