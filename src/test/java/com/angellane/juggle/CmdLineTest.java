package com.angellane.juggle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CmdLineTest {
    @Test
    public void testNoArgs() {
        String[] args = {};

        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);

        assertNull(app.paramTypeNames);
        assertFalse(app.helpRequested);
        assertEquals(1, app.juggler.getImportedPackageNames().size());   // java.lang
        assertNull(app.returnTypeName);
        assertEquals(0, app.juggler.getJarNames().size());
        assertEquals(Accessibility.PUBLIC, app.minAccess);
        assertEquals(0, app.juggler.getModuleNames().size());
    }

    @Test
    public void testNoParamsOption() {
        String[] args = {};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNull(app.paramTypeNames);
    }

    @Test
    public void testNullParamsOption() {
        String[] args = {"-p", ""};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypeNames);
        assertEquals(0, app.paramTypeNames.size());
    }

    @Test
    public void testOneParamsOption() {
        String[] args = {"-p", "one"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypeNames);
        assertEquals(1, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
    }

    @Test
    public void testOneCommaNullParamsOption() {
        String[] args = {"-p", "one,"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypeNames);
        assertEquals(1, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
    }

    @Test
    public void testNullCommaOneParamsOption() {
        String[] args = {"-p", ",one"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypeNames);
        assertEquals(1, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
    }

    @Test
    public void testOnePlusNullParamsOption() {
        String[] args = {"-p", "one", "-p", ""};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypeNames);
        assertEquals(1, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
    }

    @Test
    public void testTwoParamsOptions() {
        String[] args = {"-p", "one", "-p", "two"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypeNames);
        assertEquals(2, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
        assertEquals("two", app.paramTypeNames.get(1));
    }

    @Test
    public void testCommaSeparatedParamsOptions() {
        String[] args = {"-p", "one,two", "-p", "three"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
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
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypeNames);
        assertEquals(2, app.paramTypeNames.size());
        assertEquals("one", app.paramTypeNames.get(0));
        assertEquals("two", app.paramTypeNames.get(1));
    }
}
