#include <cppunit/TestFixture.h>
#include <cppunit/TestCaller.h>
#include <cppunit/TestAssert.h>
#include <cppunit/TestSuite.h>
#include <cstdio>
#include <algorithm>
#include "parser.h"

#include <cppunit/extensions/HelperMacros.h>

class ParserTest: public CppUnit::TestFixture {
private:
    CPPUNIT_TEST_SUITE(ParserTest);
    CPPUNIT_TEST(testSimple);
    CPPUNIT_TEST_SUITE_END();

public:
    void testSimple() {
        Parser p;
        CPPUNIT_ASSERT(p.handle('p') == false);
        CPPUNIT_ASSERT_EQUAL((uint8_t) Parser::P, p.command);
        CPPUNIT_ASSERT(p.handle(' ') == false);
        CPPUNIT_ASSERT(p.handle('1') == false);
        CPPUNIT_ASSERT(p.handle('.') == false);
        CPPUNIT_ASSERT(p.handle('2') == false);
        CPPUNIT_ASSERT(p.handle('3') == false);
        CPPUNIT_ASSERT(p.handle(' ') == true);
        CPPUNIT_ASSERT_DOUBLES_EQUAL(1.23, p.args[0], 0.0001);

        const char command[] = "M 4.56 -7.89";
        for (const char* c = command; c - command < sizeof(command); c++)
            CPPUNIT_ASSERT(p.handle(*c) == false);
        CPPUNIT_ASSERT(p.handle(' ') == true);
        CPPUNIT_ASSERT_EQUAL((uint8_t) Parser::MOVE, p.command);
        CPPUNIT_ASSERT_DOUBLES_EQUAL(4.56, p.args[0], 0.0001);
        CPPUNIT_ASSERT_DOUBLES_EQUAL(-7.89, p.args[1], 0.0001);
    }
};

CPPUNIT_TEST_SUITE_REGISTRATION(ParserTest);
