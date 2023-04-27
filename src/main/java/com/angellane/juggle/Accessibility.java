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

    /**
     * Compares two Accessibility objects for permissiveness.
     * <p>
     * Does this Accessibility object represent an access level that is more permissive than other?
     * For example, if this is PROTECTED and OTHER is PRIVATE then this is more accessible than other.
     * </p>
     * @param other Accessibility value to compare with
     * @return true if `this` is at least accessible as `other`
     */
    public boolean isAtLeastAsAccessibleAsOther(Accessibility other) {
        // Note: relies on enumerands being declared in order of increasing visibility.
        return this.compareTo(other) >= 0;
    }
}
