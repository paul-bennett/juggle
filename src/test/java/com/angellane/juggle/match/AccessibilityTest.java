/*
 *  Juggle -- a declarative search tool for Java
 *
 *  Copyright 2020,2024 Paul Bennett
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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

public class AccessibilityTest {

    @Test
    public void isAtLastAsAccessibleAsOther() {
        assertTrue (Accessibility.PUBLIC.isAtLeastAsAccessibleAsOther(Accessibility.PUBLIC));
        assertTrue (Accessibility.PUBLIC.isAtLeastAsAccessibleAsOther(Accessibility.PROTECTED));
        assertTrue (Accessibility.PUBLIC.isAtLeastAsAccessibleAsOther(Accessibility.PACKAGE));
        assertTrue (Accessibility.PUBLIC.isAtLeastAsAccessibleAsOther(Accessibility.PRIVATE));

        assertFalse(Accessibility.PROTECTED.isAtLeastAsAccessibleAsOther(Accessibility.PUBLIC));
        assertTrue (Accessibility.PROTECTED.isAtLeastAsAccessibleAsOther(Accessibility.PROTECTED));
        assertTrue (Accessibility.PROTECTED.isAtLeastAsAccessibleAsOther(Accessibility.PACKAGE));
        assertTrue (Accessibility.PROTECTED.isAtLeastAsAccessibleAsOther(Accessibility.PRIVATE));

        assertFalse(Accessibility.PACKAGE.isAtLeastAsAccessibleAsOther(Accessibility.PUBLIC));
        assertFalse(Accessibility.PACKAGE.isAtLeastAsAccessibleAsOther(Accessibility.PROTECTED));
        assertTrue (Accessibility.PACKAGE.isAtLeastAsAccessibleAsOther(Accessibility.PACKAGE));
        assertTrue (Accessibility.PACKAGE.isAtLeastAsAccessibleAsOther(Accessibility.PRIVATE));

        assertFalse(Accessibility.PRIVATE.isAtLeastAsAccessibleAsOther(Accessibility.PUBLIC));
        assertFalse(Accessibility.PRIVATE.isAtLeastAsAccessibleAsOther(Accessibility.PROTECTED));
        assertFalse(Accessibility.PRIVATE.isAtLeastAsAccessibleAsOther(Accessibility.PACKAGE));
        assertTrue (Accessibility.PRIVATE.isAtLeastAsAccessibleAsOther(Accessibility.PRIVATE));
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

    @Test
    public void fromInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> Accessibility.fromString("junk"));
    }
}