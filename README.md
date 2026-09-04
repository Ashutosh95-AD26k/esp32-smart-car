# ESP32 Smart Car - Production Control System

A complete, production-grade robotics control platform consisting of a modern **Android Jetpack Compose Application** and **ESP32 Classic Bluetooth (SPP) Firmware** for a 4WD obstacle-detecting robotic vehicle.

---

## Key Highlights

- **Direct Classic Bluetooth (SPP / RFCOMM)**: Connects directly to the ESP32's onboard Bluetooth. No external HC-05/HC-06 modules required.
- **Ultra Low-Latency Command Protocol**: Framed deterministic ASCII packets (`F\n`, `B\n`, `L\n`, `R\n`, `S\n`, `X\n`, `V###\n`) with sub-10ms latency.
- **Hardware-Level Obstacle Safety Interlock**:
  - $> 30\text{ cm}$: Safe cruising zone
  - $20\text{ - }30\text{ cm}$: Warning threshold
  - $\le 20\text{ cm}$: Forward movement blocked (escape reverse/turns permitted)
  - $\le 10\text{ cm}$: Immediate Emergency Stop cut-off
- **Communication Watchdog**: 1000ms dead-man timeout stops motors automatically if Bluetooth disconnects or signals cease while in motion.
- **Multilingual Voice Control**: Built-in Speech Recognition parser supporting English ("Go forward", "Stop") and Hindi ("आगे जाओ", "गाड़ी रोको", "बाएं", "दाएं").
- **Clean Architecture & Jetpack Compose UI**: Built with Material 3, MVVM, StateFlow, Coroutines, and responsive dark robotics aesthetic.
- **Tactile Haptic Feedback**: Vibrations for virtual joystick actions, obstacle threshold alarms, and emergency stops.

---

## System Architecture

```
+-------------------------------------------------------------------+
|                        Android Application                        |
|                                                                   |
|  [ Presentation Layer: Jetpack Compose + Material 3 ]            |
|    - Home Dashboard (Telemetry & Status HUD, Quick Action Cards)  |
|    - Drive Screen (Virtual Joystick, D-Pad, Prominent E-STOP)     |
|    - Voice Control (SpeechRecognizer, English & Hindi parser)     |
|    - Sensor Telemetry (Distance Gauge, Obstacle Alert, Motor Log) |
|    - Settings (Device selection, Speed, Obstacle thresholds)      |
|                                                                   |
|  [ Domain Layer ]                                                 |
|    - Models: CarCommand, MovementState, TelemetryData, SafetyState|
|    - Use Cases: SendCarCommandUseCase, ParseVoiceCommandUseCase,  |
|      ProcessTelemetryUseCase, ConnectDeviceUseCase                |
|                                                                   |
|  [ Data Layer ]                                                   |
|    - BluetoothSPPManager (RfcommSocket, Coroutine I/O Loops)     |
|    - CarRepositoryImpl (Flows for Connection, Telemetry, State)   |
|    - VoiceRecognitionManager (Android SpeechRecognizer API)       |
+-------------------------------------------------------------------+
                               |
                      Bluetooth Classic SPP
                        (RFCOMM / SPP UUID)
                               |
                               v
+-------------------------------------------------------------------+
|                         ESP32 DevKit V4                           |
|                                                                   |
|  [ BluetoothSerial ("ESP32_SMART_CAR") ]                          |
|    - Low-latency framed ASCII parser (F, B, L, R, S, X, V###)     |
|    - Telemetry emitter (D###\n, H\n)                              |
|    - Communication Watchdog (1000ms dead-man timeout -> Stop)     |
|                                                                   |
|  [ Safety & Obstacle Engine ]                                     |
|    - HC-SR04 Ultrasonic Driver (Trig: GPIO32, Echo: GPIO33)       |
|    - Voltage Divider Level-Shifted (5V -> 3.3V)                   |
|    - Forward Obstacle Interlock                                   |
|                                                                   |
|  [ Motor Control Subsystem ]                                      |
|    - L298N Dual H-Bridge Driver                                   |
|    - Left Motors: ENA (GPIO22 PWM), IN1 (GPIO16), IN2 (GPIO17)    |
|    - Right Motors: ENB (GPIO23 PWM), IN3 (GPIO18), IN4 (GPIO19)   |
|    - ESP32 LEDC PWM Frequency (1000Hz, 8-bit resolution: 0-255)  |
+-------------------------------------------------------------------+
```

---

## Hardware Pinout & Wiring

