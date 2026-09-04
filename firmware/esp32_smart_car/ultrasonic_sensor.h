#ifndef ULTRASONIC_SENSOR_H
#define ULTRASONIC_SENSOR_H

#include "config.h"

class UltrasonicSensor {
public:
    UltrasonicSensor();
    void init();
    float readDistance();
    float getFilteredDistance() const;

private:
    float lastValidDistance;
    float readRawDistance();
};

#endif // ULTRASONIC_SENSOR_H
