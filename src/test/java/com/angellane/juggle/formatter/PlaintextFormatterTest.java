package com.angellane.juggle.formatter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaintextFormatterTest {
    Formatter f = new PlaintextFormatter();

    @Test
    void testFormatKeyword() {
        assertEquals("private", f.formatKeyword("private"));
    }

    @Test
    void testFormatPackageName() {
        assertEquals("com.angellane.juggle", f.formatPackageName("com.angellane.juggle"));
    }

    @Test
    void testFormatClassName() {
        assertEquals("Juggler", f.formatClassName("Juggler"));
    }

    @Test
    void testFormatMethodName() {
        assertEquals("goJuggle", f.formatMethodName("goJuggle"));
    }

    @Test
    void testFormatType() {
        assertEquals("int", f.formatType("int"));
        assertEquals("com.angellane.juggle.Juggler", f.formatType("com.angellane.juggle.Juggler"));
    }
}
