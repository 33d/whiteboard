#if !defined(ENCODER_H)
#define ENCODER_H

#include <stdint.h>
#include "motor.h"

/**
 * Handles the encoder counting.  get_count() returns the pulse count; this
 * is updated on both a high and low transition, so it will be TWICE what
 * it should be (ie. for the Faulhaber motors, it will read about 280 pulses
 * per revolution).
 */
class Encoder {
    const Motor& motor;
    volatile const uint8_t* const port;
    const uint8_t pin;
    uint16_t count;
    bool old;

public:
    /**
     * @param port The PINx value for the input.
     */
    Encoder(const Motor& motor, volatile const uint8_t* const port, uint8_t pin)
        : motor(motor), port(port), pin(pin) {}
    void check();
    uint16_t get_count() { return count; }
};

#endif
