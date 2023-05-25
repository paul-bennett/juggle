/*
 *  Juggle -- an API search tool for Java
 *
 *  Copyright 2020,2023 Paul Bennett
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.angellane.juggle.match;

import java.lang.reflect.Modifier;

public enum Accessibility implements Comparable<Accessibility> {
    // The enumerands are arranged in order of increasing visibility.
    PRIVATE, PACKAGE, PROTECTED, PUBLIC;

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
