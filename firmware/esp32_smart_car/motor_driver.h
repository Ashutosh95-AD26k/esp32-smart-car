#ifndef MOTOR_DRIVER_H
#define MOTOR_DRIVER_H

#include "config.h"

enum CarMovement {
    CAR_STOPPED,
    CAR_FORWARD,
    CAR_BACKWARD,
    CAR_LEFT,
    CAR_RIGHT,
    CAR_BLOCKED,
    CAR_EMERGENCY
};

class MotorDriver {
public:
    MotorDriver();
    void init();
    void forward();
    void backward();
    void turnLeft();
    void turnRight();
    void stopCar();
    void emergencyStop();
    void setSpeed(uint8_t speed);
    uint8_t getSpeed() const;
    CarMovement getCurrentState() const;
    const char* getStateString() const;

private:
    uint8_t currentSpeed;
    CarMovement currentState;
    void applyPwm(uint8_t leftPwm, uint8_t rightPwm);
};

#endif // MOTOR_DRIVER_H
