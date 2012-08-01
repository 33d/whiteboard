#include <stdint.h>
#include <stdio.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/sleep.h>
#include "parser.h"
#include "serial.h" // for printf
#include "motor.h"
#include "encoder.h"

// Serial port settings
#define SERIAL_BAUD 9600
// #define SERIAL_U2X

// Serial port calculations
#if defined(SERIAL_U2X)
#define SERIAL_UBRR_VAL ((F_CPU / 8 / SERIAL_BAUD) - 1)
#else
#define SERIAL_UBRR_VAL ((F_CPU / 16 / SERIAL_BAUD) - 1)
#endif

#define MS_PER_TIMER1_TICK (1024 / F_CPU * 1000)

namespace Events {
    const uint8_t SERIAL_RX = 1;
    const uint8_t MOTORL = 2;
    const uint8_t MOTORR = 4;
    char serial_rx;
}
#define events (GPIOR0)

Parser parser;

Motor left_motor(&OCR0A, &OCR0B);
Motor right_motor(&OCR2B, &OCR2A);
Encoder left_encoder(left_motor, &PINB, _BV(PORTB0));
Encoder right_encoder(right_motor, &PINC, _BV(PORTC5));

static void run_motor(Motor& motor, float speed) {
    motor.set_direction(speed > 0 ? Motor::FORWARDS : Motor::BACKWARDS);
    if (speed < 0) speed = -speed;
    motor.set_speed((uint8_t) (speed * 255));
}

static void handle_input() {
    if (parser.handle(Events::serial_rx)) {
        printf("%d %f %f\n", parser.command, parser.args[0], parser.args[1]);
        if (parser.command == Parser::DRAW || parser.command == Parser::MOVE) {
            run_motor(left_motor, parser.args[0]);
            run_motor(right_motor, parser.args[1]);
            printf("%d %d\n", OCR0A, OCR0B);
        } else
            printf("%d %d\n", left_encoder.get_count(), right_encoder.get_count());
    }
    events &= ~Events::SERIAL_RX;
}

ISR(PCINT0_vect) {
    events |= Events::MOTORL;
}

ISR(PCINT1_vect) {
    events |= Events::MOTORR;
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

void init_timers() {
    TCCR0A =
        // Set output pins on compare match, clear on overflow
        _BV(COM0A1) | _BV(COM0A0)
        | _BV(COM0B1) | _BV(COM0B0)
        // Fast PWM mode, between 0 and 0xFF
        | _BV(WGM01) | _BV(WGM00);
    TCCR0B =
        // 8x prescaling
        _BV(CS01);

    // Repeat for timer 2
    TCCR2A =
        // Set timers on compare match, clear on overflow
        _BV(COM2A1) | _BV(COM2A0)
        | _BV(COM2B1) | _BV(COM2B0)
        // Fast PWM mode, between 0 and 0xFF
        | _BV(WGM21) | _BV(WGM20);
    TCCR0B =
        // 8x prescaling
        _BV(CS21);

    // Use timer 1 for the clock
    TCCR1A = 0; // Normal mode, no output
    TCCR1B = _BV(CS12) | _BV(CS10); // 1024x prescaling
}

static void init_encoders() {
    DDRB &= ~_BV(PORTB0);
    DDRC &= ~_BV(PORTC5);

    // Enable interrupts
    PCMSK0 = _BV(PCINT0); // pin B0
    PCMSK1 = _BV(PCINT13); // pin C5
    PCICR = _BV(PCIE1) | _BV(PCIE0);
}

int main(void) {
    init_serial();
    serial_init();
    init_timers();
    init_encoders();

    DDRB |= _BV(PORTB5);
    // Set the motors as outputs
    DDRB |= _BV(PORTB3);
    DDRD |= _BV(PORTD3) | _BV(PORTD5) | _BV(PORTD6);

    sei();

    while(true) {
        while (events) {
            if (events & Events::MOTORL) {
                left_encoder.check();
                events &= ~Events::MOTORL;
            } else if (events & Events::MOTORR) {
                right_encoder.check();
                events &= ~Events::MOTORR;
            } else if (events & Events::SERIAL_RX)
                handle_input();
        }
        sleep_mode();
    }
}
