#ifndef CONFIG_H
#define CONFIG_H

#include <Arduino.h>

// ==========================================
// BLUETOOTH CONFIGURATION
// ==========================================
#define BT_DEVICE_NAME "ESP32_SMART_CAR"

// ==========================================
// MOTOR DRIVER (L298N) PIN CONFIGURATION
// ==========================================
#define PIN_ENA 22  // Left Motors PWM Speed
#define PIN_IN1 16  // Left Motors Direction 1
#define PIN_IN2 17  // Left Motors Direction 2

#define PIN_IN3 18  // Right Motors Direction 1
#define PIN_IN4 19  // Right Motors Direction 2
#define PIN_ENB 23  // Right Motors PWM Speed

// PWM Configuration (ESP32 LEDC)
#define PWM_FREQ 1000       // 1 kHz
#define PWM_RESOLUTION 8    // 8-bit resolution (0 - 255)
#define PWM_CH_LEFT 0
#define PWM_CH_RIGHT 1

#define DEFAULT_SPEED 180   // Default speed (0 - 255)

// ==========================================
// ULTRASONIC SENSOR (HC-SR04) PIN CONFIGURATION
// ==========================================
#define PIN_TRIG 32  // Trigger Output (3.3V logic)
#define PIN_ECHO 33  // Echo Input (MUST use 1k/2k voltage divider from 5V HC-SR04!)

// ==========================================
// SAFETY & OBSTACLE THRESHOLDS (in cm)
// ==========================================
#define DIST_SAFE_THRESHOLD 30.0f       // > 30 cm: Normal operation
#define DIST_WARNING_THRESHOLD 20.0f    // 20 - 30 cm: Warning zone
#define DIST_BLOCK_FORWARD 20.0f        // <= 20 cm: Forward movement blocked
#define DIST_EMERGENCY_STOP 10.0f       // <= 10 cm: Immediate emergency cut-off

// ==========================================
// TIMINGS & WATCHDOG (in milliseconds)
// ==========================================
#define COMMAND_WATCHDOG_TIMEOUT 1000   // 1.0s communication dead-man timeout
#define TELEMETRY_SEND_INTERVAL 100     // 100ms (10Hz) distance telemetry update
#define ULTRASONIC_SAMPLE_INTERVAL 50   // 50ms (20Hz) sensor read

#endif // CONFIG_H
