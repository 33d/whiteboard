#if !defined(PARSER_H)
#define PARSER_H

#include <stdint.h>

class Parser {
public:
    enum Command { MOVE, DRAW, P, I, D };

private:
    char buf[32];
    uint8_t args_count;

public:
    int16_t args[2];
    uint8_t command;

    Parser() : args_count(0) { buf[0] = 0; }
    bool handle(char c);
};

#endif
