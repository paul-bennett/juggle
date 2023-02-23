package com.angellane.juggle.comparator;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClassComparatorTest {

    @Test
    @SuppressWarnings("EqualsWithItself")
    public void compare() {
        TypeComparator c = new TypeComparator();

        // Should compare equal if two classes are equal
        assertEquals( 0, c.compare(Object.class, Object.class));

        // String is more specific than Object
        assertEquals(-1, c.compare(String.class, Object.class));
        assertEquals(+1, c.compare(Object.class, String.class));

        // String and Double aren't assignment compatible either way
        assertEquals( 0, c.compare(String.class, Double.class));
    }
}