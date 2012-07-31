#include <stdint.h>
#include <string.h>
#include <stdlib.h>
#include "parser.h"

static const uint8_t arg_count[] = { 2, 2, 1, 1, 1 };
static const char* command_chars = "MDpid";

// Appends a character to a string. Is there no standard function for this?
static void append(char* s, char c) {
    uint8_t len = strlen(s);
    s[len] = c;
    s[len+1] = 0;
}

bool Parser::handle(char c) {
    // This can be found in the list of commands, so handle it differently
    if (c == 0)
        return false;
    if (c == ' ' && strlen(buf) > 0) {
        args[args_count++] = atof(buf);
        buf[0] = 0;
        return (args_count == arg_count[command]);
    }
    if ((c >= '0' && c <= '9') || c == '-' || c == '.')
        append(buf, c);
    else {
        const char* command_p = strchr(command_chars, c);
        if (command_p != NULL) {
            command = (command_p - command_chars);
            args_count = 0;
        }
    }
    return false;
}
