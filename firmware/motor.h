#ifndef MOTOR_H_
#define MOTOR_H_

#include <stdint.h>
#include "driver.h"
#include "encoder.h"

class Motor {
public:
    Driver driver;
    Encoder encoder;

    Motor(volatile uint8_t* b_reg, volatile uint8_t* f_reg,
            volatile const uint8_t* const port, uint8_t pin)
        : driver(b_reg, f_reg), encoder(driver, port, pin) {}
    void move(int16_t distance, uint8_t speed);
};

#endif /* MOTOR_H_ */
