#include "motor_driver.h"

MotorDriver::MotorDriver() : currentSpeed(DEFAULT_SPEED), currentState(CAR_STOPPED) {}

void MotorDriver::init() {
    pinMode(PIN_IN1, OUTPUT);
    pinMode(PIN_IN2, OUTPUT);
    pinMode(PIN_IN3, OUTPUT);
    pinMode(PIN_IN4, OUTPUT);

    // Setup ESP32 LEDC PWM Channels
    ledcSetup(PWM_CH_LEFT, PWM_FREQ, PWM_RESOLUTION);
    ledcSetup(PWM_CH_RIGHT, PWM_FREQ, PWM_RESOLUTION);

    ledcAttachPin(PIN_ENA, PWM_CH_LEFT);
    ledcAttachPin(PIN_ENB, PWM_CH_RIGHT);

    stopCar();
}

void MotorDriver::setSpeed(uint8_t speed) {
    currentSpeed = speed;
    if (currentState != CAR_STOPPED && currentState != CAR_BLOCKED && currentState != CAR_EMERGENCY) {
        applyPwm(currentSpeed, currentSpeed);
    }
}

uint8_t MotorDriver::getSpeed() const {
    return currentSpeed;
}

CarMovement MotorDriver::getCurrentState() const {
    return currentState;
}

const char* MotorDriver::getStateString() const {
    switch (currentState) {
        case CAR_FORWARD:   return "FWD";
        case CAR_BACKWARD:  return "REV";
        case CAR_LEFT:      return "LEFT";
        case CAR_RIGHT:     return "RIGHT";
        case CAR_BLOCKED:   return "BLOCKED";
        case CAR_EMERGENCY: return "EMERGENCY";
        default:            return "STOP";
    }
}

void MotorDriver::forward() {
    digitalWrite(PIN_IN1, HIGH);
    digitalWrite(PIN_IN2, LOW);
    digitalWrite(PIN_IN3, HIGH);
    digitalWrite(PIN_IN4, LOW);
    applyPwm(currentSpeed, currentSpeed);
    currentState = CAR_FORWARD;
}

void MotorDriver::backward() {
    digitalWrite(PIN_IN1, LOW);
    digitalWrite(PIN_IN2, HIGH);
    digitalWrite(PIN_IN3, LOW);
    digitalWrite(PIN_IN4, HIGH);
    applyPwm(currentSpeed, currentSpeed);
    currentState = CAR_BACKWARD;
}

void MotorDriver::turnLeft() {
    // Left side backward, Right side forward for pivot turn
    digitalWrite(PIN_IN1, LOW);
    digitalWrite(PIN_IN2, HIGH);
    digitalWrite(PIN_IN3, HIGH);
    digitalWrite(PIN_IN4, LOW);
    applyPwm(currentSpeed, currentSpeed);
    currentState = CAR_LEFT;
}

void MotorDriver::turnRight() {
    // Left side forward, Right side backward for pivot turn
    digitalWrite(PIN_IN1, HIGH);
    digitalWrite(PIN_IN2, LOW);
    digitalWrite(PIN_IN3, LOW);
    digitalWrite(PIN_IN4, HIGH);
    applyPwm(currentSpeed, currentSpeed);
    currentState = CAR_RIGHT;
}

void MotorDriver::stopCar() {
    digitalWrite(PIN_IN1, LOW);
    digitalWrite(PIN_IN2, LOW);
    digitalWrite(PIN_IN3, LOW);
    digitalWrite(PIN_IN4, LOW);
    applyPwm(0, 0);
    currentState = CAR_STOPPED;
}

void MotorDriver::emergencyStop() {
    stopCar();
    currentState = CAR_EMERGENCY;
}

void MotorDriver::applyPwm(uint8_t leftPwm, uint8_t rightPwm) {
    ledcWrite(PWM_CH_LEFT, leftPwm);
    ledcWrite(PWM_CH_RIGHT, rightPwm);
}
