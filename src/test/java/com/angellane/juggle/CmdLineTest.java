package com.angellane.juggle;

import org.junit.Test;

import static org.junit.Assert.*;

public class CmdLineTest {
    @Test
    public void testNoArgs() {
        String[] args = {};

        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);

        assertNull(app.paramTypes);
        assertFalse(app.helpRequested);
        assertEquals(0, app.importPackageNames.size());
        assertNull(app.returnType);
        assertEquals(0, app.jarPaths.size());
        assertEquals(Accessibility.PUBLIC, app.minAccess);
        assertEquals(0, app.moduleNames.size());
    }

    @Test
    public void testNoParamsOption() {
        String[] args = {};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNull(app.paramTypes);
    }

    @Test
    public void testNullParamsOption() {
        String[] args = {"-p", ""};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypes);
        assertEquals(0, app.paramTypes.size());
    }

    @Test
    public void testOneParamsOption() {
        String[] args = {"-p", "one"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypes);
        assertEquals(1, app.paramTypes.size());
        assertEquals("one", app.paramTypes.get(0));
    }

    @Test
    public void testOneCommaNullParamsOption() {
        String[] args = {"-p", "one,"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypes);
        assertEquals(1, app.paramTypes.size());
        assertEquals("one", app.paramTypes.get(0));
    }

    @Test
    public void testNullCommaOneParamsOption() {
        String[] args = {"-p", ",one"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypes);
        assertEquals(1, app.paramTypes.size());
        assertEquals("one", app.paramTypes.get(0));
    }

    @Test
    public void testOnePlusNullParamsOption() {
        String[] args = {"-p", "one", "-p", ""};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypes);
        assertEquals(1, app.paramTypes.size());
        assertEquals("one", app.paramTypes.get(0));
    }

    @Test
    public void testTwoParamsOptions() {
        String[] args = {"-p", "one", "-p", "two"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypes);
        assertEquals(2, app.paramTypes.size());
        assertEquals("one", app.paramTypes.get(0));
        assertEquals("two", app.paramTypes.get(1));
    }

    @Test
    public void testCommaSeparatedParamsOptions() {
        String[] args = {"-p", "one,two", "-p", "three"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypes);
        assertEquals(3, app.paramTypes.size());
        assertEquals("one", app.paramTypes.get(0));
        assertEquals("two", app.paramTypes.get(1));
        assertEquals("three", app.paramTypes.get(2));
    }

    @Test
    public void testDoubleCommaParamsOptions() {
        String[] args = {"-p", "one,,two"};
        Main app = new Main();
        boolean parseResult = app.parseArgs(args);

        assertTrue(parseResult);
        assertNotNull(app.paramTypes);
        assertEquals(2, app.paramTypes.size());
        assertEquals("one", app.paramTypes.get(0));
        assertEquals("two", app.paramTypes.get(1));
    }
}
