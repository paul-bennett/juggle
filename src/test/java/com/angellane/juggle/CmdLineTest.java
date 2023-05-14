package com.angellane.juggle;

import com.angellane.juggle.match.Accessibility;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CmdLineTest {
    @Test
    public void testNoArgs() {
        String[] args = {};

        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);

        assertEquals(List.of(), result.unmatched());

        assertNull(app.paramTypeNames);
        assertEquals(1, app.juggler.getImportedPackageNames().size());   // java.lang
        assertNull(app.returnTypeName);
        assertEquals(Accessibility.PUBLIC, app.minAccess);
        assertEquals(1, app.juggler.getSources().size());   // just the default source, java.base

        assertNull(app.paramTypeNames);
    }

    @Test
    public void testJunkArgs() {
        String[] args = {"--bad-argument"};

        Main app = new Main();

        assertThrows(CommandLine.UnmatchedArgumentException.class, () -> new CommandLine(app).parseArgs(args));
    }

    @Test
    public void testNullParamsOption() {
        String[] args = {"-p", ""};
        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.paramTypeNames);
        assertEquals(0, app.paramTypeNames.size());
    }

    @Test
    public void testOneParamsOption() {
        String[] args = {"-p", "one"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.paramTypeNames);
        assertEquals(1, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
    }

    @Test
    public void testOneCommaNullParamsOption() {
        String[] args = {"-p", "one,"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.paramTypeNames);
        assertEquals(1, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
    }

    @Test
    public void testNullCommaOneParamsOption() {
        String[] args = {"-p", ",one"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.paramTypeNames);
        assertEquals(1, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
    }

    @Test
    public void testOnePlusNullParamsOption() {
        String[] args = {"-p", "one", "-p", ""};
        Main app = new Main();
        ParseResult result = new CommandLine(app).setOverwrittenOptionsAllowed(true).parseArgs(args);

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.paramTypeNames);
        assertEquals(1, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
    }

    @Test
    public void testTwoParamsOptions() {
        String[] args = {"-p", "one", "-p", "two"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).setOverwrittenOptionsAllowed(true).parseArgs(args);

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.paramTypeNames);
        assertEquals(2, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
        assertEquals("two", app.paramTypeNames.get(1));
    }

    @Test
    public void testCommaSeparatedParamsOptions() {
        String[] args = {"-p", "one,two", "-p", "three"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).setOverwrittenOptionsAllowed(true).parseArgs(args);

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.paramTypeNames);
        assertEquals(3, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
        assertEquals("two", app.paramTypeNames.get(1));
        assertEquals("three", app.paramTypeNames.get(2));
    }

    @Test
    public void testDoubleCommaParamsOptions() {
        String[] args = {"-p", "one,,two"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.paramTypeNames);
        assertEquals(2, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
        assertEquals("two", app.paramTypeNames.get(1));
    }
}
