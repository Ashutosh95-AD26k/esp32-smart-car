# ESP32 Smart Car - Low-Latency Communication Protocol

This document specifies the deterministic, ultra-low latency serial communication protocol exchanged between the **Android Smart Car App** and the **ESP32 Firmware** over Classic Bluetooth SPP (Serial Port Profile, RFCOMM).

---

## 1. Transport Characteristics

- **Transport**: Bluetooth Classic SPP (Serial Port Profile / RFCOMM)
- **Service UUID**: `00001101-0000-1000-8000-00805F9B34FB`
- **Default Device Name**: `ESP32_SMART_CAR`
- **Frame Delimiter**: `\n` (ASCII Line Feed `0x0A` or `\r\n`)
- **Encoding**: UTF-8 ASCII
- **Payload Design**: 1 to 5 characters per frame (minimal serialization and transmission overhead)

---

## 2. Command Set (Android App -> ESP32)

| Frame | Name | Description | Response / Action |
| :--- | :--- | :--- | :--- |
| `F\n` | **Forward** | Engage both motor sides forward | `S:FWD\n` (or `S:BLOCKED\n` if obstacle <= 20cm) |
| `B\n` | **Backward** | Engage both motor sides in reverse | `S:REV\n` |
| `L\n` | **Turn Left** | Pivot turn left (Left side reverse, Right side forward) | `S:LEFT\n` |
| `R\n` | **Turn Right** | Pivot turn right (Left side forward, Right side reverse) | `S:RIGHT\n` |
| `S\n` | **Stop** | Normal deceleration / Stop motors | `S:STOP\n` |
| `X\n` | **Emergency Stop**| Instant motor cut-off and lock | `S:EMERGENCY\n` |
| `V<0..255>\n` | **Set Speed** | Adjust 8-bit PWM speed (e.g. `V200\n`) | Updates current PWM duty cycle |
| `H\n` | **Heartbeat** | Ping from Android client to keep watchdog alive | `H\n` (Heartbeat Ack) |

---

## 3. Telemetry Set (ESP32 -> Android App)

| Frame Format | Name | Description | Frequency |
| :--- | :--- | :--- | :--- |
| `D<value>\n` | **Distance Telemetry** | Filtered obstacle distance in cm (e.g. `D34.5\n` or `D120.0\n`) | 10 Hz (every 100ms) |
| `S:FWD\n` | **State: Forward** | Acknowledges vehicle is driving forward | On state change |
| `S:REV\n` | **State: Reverse** | Acknowledges vehicle is driving backward | On state change |
| `S:LEFT\n` | **State: Left** | Acknowledges vehicle is turning left | On state change |
| `S:RIGHT\n` | **State: Right** | Acknowledges vehicle is turning right | On state change |
| `S:STOP\n` | **State: Stopped** | Vehicle is halted | On state change |
| `S:BLOCKED\n` | **State: Blocked** | Forward motion blocked by obstacle detection interlock | On obstacle event |
| `S:EMERGENCY\n`| **State: Emergency** | Hardware emergency stop triggered | On emergency cut-off |
| `H\n` | **Heartbeat Ack** | Heartbeat echo | On receipt of `H\n` |

---

## 4. Priority & Safety Hierarchy

```
Priority 1: EMERGENCY STOP (`X` command or distance <= 10 cm)
             ↳ Immediate PWM 0 cut-off & brake
Priority 2: STOP (`S` command / Watchdog timeout)
             ↳ Normal motor halt
Priority 3: OBSTACLE SAFETY STOP (distance <= 20 cm)
             ↳ Rejects `F`, stops if moving forward, allows `B`, `L`, `R`
Priority 4: MANUAL DIRECTIONAL DRIVING (`F`, `B`, `L`, `R`)
             ↳ High-frequency real-time user inputs
Priority 5: VOICE RECOGNITION COMMANDS
             ↳ Parsed and validated via NLP before transmission
Priority 6: SPEED & TELEMETRY STREAMING (`V###`, `D###`, `H`)
             ↳ Non-blocking background telemetry
```

---

## 5. Communication Watchdog & Fail-Safe

- The ESP32 maintains a **1000ms dead-man communication watchdog**.
- If the vehicle is in motion and no new command or heartbeat frame is received within 1000ms (e.g. due to out-of-range, app crash, or phone disconnect), the ESP32 automatically halts all 4 motors and emits `S:STOP\n`.
