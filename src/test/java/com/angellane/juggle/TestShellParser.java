package com.angellane.juggle;

import com.angellane.juggle.testsupport.ShellParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestShellParser {
    ShellParser p = new ShellParser();

    @Test
    public void testOneArg() {
        assertArrayEquals(new String[] {"arg"}, p.parse("arg"));
    }

    @Test
    public void testMultipleArgs() {
        assertArrayEquals(new String[] {"one", "two", "three"}, p.parse("one two three"));
    }

    @Test
    public void testEscape() {
        // Backslash quotes a space or a backslash
        assertArrayEquals(new String[] {"one", "t w o", "three"}, p.parse("one t\\ w\\ o three"));
        assertArrayEquals(new String[] {"\\"}, p.parse("\\\\"));
    }

    @Test
    public void testQuotes() {
        // Using quote marks in various ways
        assertArrayEquals(new String[] {"one", "two", "th ree"}, p.parse("one two 'th ree'"));
        assertArrayEquals(new String[] {"o n e", "two", "three"}, p.parse("\"o n e\" two three"));
        assertArrayEquals(new String[] {"some 'funky' stuff"}, p.parse("some\" 'funky' \"stuff"));
    }

    @Test
    public void testEmptyArgs() {
        // A couple of ways of specifying arguments without any value
        assertArrayEquals(new String[] {"", "", ""}, p.parse("'' \"\" ''\"\""));
    }

    @Test
    public void testNoArgs() {
        // If the input is just whitespace, there are no arguments.
        assertArrayEquals(new String[] {}, p.parse(""));
        assertArrayEquals(new String[] {}, p.parse("    "));
        assertArrayEquals(new String[] {}, p.parse(" \r \t \n "));
    }
}
