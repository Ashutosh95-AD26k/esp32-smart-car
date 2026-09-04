# ESP32 Smart Car - QA & Test Documentation

Comprehensive test coverage matrix, test suites, and physical bench test procedures for the ESP32 Smart Car system.

---

## 1. Unit Test Suites (Android App)

The Android test suite is located in `app/src/test/java/com/esp32/smartcar/`.

### A. Protocol Serialization & Parsing Tests (`CommandProtocolTest.kt`)
- **Objective**: Verify exact ASCII frame generation and deterministic token parsing.
- **Test Cases**:
  - Validates `F\n`, `B\n`, `L\n`, `R\n`, `S\n`, `X\n`, `H\n`.
  - Validates variable speed clamping: `V200\n`, negative clamp `V0\n`, overflow clamp `V255\n`.
  - Validates inbound string token parsing and null safety on malformed packets.

### B. Natural Language & Multilingual Voice Command Tests (`VoiceCommandParserTest.kt`)
- **Objective**: Validate speech text normalization, noise reduction, and dual-language mapping.
- **Test Cases**:
  - **English Commands**:
    - "go forward", "move forward please!", "ahead" $\rightarrow$ `Forward` (`F\n`)
    - "go backward", "reverse", "move back" $\rightarrow$ `Backward` (`B\n`)
    - "turn left", "go left" $\rightarrow$ `Left` (`L\n`)
    - "turn right", "take right" $\rightarrow$ `Right` (`R\n`)
    - "stop the car", "halt", "freeze" $\rightarrow$ `Stop` (`S\n`)
    - "emergency stop now" $\rightarrow$ `EmergencyStop` (`X\n`)
  - **Hindi Commands**:
    - "aage jao", "aage chalo", "samne jao" $\rightarrow$ `Forward` (`F\n`)
    - "peeche jao", "piche chalo", "piche lo" $\rightarrow$ `Backward` (`B\n`)
    - "baaye mudo", "baye jao" $\rightarrow$ `Left` (`L\n`)
    - "daaye jao", "daye mudo" $\rightarrow$ `Right` (`R\n`)
    - "ruk jao", "gaadi roko", "ruko" $\rightarrow$ `Stop` (`S\n`)
    - "turant roko", "aapatkaal" $\rightarrow$ `EmergencyStop` (`X\n`)
  - **Rejection & Garbage Input Handling**:
    - Non-driving phrases (e.g. "what is the weather today") return `VoiceCommandResult.Unrecognized`.

### C. Obstacle Threshold & Safety Logic Tests (`SafetyLogicTest.kt`)
- **Objective**: Verify distance classification and boundary zones.
- **Test Cases**:
  - Distance $> 30\text{ cm} \rightarrow \text{SAFE}$
  - Distance $20\text{ cm} < d \le 30\text{ cm} \rightarrow \text{WARNING}$
  - Distance $10\text{ cm} < d \le 20\text{ cm} \rightarrow \text{BLOCKED}$ (Forward locked)
  - Distance $\le 10\text{ cm} \rightarrow \text{EMERGENCY STOP}$

---

## 2. Physical Bench Testing & Step-by-Step Verification

### Step 1: ESP32 Firmware Flashing & Serial Monitor
1. Connect ESP32 DevKit to PC via micro-USB.
2. In Arduino IDE / PlatformIO, select board: **ESP32 Dev Module**.
3. Upload `firmware/esp32_smart_car/esp32_smart_car.ino`.
4. Open Serial Monitor at **115200 baud**.
5. Verify boot banner:
   ```
   ==============================================
          ESP32 SMART CAR - INITIALIZING         
   ==============================================
   [SYSTEM] Motor driver initialized on GPIO 22,16,17,18,19,23.
   [SYSTEM] HC-SR04 sensor initialized on TRIG:32, ECHO:33.
   [SYSTEM] Bluetooth Classic SPP ready. Device name: ESP32_SMART_CAR
   [SYSTEM] Ready for pairing & control.
   ```

### Step 2: Bluetooth Pairing
1. On your Android smartphone, open **Settings $\rightarrow$ Connected Devices / Bluetooth**.
2. Scan for available devices.
3. Select and pair with **`ESP32_SMART_CAR`** (Default PIN if prompted: `1234` or `0000`).

### Step 3: Android App Connection
1. Launch **ESP32 Smart Car** app on phone.
2. Grant Bluetooth Connect & Scan permissions.
3. Navigate to **Settings** tab or tap **Connect** on the header.
4. Select `ESP32_SMART_CAR` $\rightarrow$ Status pill turns green **CONNECTED**.

### Step 4: Motor & Directional Driving Verification
1. Place car on a test stand (wheels elevated).
2. Go to **Drive** tab.
3. Test **Virtual Joystick**:
   - Drag Up: Left and Right wheels spin forward.
   - Drag Down: All wheels spin backward.
   - Drag Left: Pivot spin left.
   - Drag Right: Pivot spin right.
   - Release Joystick: Wheels stop immediately.
4. Test **Directional D-Pad**: Press & hold each button and verify smooth stop on release.

### Step 5: Obstacle Avoidance Verification
1. Place an object (e.g. hand or cardboard box) $15\text{ cm}$ in front of the HC-SR04 ultrasonic sensor.
2. Observe Android UI: Radar changes to **RED** with **"OBSTACLE DETECTED: 15 CM - FORWARD LOCKED"**.
3. Attempt to drive forward: Command is safely blocked; car does not move forward.
4. Attempt to drive backward or turn: Reverse and pivot remain active to escape the obstacle.
5. Move object closer than $10\text{ cm}$: Immediate **EMERGENCY STOP** triggers.

### Step 6: Voice Control Verification
1. Open **Voice** tab.
2. Tap the large microphone icon.
3. Say in English: **"Go forward"** $\rightarrow$ Car moves forward.
4. Say in Hindi: **"Gaadi roko"** or **"Ruk jao"** $\rightarrow$ Car halts.
5. Say: **"Emergency stop"** $\rightarrow$ Vehicle executes immediate emergency stop.
