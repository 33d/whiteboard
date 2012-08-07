#include "encoder.h"

void Encoder::check() {
    bool level = ((*port) & pin) != 0;
    if (level != old) {
        count += 1;
        old = level;
    }
}
