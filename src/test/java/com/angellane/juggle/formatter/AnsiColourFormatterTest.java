package com.angellane.juggle.formatter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnsiColourFormatterTest {
    Formatter f = new AnsiColourFormatter();

    @Test
    void testFormatKeyword() {
        assertEquals("\033[2mprivate\033[0;22;23;24m", f.formatKeyword("private"));
    }

    @Test
    void testFormatPackageName() {
        assertEquals("\033[2m\033[32mcom.angellane.juggle\033[0;22;23;24m", f.formatPackageName("com.angellane.juggle"));
    }

    @Test
    void testFormatClassName() {
        assertEquals("\033[32mJuggler\033[0;22;23;24m", f.formatClassName("Juggler"));
    }

    @Test
    void testFormatMethodName() {
        assertEquals("\033[92mgoJuggle\033[0;22;23;24m", f.formatMethodName("goJuggle"));
    }

    @Test
    void testFormatType() {
        assertEquals("\033[94mint\033[0;22;23;24m", f.formatType("int"));
        assertEquals("\033[94mcom.angellane.juggle.Juggler\033[0;22;23;24m", f.formatType("com.angellane.juggle.Juggler"));
    }
}
