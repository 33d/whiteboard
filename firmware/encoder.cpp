#include "encoder.h"

void Encoder::check() {
    bool level = ((*port) & pin) != 0;
    if (level != old) {
        count += motor.direction == Motor::FORWARDS ? 1 : -1;
        old = level;
    }
}