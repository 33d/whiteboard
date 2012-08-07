#ifndef MOTORS_H_
#define MOTORS_H_

#include "motor.h"
#include <stdint.h>

class Motors {
    Motor* motors;
    // The indices of the fastest and slowest motors.  The slowest will need
    // to be speed controlled.
    uint8_t fastest, slowest;
    // The value each motor is *supposed* to have.  The motor will overrun
    // this value as it stops.
    uint16_t expected[2];

public:
    Motors(Motor motors[2])
        : motors(motors) {
        expected[0] = 0; expected[1] = 0;
    }
    void move(int16_t dist[2]);
    // Updates the encoder status, and adjusts the motors speeds.
    void check(uint8_t motor);
};

#endif /* MOTORS_H_ */
