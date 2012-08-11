#ifndef DRIVER_H_
#define DRIVER_H_

#include <stdint.h>

class Driver {
public:
    enum Direction { FORWARDS, BACKWARDS };
    Direction direction;

private:
    volatile uint8_t* const b_reg;
    volatile uint8_t* const f_reg;

public:
    Driver(volatile uint8_t* b_reg, volatile uint8_t* f_reg)
        : b_reg(b_reg), f_reg(f_reg) {}
    void set_direction(Direction direction) { this->direction = direction; }
    void set_speed(uint8_t speed) {
        if (speed == 0) {
            *f_reg = 0;
            *b_reg = 0;
        } else if (direction == FORWARDS) {
            *b_reg = 0;
            *f_reg = speed;
        } else {
            *f_reg = 0;
            *b_reg = speed;
        }
    }
    uint8_t get_speed() { return (*b_reg == 0) ? *f_reg : *b_reg; }
};

#endif /* MOTOR_H_ */
