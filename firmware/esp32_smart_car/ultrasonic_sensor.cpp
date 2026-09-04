#include "ultrasonic_sensor.h"

UltrasonicSensor::UltrasonicSensor() : lastValidDistance(100.0f) {}

void UltrasonicSensor::init() {
    pinMode(PIN_TRIG, OUTPUT);
    pinMode(PIN_ECHO, INPUT);
    digitalWrite(PIN_TRIG, LOW);
}

float UltrasonicSensor::readRawDistance() {
    // Send 10 microsecond pulse to TRIG pin
    digitalWrite(PIN_TRIG, LOW);
    delayMicroseconds(2);
    digitalWrite(PIN_TRIG, HIGH);
    delayMicroseconds(10);
    digitalWrite(PIN_TRIG, LOW);

    // Read duration of ECHO high pulse (timeout 25ms ~ 400cm max)
    unsigned long duration = pulseIn(PIN_ECHO, HIGH, 25000);

    if (duration == 0) {
        // No echo received (out of range or sensor error)
        return -1.0f;
    }

    // Distance calculation: Speed of sound = 343 m/s = 0.0343 cm/microsecond
    // Distance = (duration * 0.0343) / 2
    float distance = (duration * 0.0343f) / 2.0f;
    return distance;
}

float UltrasonicSensor::readDistance() {
    float raw = readRawDistance();
    if (raw > 1.0f && raw < 400.0f) {
        // Simple Exponential Moving Average (EMA) filter: alpha = 0.7
        lastValidDistance = (0.7f * raw) + (0.3f * lastValidDistance);
    }
    return lastValidDistance;
}

float UltrasonicSensor::getFilteredDistance() const {
    return lastValidDistance;
}
