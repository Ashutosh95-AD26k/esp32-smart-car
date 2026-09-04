# ESP32 Smart Car - Hardware Wiring & Circuit Guide

Complete schematic reference, pinout mappings, voltage divider calculations, and electrical safety instructions for the 4WD ESP32 Smart Car.

---

## 1. Bill of Materials (BOM)

| Item | Quantity | Description |
| :--- | :--- | :--- |
| **Microcontroller** | 1 | ESP32 DevKit V4 / ESP32-WROOM-32 (30-pin board) |
| **Motor Driver** | 1 | L298N Dual H-Bridge Module |
| **Motors & Wheels** | 4 | TT Geared DC Motors (3V-6V) with Rubber Wheels |
| **Ultrasonic Sensor**| 1 | HC-SR04 (4-pin: VCC, TRIG, ECHO, GND) |
| **Resistors** | 2 | 1 kΩ and 2 kΩ (for 5V -> 3.3V ECHO Voltage Divider) |
| **Power Source** | 1 | 2x 18650 Li-ion Batteries in series (7.4V - 8.4V) or 3S LiPo (11.1V) |
| **Chassis** | 1 | 4WD Acrylic / Aluminum Robot Chassis |
| **Jumper Wires** | -- | Male-to-Female, Male-to-Male |

---

## 2. Pin Mapping Table

### A. ESP32 to L298N Dual H-Bridge Motor Driver
| ESP32 Pin | L298N Pin | Function | Notes |
| :--- | :--- | :--- | :--- |
| **GPIO 22** | `ENA` | Left Motors Speed (PWM) | Remove jumper on ENA |
| **GPIO 16** | `IN1` | Left Motors Direction A | Digital Output |
| **GPIO 17** | `IN2` | Left Motors Direction B | Digital Output |
| **GPIO 18** | `IN3` | Right Motors Direction A | Digital Output |
| **GPIO 19** | `IN4` | Right Motors Direction B | Digital Output |
| **GPIO 23** | `ENB` | Right Motors Speed (PWM) | Remove jumper on ENB |
| **GND** | `GND` | Common Ground Reference | **CRITICAL: Connect ESP32 GND to L298N GND** |

### B. ESP32 to HC-SR04 Ultrasonic Distance Sensor
| ESP32 Pin | HC-SR04 Pin | Function | Electrical Safety Note |
| :--- | :--- | :--- | :--- |
| **5V / VIN** | `VCC` | 5V Power Supply | Power directly from 5V rail |
| **GPIO 32** | `TRIG` | Trigger Pulse Output | 3.3V signal from ESP32 is sufficient for HC-SR04 |
| **GPIO 33** | `ECHO` | Echo Return Input | **MUST USE VOLTAGE DIVIDER! (Do not connect 5V directly to GPIO 33)** |
| **GND** | `GND` | Ground | Common system ground |

---

## 3. HC-SR04 ECHO Voltage Divider Level-Shifter

> [!WARNING]
> HC-SR04 ECHO pin produces a 5V TTL pulse. The ESP32 GPIO pins are rated for a maximum of 3.3V. Connecting a 5V ECHO pulse directly to GPIO33 will degrade or permanently damage the ESP32 GPIO input buffer.

### Circuit Diagram:

```
                  +5V (VCC from Power / L298N 5V Out)
                   |
             +-----------+
             |  HC-SR04  |
             |           |
             | TRIG  ECHO|
             +---+----+--+
                 |    |
   GPIO 32 <-----+    | (5V Pulse)
                      |
                     [R1: 1kΩ]
                      |
                      +------------------> GPIO 33 (3.3V Logic Safe)
                      |
                     [R2: 2kΩ]
                      |
                     GND
```

### Calculation:
$$V_{\text{out}} = V_{\text{in}} \times \frac{R_2}{R_1 + R_2} = 5.0\text{V} \times \frac{2000}{1000 + 2000} = 5.0\text{V} \times \frac{2}{3} \approx 3.33\text{V}$$

---

## 4. Motor Grouping & Wiring (4WD Configuration)

- **Left Group**: Wire Front-Left and Rear-Left motors in parallel to `OUT1` and `OUT2` on the L298N.
- **Right Group**: Wire Front-Right and Rear-Right motors in parallel to `OUT3` and `OUT4` on the L298N.

### Motor Direction Truth Table

| Action | IN1 | IN2 | IN3 | IN4 | Left PWM (ENA) | Right PWM (ENB) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Forward** | HIGH | LOW | HIGH | LOW | Speed PWM | Speed PWM |
| **Backward** | LOW | HIGH | LOW | HIGH | Speed PWM | Speed PWM |
| **Turn Left** | LOW | HIGH | HIGH | LOW | Speed PWM | Speed PWM |
| **Turn Right** | HIGH | LOW | LOW | HIGH | Speed PWM | Speed PWM |
| **Stop** | LOW | LOW | LOW | LOW | 0 | 0 |

---

## 5. Power Distribution Architecture

```
           +-------------------------+
           |  2x 18650 Li-ion Battery| (7.4V - 8.4V)
           +------------+------------+
                        |
                        v
           +-------------------------+
           |     L298N Power IN      |
           | 12V In       GND   5V Out|
           +---+-----------+------+---+
               |           |      |
         (Motor Power)     |      +--------> ESP32 5V/VIN & HC-SR04 VCC
                           |                 (Regulated 5V from L298N onboard regulator)
                           |
                           +----------------> ESP32 GND & HC-SR04 GND
                                             (Common Ground System)
```

> [!IMPORTANT]
> Ensure the L298N onboard 5V regulator jumper is installed if the input battery voltage is under 12V. This allows the L298N `5V` screw terminal to supply steady 5V to the ESP32 and ultrasonic sensor.
