#if !defined(SERIAL_H)
#define SERIAL_H

#include <stdio.h>

/* defined in serial.c */
extern FILE stdout_uart;

void serial_init() { 
  stdout = &stdout_uart;
}

#endif
