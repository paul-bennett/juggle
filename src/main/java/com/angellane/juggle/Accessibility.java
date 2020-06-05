package com.angellane.juggle;

import java.lang.reflect.Modifier;

public enum Accessibility implements Comparable<Accessibility> {
    // The enumerands are arranged in order of increasing visibility.
    PRIVATE, PROTECTED, PACKAGE, PUBLIC;

    public static Accessibility fromModifiers(int modifiers) {
             if (Modifier.PRIVATE   == (modifiers & Modifier.PRIVATE))      return PRIVATE;
        else if (Modifier.PROTECTED == (modifiers & Modifier.PROTECTED))    return PROTECTED;
        else if (Modifier.PUBLIC    == (modifiers & Modifier.PUBLIC))       return PUBLIC;
        else                                                                return PACKAGE;
    }

    public static Accessibility fromString(String s) {
        return valueOf(s.toUpperCase());
    }

    boolean isAtLastAsAccessibleAsOther(Accessibility other) {
        // Note: relies on enumerands being declared in order of increasing visibility.
        return this.compareTo(other) >= 0;
    }
}
