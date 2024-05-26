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
package com.angellane.juggle.util;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class NegatablePatternTest {
    @Test
    public void testIdenticalReferencesEqual() {
        NegatablePattern pat = new NegatablePattern("foo", true);

        assertEquals(pat, pat);
    }

    @Test
    public void testSameTestEqual() {
        NegatablePattern pat1 = new NegatablePattern("foo", true);
        NegatablePattern pat2 = new NegatablePattern("foo", true);

        assertEquals(pat1, pat2);
    }

    @Test
    public void testNegatedTestUnequal() {
        NegatablePattern pat1 = new NegatablePattern("foo", true);
        NegatablePattern pat2 = new NegatablePattern("foo", false);

        assertNotEquals(pat1, pat2);
    }

    @Test
    public void testDifferentPatternUnequal() {
        NegatablePattern pat1 = new NegatablePattern("foo", true);
        NegatablePattern pat2 = new NegatablePattern("bar", true);

        assertNotEquals(pat1, pat2);
    }

    @Test
    public void testNullUnequal() {
        NegatablePattern pat = new NegatablePattern("foo", true);

        assertNotEquals(pat, null);
        assertNotEquals(null, pat);
        assertNotEquals(null, pat);
    }

    @Test
    public void testDifferentTypesUnequal() {
        Object pat = new NegatablePattern("foo", true);
        Object str = "foo";

        assertNotEquals(pat, str);
    }

    @Test
    public void testDifferentPatternFlagsUnequal() {
        NegatablePattern pat1 = new NegatablePattern(
                Pattern.compile("foo", Pattern.CASE_INSENSITIVE), false);
        NegatablePattern pat2 = new NegatablePattern(
                Pattern.compile("foo", 0), false);

        assertNotEquals(pat1, pat2);
    }

    @Test
    public void testPositiveMatch() {
        NegatablePattern pat = new NegatablePattern("foo", true);

        assertTrue(pat.test("barfooloza"));
        assertFalse(pat.test("bar"));
        assertTrue(pat.testAll("barfooloza", "bar"));
        assertFalse(pat.testAll("baa", "baa", "black", "sheep"));
    }

    @Test
    public void testNegativeMatch() {
        NegatablePattern pat = new NegatablePattern("foo", false);

        assertFalse(pat.test("barfooloza"));
        assertTrue(pat.test("bar"));
        assertFalse(pat.testAll("barfooloza", "bar"));
        assertTrue(pat.testAll("baa", "baa", "black", "sheep"));
    }

    @Test
    public void testCaseInsensitiveMatch() {
        NegatablePattern pat = new NegatablePattern(
                Pattern.compile("foo", Pattern.CASE_INSENSITIVE), true);

        assertTrue(pat.test("foo"));
        assertTrue(pat.test("Foo"));
        assertFalse(pat.test("bar"));
    }
}
