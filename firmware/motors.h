#ifndef MOTORS_H_
#define MOTORS_H_

#include "motor.h"
#include <stdint.h>

class Motors {
    // The indices of the fastest and slowest motors.  The slowest will need
    // to be speed controlled.
    uint8_t fastest, slowest;
    // The value each motor is *supposed* to have.  The motor will overrun
    // this value as it stops.
    uint16_t expected[2];
    volatile uint16_t* const servo_reg;
    const uint16_t servo_draw;
    const uint16_t servo_move;
    bool is_drawing;

public:
    Motor* motors;
    Motors(Motor motors[2], volatile uint16_t* const servo_reg,
            uint16_t servo_draw, uint16_t servo_move)
        : servo_reg(servo_reg),
          servo_draw(servo_draw),
          servo_move(servo_move),
          is_drawing(false),
          motors(motors) {
        expected[0] = 0; expected[1] = 0;
    }
    void move(int16_t dist[2]);
    // Updates the encoder status, and adjusts the motors speeds.
    void check(uint8_t motor);
    void set_drawing(bool is_drawing);
};

#endif /* MOTORS_H_ */
