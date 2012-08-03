#include "motor.h"

void Motor::move(int16_t distance) {
    encoder.expected += distance;
    driver.direction = distance < 0 ? Driver::BACKWARDS : Driver::FORWARDS;
    if (distance != 0) driver.set_speed(255);
}
