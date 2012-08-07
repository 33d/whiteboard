#include <stdlib.h>
#include "motors.h"

void Motors::move(int16_t dist[2]) {
    uint16_t adist[] = { abs(dist[0]), abs(dist[1]) };
    fastest = adist[0] > adist[1] ? 0 : 1;
    slowest = (~fastest) & 1;

    // Run the fastest motor at full speed
    motors[fastest].move(dist[fastest], 255);

    // Guess the speed for the other motor
    uint8_t speed = adist[0] == adist[1]
          ? 255
          : (adist[slowest] * 256) / adist[fastest];
    motors[slowest].move(dist[slowest], speed);

    for (uint8_t i = 0; i < 2; i++) {
        motors[i].encoder.count = 0;
        expected[i] = adist[i];
    }
}

void Motors::check(uint8_t motor) {
    motors[motor].encoder.check();
    if (motors[motor].encoder.get_count() >= expected[motor])
        motors[motor].driver.set_speed(0);
}
