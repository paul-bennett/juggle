package com.angellane.juggle.processor;

import com.angellane.juggle.processor.DeclQuery.BoundedType;
import org.junit.jupiter.api.Test;

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
}
