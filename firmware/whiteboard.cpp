#include <stdint.h>
#include <stdio.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/sleep.h>
#include "parser.h"
#include "serial.h" // for printf

// Serial port settings
#define SERIAL_BAUD 9600
// #define SERIAL_U2X

// Serial port calculations
#if defined(SERIAL_U2X)
#define SERIAL_UBRR_VAL ((F_CPU / 8 / SERIAL_BAUD) - 1)
#else
#define SERIAL_UBRR_VAL ((F_CPU / 16 / SERIAL_BAUD) - 1)
#endif

namespace Events {
    const uint8_t SERIAL_RX = 1;
    char serial_rx;
}
#define events (GPIOR0)

Parser parser;

static void handle_input() {
    if (parser.handle(Events::serial_rx)) {
        printf("%d %f %f\n", parser.command, parser.args[0], parser.args[1]);
        PINB |= _BV(PORTB5);
    }
    events &= ~Events::SERIAL_RX;
}

ISR(USART_RX_vect) {
    events |= Events::SERIAL_RX;
    Events::serial_rx = UDR0;
}

void init_serial() {
    // Init serial
    UBRR0H = (uint8_t)(SERIAL_UBRR_VAL >> 8);
    UBRR0L = (uint8_t)(SERIAL_UBRR_VAL);
    // Asynchronous, no parity, 1 stop bit, 8 data bits
    UCSR0C = _BV(UCSZ01) | _BV(UCSZ00);
    // Enable TX and RX, interrupts on RX
    UCSR0B = _BV(TXEN0) | _BV(RXEN0) | _BV(RXCIE0);
    UCSR0A = 0
#if defined(SERIAL_U2X)
      | _BV(U2X0)
#endif
    ;
}

int main(void) {
    init_serial();
    serial_init();

    DDRB |= _BV(PORTB5);

    sei();

    while(true) {
        if (events & Events::SERIAL_RX)
            handle_input();
        sleep_mode();
    }
}
