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
package com.angellane.juggle.query;

import com.angellane.juggle.match.TypeMatcher;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests constructors and simple matches for a small class hierarchy
 */
public class TestBoundedType {
    interface RootI {}
    interface Inter extends RootI {}
    static class Top {}
    static class Middle extends Top implements Inter {}
    static class Bottom extends Middle {}

    @Test
    public void testExactType() {
        BoundedType bt = BoundedType.exactType(Middle.class);

        assertFalse(bt.matchesClass(RootI.class),  "RootI /= Middle");
        assertFalse(bt.matchesClass(Inter.class),  "Inter /= Middle");
        assertFalse(bt.matchesClass(Top.class),    "Top /= Middle");
        assertTrue(bt.matchesClass(Middle.class),  "Middle == Middle");
        assertFalse(bt.matchesClass(Bottom.class), "Bottom /= Middle");
    }

    @Test
    public void testLowerBoundClass() {
        BoundedType bt = BoundedType.supertypeOf(Middle.class);

        assertTrue(bt.matchesClass(RootI.class),   "Middle -> Inter -> RootI");
        assertTrue(bt.matchesClass(Inter.class),   "Middle -> Inter");
        assertTrue(bt.matchesClass(Top.class),     "Middle => Top");
        assertTrue(bt.matchesClass(Middle.class),  "Middle == Middle");
        assertFalse(bt.matchesClass(Bottom.class), "Bottom => Middle => Top");
    }

    @Test
    public void testLowerBoundInterface() {
        BoundedType bt = BoundedType.supertypeOf(Inter.class);

        assertTrue(bt.matchesClass(RootI.class),   "Inter => RootI");
        assertTrue(bt.matchesClass(Inter.class),   "Inter == Inter");
        assertFalse(bt.matchesClass(Top.class),    "Top ~ Inter");
        assertFalse(bt.matchesClass(Middle.class), "Middle -> Inter");
        assertFalse(bt.matchesClass(Bottom.class), "Bottom => Middle -> Inter");
    }

    @Test
    public void testUpperBoundClass() {
        BoundedType bt = BoundedType.subtypeOf(Top.class);

        assertFalse(bt.matchesClass(RootI.class),  "Top ~ RootI");
        assertFalse(bt.matchesClass(Inter.class),  "Top ~ Inter");
        assertTrue(bt.matchesClass(Top.class),     "Top == Top");
        assertTrue(bt.matchesClass(Middle.class),  "Middle => Top");
        assertTrue(bt.matchesClass(Bottom.class),  "Bottom => Middle => Top");
    }

    @Test
    public void testUpperBoundInterface() {
        BoundedType bt = BoundedType.subtypeOf(Inter.class);

        assertFalse(bt.matchesClass(RootI.class),  "Inter => RootI");
        assertTrue(bt.matchesClass(Inter.class),   "Inter == Inter");
        assertFalse(bt.matchesClass(Top.class),    "Top ~ Inter");
        assertTrue(bt.matchesClass(Middle.class),  "Middle -> Inter");
        assertTrue(bt.matchesClass(Bottom.class),  "Bottom => Middle -> Inter");
    }

    @Test
    public void testFullySpecified() {
        BoundedType bt =
                new BoundedType(Set.of(Inter.class, Top.class), Bottom.class);

        assertEquals(Set.of(Inter.class, Top.class), bt.upperBound());
        assertEquals(Bottom.class,                   bt.lowerBound());

        assertFalse(bt.matchesClass(RootI.class),   "RootI");
        assertFalse(bt.matchesClass(Inter.class),   "Inter");
        assertFalse(bt.matchesClass(Top.class),     "Top");
        assertTrue(bt.matchesClass(Middle.class),   "Middle");
        assertTrue(bt.matchesClass(Bottom.class),   "Bottom");
    }


    // SCORE TESTS ============================================================

    TypeMatcher tm = new TypeMatcher(false);

    @Test
    public void testScoreExactType() {
        BoundedType bt = BoundedType.exactType(Middle.class);

        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, RootI.class),  "RootI /= Middle");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Inter.class),  "Inter /= Middle");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Top.class),    "Top /= Middle");
        assertEquals(OptionalInt.of(0),   tm.scoreTypeMatch(bt, Middle.class), "Middle == Middle");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Bottom.class), "Bottom /= Middle");
    }

    @Test
    public void testScoreLowerBoundClass() {
        BoundedType bt = BoundedType.supertypeOf(Middle.class);

        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, RootI.class),  "Middle -> Inter -> RootI");
        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, Inter.class),  "Middle -> Inter");
        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, Top.class),    "Middle => Top");
        assertEquals(OptionalInt.of(0),   tm.scoreTypeMatch(bt, Middle.class), "Middle == Middle");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Bottom.class), "Bottom => Middle => Top");
    }

    @Test
    public void testScoreLowerBoundInterface() {
        BoundedType bt = BoundedType.supertypeOf(Inter.class);

        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, RootI.class),  "Inter => RootI");
        assertEquals(OptionalInt.of(0),   tm.scoreTypeMatch(bt, Inter.class),  "Inter == Inter");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Top.class),    "Top ~ Inter");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Middle.class), "Middle -> Inter");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Bottom.class), "Bottom => Middle -> Inter");
    }

    @Test
    public void testScoreUpperBoundClass() {
        BoundedType bt = BoundedType.subtypeOf(Top.class);

        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, RootI.class),  "Top ~ RootI");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Inter.class),  "Top ~ Inter");
        assertEquals(OptionalInt.of(0),   tm.scoreTypeMatch(bt, Top.class),    "Top == Top");
        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, Middle.class), "Middle => Top");
        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, Bottom.class), "Bottom => Middle => Top");
    }

    @Test
    public void testScoreUpperBoundInterface() {
        BoundedType bt = BoundedType.subtypeOf(Inter.class);

        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, RootI.class),  "Inter => RootI");
        assertEquals(OptionalInt.of(0),   tm.scoreTypeMatch(bt, Inter.class),  "Inter == Inter");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Top.class),    "Top ~ Inter");
        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, Middle.class), "Middle -> Inter");
        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, Bottom.class), "Bottom => Middle -> Inter");
    }

    @Test
    public void testScoreFullySpecified() {
        BoundedType bt =
                new BoundedType(Set.of(Inter.class, Top.class), Bottom.class);

        assertEquals(Set.of(Inter.class, Top.class), bt.upperBound());
        assertEquals(Bottom.class,                   bt.lowerBound());

        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, RootI.class),   "RootI");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Inter.class),   "Inter");
        assertEquals(OptionalInt.empty(), tm.scoreTypeMatch(bt, Top.class),     "Top");
        assertEquals(OptionalInt.of(2),   tm.scoreTypeMatch(bt, Middle.class),  "Middle");
        assertEquals(OptionalInt.of(1),   tm.scoreTypeMatch(bt, Bottom.class),  "Bottom");
    }

}
