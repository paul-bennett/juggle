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
        assertEquals(Accessibility.PUBLIC,    Accessibility.fromModifiers(Modifier.PUBLIC));
        assertEquals(Accessibility.PROTECTED, Accessibility.fromModifiers(Modifier.PROTECTED));
        assertEquals(Accessibility.PRIVATE,   Accessibility.fromModifiers(Modifier.PRIVATE));
        assertEquals(Accessibility.PACKAGE,   Accessibility.fromModifiers(0));
    }

    @Test
    public void fromValidString() {
        assertEquals(Accessibility.PUBLIC, Accessibility.fromString("PUBLIC"));

        // Lower case variants more likely
        assertEquals(Accessibility.PUBLIC,    Accessibility.fromString("public"));
        assertEquals(Accessibility.PACKAGE,   Accessibility.fromString("package"));
        assertEquals(Accessibility.PROTECTED, Accessibility.fromString("protected"));
        assertEquals(Accessibility.PRIVATE,   Accessibility.fromString("private"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromInvalidString() {
        Accessibility.fromString("junk");
    }
}