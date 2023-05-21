package com.angellane.juggle.query;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQuery {
    @Test
    public void testTotalValid() {
        assertEquals(OptionalInt.of(10),
                Query.totalScore(List.of(
                        OptionalInt.of(0),
                        OptionalInt.of(1),
                        OptionalInt.of(2),
                        OptionalInt.of(3),
                        OptionalInt.of(4)
                )));
    }

    @Test
    public void testTotalInvalid() {
        assertEquals(OptionalInt.empty(),
                Query.totalScore(List.of(
                        OptionalInt.of(0),
                        OptionalInt.of(1),
                        OptionalInt.empty(),
                        OptionalInt.of(2),
                        OptionalInt.of(3),
                        OptionalInt.of(4)
                )));
    }

    @Test
    public void testTotalEmpty() {
        assertEquals(OptionalInt.of(0), Query.totalScore(List.of()));
    }

    @Test
    public void testTotalSingletonEmpty() {
        assertEquals(OptionalInt.empty(),
                Query.totalScore(List.of(
                        OptionalInt.empty()
                )));
    }

    @Test
    public void testTotalSingletonValid() {
        assertEquals(OptionalInt.of(42),
                Query.totalScore(List.of(
                        OptionalInt.of(42)
                )));
    }

}
