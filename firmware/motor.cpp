#include "motor.h"

void Motor::move(int16_t distance, uint8_t speed) {
    driver.direction = distance < 0 ? Driver::BACKWARDS : Driver::FORWARDS;
    driver.set_speed(speed);
}
