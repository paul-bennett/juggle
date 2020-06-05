package com.angellane.juggle;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

public class AccessibilityTest {

    @Test
    public void isAtLastAsAccessibleAsOther() {
        assertTrue (Accessibility.PUBLIC.isAtLastAsAccessibleAsOther(Accessibility.PUBLIC));
        assertTrue (Accessibility.PUBLIC.isAtLastAsAccessibleAsOther(Accessibility.PACKAGE));
        assertTrue (Accessibility.PUBLIC.isAtLastAsAccessibleAsOther(Accessibility.PROTECTED));
        assertTrue (Accessibility.PUBLIC.isAtLastAsAccessibleAsOther(Accessibility.PRIVATE));

        assertFalse(Accessibility.PACKAGE.isAtLastAsAccessibleAsOther(Accessibility.PUBLIC));
        assertTrue (Accessibility.PACKAGE.isAtLastAsAccessibleAsOther(Accessibility.PACKAGE));
        assertTrue (Accessibility.PACKAGE.isAtLastAsAccessibleAsOther(Accessibility.PROTECTED));
        assertTrue (Accessibility.PACKAGE.isAtLastAsAccessibleAsOther(Accessibility.PRIVATE));

        assertFalse(Accessibility.PROTECTED.isAtLastAsAccessibleAsOther(Accessibility.PUBLIC));
        assertFalse(Accessibility.PROTECTED.isAtLastAsAccessibleAsOther(Accessibility.PACKAGE));
        assertTrue (Accessibility.PROTECTED.isAtLastAsAccessibleAsOther(Accessibility.PROTECTED));
        assertTrue (Accessibility.PROTECTED.isAtLastAsAccessibleAsOther(Accessibility.PRIVATE));

        assertFalse(Accessibility.PRIVATE.isAtLastAsAccessibleAsOther(Accessibility.PUBLIC));
        assertFalse(Accessibility.PRIVATE.isAtLastAsAccessibleAsOther(Accessibility.PACKAGE));
        assertFalse(Accessibility.PRIVATE.isAtLastAsAccessibleAsOther(Accessibility.PROTECTED));
        assertTrue (Accessibility.PRIVATE.isAtLastAsAccessibleAsOther(Accessibility.PRIVATE));
    }

    @Test
    public void fromModifiers() {
        assertEquals(Accessibility.fromModifiers(Modifier.PUBLIC),    Accessibility.PUBLIC);
        assertEquals(Accessibility.fromModifiers(Modifier.PROTECTED), Accessibility.PROTECTED);
        assertEquals(Accessibility.fromModifiers(Modifier.PRIVATE),   Accessibility.PRIVATE);
        assertEquals(Accessibility.fromModifiers(0),                  Accessibility.PACKAGE);
    }

    @Test
    public void fromValidString() {
        assertEquals(Accessibility.fromString("PUBLIC"), Accessibility.PUBLIC);

        // Lower case variants more likely
        assertEquals(Accessibility.fromString("public"), Accessibility.PUBLIC);
        assertEquals(Accessibility.fromString("package"), Accessibility.PACKAGE);
        assertEquals(Accessibility.fromString("protected"), Accessibility.PROTECTED);
        assertEquals(Accessibility.fromString("private"), Accessibility.PRIVATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromInvalidString() {
        Accessibility.fromString("junk");
    }
}