| ESP32 Pin | Connected Hardware | Function |
| :--- | :--- | :--- |
| **GPIO 22** | L298N `ENA` | Left Motors Speed (PWM LEDC Ch 0) |
| **GPIO 16** | L298N `IN1` | Left Motor Direction 1 |
| **GPIO 17** | L298N `IN2` | Left Motor Direction 2 |
| **GPIO 18** | L298N `IN3` | Right Motor Direction 1 |
| **GPIO 19** | L298N `IN4` | Right Motor Direction 2 |
| **GPIO 23** | L298N `ENB` | Right Motors Speed (PWM LEDC Ch 1) |
| **GPIO 32** | HC-SR04 `TRIG` | Ultrasonic 10µs Trigger Output |
| **GPIO 33** | HC-SR04 `ECHO` | Ultrasonic Echo Input (**Through 1kΩ / 2kΩ Voltage Divider**) |
| **GND** | L298N GND & Sensor GND | Common System Ground |

> [!CAUTION]
> HC-SR04 ECHO pin outputs 5V. Always use a voltage divider (1kΩ between ECHO and GPIO33, 2kΩ between GPIO33 and GND) to step down the signal to 3.3V before connecting to the ESP32.

---

## Directory Structure

```
d:\ashutosh controll appp\
├── android/                         # Complete Android Studio project
│   ├── app/
│   │   ├── build.gradle.kts
│   │   ├── proguard-rules.pro
│   │   └── src/
│   │       ├── main/
│   │       │   ├── AndroidManifest.xml
│   │       │   ├── java/com/esp32/smartcar/
│   │       │   │   ├── SmartCarApp.kt
│   │       │   │   ├── MainActivity.kt
│   │       │   │   ├── data/
│   │       │   │   │   ├── bluetooth/ (BluetoothSPPManager, BluetoothState)
│   │       │   │   │   ├── repository/ (CarRepositoryImpl)
│   │       │   │   │   └── voice/ (VoiceRecognitionManager)
│   │       │   │   ├── domain/
│   │       │   │   │   ├── model/ (CarCommand, MovementState, TelemetryData)
│   │       │   │   │   ├── repository/ (CarRepository)
│   │       │   │   │   └── usecase/ (SendCarCommand, ParseVoiceCommand, ...)
│   │       │   │   ├── presentation/
│   │       │   │   │   ├── components/ (VirtualJoystick, DPad, RadarGauge, ...)
│   │       │   │   │   ├── home/ (HomeScreen, HomeViewModel)
│   │       │   │   │   ├── drive/ (DriveScreen, DriveViewModel)
│   │       │   │   │   ├── voice/ (VoiceScreen, VoiceViewModel)
│   │       │   │   │   ├── sensors/ (SensorsScreen, SensorsViewModel)
│   │       │   │   │   ├── settings/ (SettingsScreen, SettingsViewModel)
│   │       │   │   │   ├── theme/ (Color, Theme, Type)
│   │       │   │   │   ├── navigation/ (NavRoutes, AppNavigation)
│   │       │   │   │   └── utils/ (PermissionHelper, HapticFeedbackUtil)
│   │       │   └── res/
│   │       └── test/java/com/esp32/smartcar/ (Unit Tests)
├── firmware/
│   └── esp32_smart_car/             # ESP32 C++ Arduino Firmware
│       ├── esp32_smart_car.ino      # Main sketch with Bluetooth & Watchdog
│       ├── config.h                 # Pinouts & safety parameters
│       ├── motor_driver.h / .cpp    # L298N LEDC PWM motor controller
│       ├── ultrasonic_sensor.h / .cpp # HC-SR04 EMA filtered driver
│       └── protocol_parser.h / .cpp # Compact ASCII line protocol parser
├── HARDWARE_WIRING.md               # Schematic & voltage divider calculations
├── PROTOCOL.md                      # Low-latency ASCII frame specifications
├── TESTING.md                       # Test suites & bench test procedures
└── README.md                        # Master documentation
```

---

## How to Build & Run

### 1. ESP32 Firmware
1. Open Arduino IDE (version 2.0+).
2. Install **esp32 by Espressif Systems** via Boards Manager.
3. Select board: **ESP32 Dev Module**.
4. Open `firmware/esp32_smart_car/esp32_smart_car.ino`.
5. Connect ESP32 via USB and click **Upload**.

### 2. Android App
1. Open Android Studio (Hedgehog / Iguana / Jellyfish or newer).
2. Open the project at `d:\ashutosh controll appp\`.
3. Let Gradle sync project dependencies.
4. Run Unit Tests: Right-click `app/src/test` and select **Run 'Tests in 'smartcar''**.
5. Connect an Android phone (USB debugging enabled) and click **Run 'app'** or build APK via **Build $\rightarrow$ Build Bundle(s) / APK(s) $\rightarrow$ Build APK(s)**.

---

## License
MIT License - Open Source for robotics enthusiasts and engineers.
