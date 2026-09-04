/**
 * ============================================================================
 * ESP32 SMART CAR - ROBOTIC VEHICLE FIRMWARE
 * Microcontroller: ESP32 DevKit V4 (30-pin)
 * Motor Driver: L298N Dual H-Bridge (4 DC Motors)
 * Ultrasonic Sensor: HC-SR04 with Voltage Divider (ECHO to GPIO33)
 * Bluetooth: ESP32 Built-in Classic Bluetooth SPP (BluetoothSerial)
 * ============================================================================
 */

#include "BluetoothSerial.h"
#include "config.h"
#include "motor_driver.h"
#include "ultrasonic_sensor.h"
#include "protocol_parser.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to enable it
#endif

BluetoothSerial SerialBT;
MotorDriver motors;
UltrasonicSensor ultrasonic;

// State variables
unsigned long lastCommandTime = 0;
unsigned long lastTelemetryTime = 0;
unsigned long lastUltrasonicTime = 0;
String btCommandBuffer = "";
bool isObstacleBlocked = false;

void setup() {
    Serial.begin(115200);
    delay(500);

    Serial.println("\n==============================================");
    Serial.println("       ESP32 SMART CAR - INITIALIZING         ");
    Serial.println("==============================================");

    // Initialize Motor Driver
    motors.init();
    Serial.println("[SYSTEM] Motor driver initialized on GPIO 22,16,17,18,19,23.");

    // Initialize Ultrasonic Sensor
    ultrasonic.init();
    Serial.println("[SYSTEM] HC-SR04 sensor initialized on TRIG:32, ECHO:33.");

    // Initialize Built-in Bluetooth Classic SPP
    if (!SerialBT.begin(BT_DEVICE_NAME)) {
        Serial.println("[ERROR] Failed to initialize Bluetooth Serial!");
    } else {
        Serial.print("[SYSTEM] Bluetooth Classic SPP ready. Device name: ");
        Serial.println(BT_DEVICE_NAME);
    }

    lastCommandTime = millis();
    lastTelemetryTime = millis();
    lastUltrasonicTime = millis();
    Serial.println("[SYSTEM] Ready for pairing & control.");
}

void processCommand(const ParsedCommand& cmd) {
    lastCommandTime = millis();

    switch (cmd.type) {
        case CMD_FORWARD:
            if (isObstacleBlocked) {
                Serial.println("[SAFETY] Forward command rejected: Obstacle within block threshold!");
                motors.stopCar();
                SerialBT.println("S:BLOCKED");
            } else {
                Serial.println("[CMD] Moving FORWARD");
                motors.forward();
                SerialBT.println("S:FWD");
            }
            break;

        case CMD_BACKWARD:
            Serial.println("[CMD] Moving BACKWARD");
            motors.backward();
            SerialBT.println("S:REV");
            break;

        case CMD_LEFT:
            Serial.println("[CMD] Turning LEFT");
            motors.turnLeft();
            SerialBT.println("S:LEFT");
            break;

        case CMD_RIGHT:
            Serial.println("[CMD] Turning RIGHT");
            motors.turnRight();
            SerialBT.println("S:RIGHT");
            break;

        case CMD_STOP:
            Serial.println("[CMD] STOP");
            motors.stopCar();
            SerialBT.println("S:STOP");
            break;

        case CMD_EMERGENCY_STOP:
            Serial.println("[SAFETY] EMERGENCY STOP TRIGGERED!");
            motors.emergencyStop();
            SerialBT.println("S:EMERGENCY");
            break;

        case CMD_SET_SPEED:
            Serial.print("[CMD] Set Speed PWM: ");
            Serial.println(cmd.value);
            motors.setSpeed(cmd.value);
            break;

        case CMD_HEARTBEAT:
            SerialBT.println("H");
            break;

        case CMD_NONE:
        default:
            break;
    }
}

void loop() {
    unsigned long currentMillis = millis();

    // 1. Read HC-SR04 Distance at interval (20Hz)
    if (currentMillis - lastUltrasonicTime >= ULTRASONIC_SAMPLE_INTERVAL) {
        lastUltrasonicTime = currentMillis;
        float distance = ultrasonic.readDistance();

        // Safety Logic Assessment
        if (distance > 0.0f && distance <= DIST_EMERGENCY_STOP) {
            // Immediate Emergency Stop if too close
            if (motors.getCurrentState() != CAR_STOPPED && motors.getCurrentState() != CAR_BACKWARD) {
                Serial.printf("[SAFETY ALERT] Emergency Obstacle! Distance: %.1f cm -> EMERGENCY STOP\n", distance);
                motors.emergencyStop();
                SerialBT.println("S:EMERGENCY");
            }
            isObstacleBlocked = true;
        } else if (distance > 0.0f && distance <= DIST_BLOCK_FORWARD) {
            // Block forward movement
            if (motors.getCurrentState() == CAR_FORWARD) {
                Serial.printf("[SAFETY ALERT] Obstacle ahead! Distance: %.1f cm -> FORWARD BLOCKED\n", distance);
                motors.stopCar();
                SerialBT.println("S:BLOCKED");
            }
            isObstacleBlocked = true;
        } else {
            isObstacleBlocked = false;
        }
    }

    // 2. Read Bluetooth Serial Incoming Characters
    while (SerialBT.available()) {
        char c = (char)SerialBT.read();
        if (c == '\n' || c == '\r') {
            if (btCommandBuffer.length() > 0) {
                ParsedCommand cmd = ProtocolParser::parse(btCommandBuffer);
                processCommand(cmd);
                btCommandBuffer = "";
            }
        } else {
            btCommandBuffer += c;
            if (btCommandBuffer.length() > 32) {
                btCommandBuffer = ""; // Overflow protection
            }
        }
    }

    // 3. Watchdog Dead-man Fail-safe Timeout
    // If car is moving and no command is received within 1000ms, safely stop motors
    if (motors.getCurrentState() != CAR_STOPPED && motors.getCurrentState() != CAR_EMERGENCY) {
        if (currentMillis - lastCommandTime > COMMAND_WATCHDOG_TIMEOUT) {
            Serial.println("[WATCHDOG] Communication timeout! Stopping motors for safety.");
            motors.stopCar();
            SerialBT.println("S:STOP");
            lastCommandTime = currentMillis;
        }
    }

    // 4. Send Periodic Telemetry over Bluetooth (10Hz)
    if (currentMillis - lastTelemetryTime >= TELEMETRY_SEND_INTERVAL) {
        lastTelemetryTime = currentMillis;
        if (SerialBT.hasClient()) {
            float dist = ultrasonic.getFilteredDistance();
            SerialBT.printf("D%.1f\n", dist);
        }
    }

    yield();
}
