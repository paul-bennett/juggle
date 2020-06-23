package com.angellane.juggle;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

import static com.angellane.juggle.PermutationGenerator.PermSpliterator.permutationAfter;

public class PermutationGeneratorTest {

    /**
     * Test permutationAfter with degenerate empty array
     */
    @Test
    public void permutationAfter_0() {
        assertArrayEquals(permutationAfter(new int[] {}), new int[] {});
    }

    /**
     * Test permutationAfter with single element array
     */
    @Test
    public void permutationAfter_1() {
        assertArrayEquals(permutationAfter(new int[] {42}), new int[] {42});
    }

    /**
     * All permutations of a 2-element array
     */
    @Test
    public void permutationAfter_2() {
        assertArrayEquals(permutationAfter(new int[] {1,2}), new int[] {2,1});
        assertArrayEquals(permutationAfter(new int[] {2,1}), new int[] {1,2});
    }

    /**
     * Tests the permutationAfter method by cycling through all permutations of a three-member array.
     */
    @Test
    public void permutationAfter_3() {
        assertArrayEquals(permutationAfter(new int[] {1,2,3}), new int[] {1,3,2});
        assertArrayEquals(permutationAfter(new int[] {1,3,2}), new int[] {2,1,3});
        assertArrayEquals(permutationAfter(new int[] {2,1,3}), new int[] {2,3,1});
        assertArrayEquals(permutationAfter(new int[] {2,3,1}), new int[] {3,1,2});
        assertArrayEquals(permutationAfter(new int[] {3,1,2}), new int[] {3,2,1});
        assertArrayEquals(permutationAfter(new int[] {2,1,0}), new int[] {0,1,2});  // Note: should wrap to start
    }

    /**
     * Test example Knuth gives for Algorithm L:
     * 1223, 1232, 1322, 2123, 2132, 2213, 2231, 2312, 2321, 3122, 3212, 3221
     */
    @Test
    public void permutationAfter_Knuth() {
        assertArrayEquals(permutationAfter(new int[] {1,2,2,3}), new int[] {1,2,3,2});
        assertArrayEquals(permutationAfter(new int[] {1,2,3,2}), new int[] {1,3,2,2});
        assertArrayEquals(permutationAfter(new int[] {1,3,2,2}), new int[] {2,1,2,3});
        assertArrayEquals(permutationAfter(new int[] {2,1,2,3}), new int[] {2,1,3,2});
        assertArrayEquals(permutationAfter(new int[] {2,1,3,2}), new int[] {2,2,1,3});
        assertArrayEquals(permutationAfter(new int[] {2,2,1,3}), new int[] {2,2,3,1});
        assertArrayEquals(permutationAfter(new int[] {2,2,3,1}), new int[] {2,3,1,2});
        assertArrayEquals(permutationAfter(new int[] {2,3,1,2}), new int[] {2,3,2,1});
        assertArrayEquals(permutationAfter(new int[] {2,3,2,1}), new int[] {3,1,2,2});
        assertArrayEquals(permutationAfter(new int[] {3,1,2,2}), new int[] {3,2,1,2});
        assertArrayEquals(permutationAfter(new int[] {3,2,1,2}), new int[] {3,2,2,1});
        assertArrayEquals(permutationAfter(new int[] {3,2,2,1}), new int[] {1,2,2,3});  // Cycle back to start
    }

    @Test
    public void permutationStream_sequential() {
        var input = List.of("one", "two", "three", "four");

        PermutationGenerator<String> gen = new PermutationGenerator<>(input);

        var perms = gen.stream().collect(Collectors.toList());

        assertEquals(input.size(), 4);

        // Should be 4! permutations, assuming 4 distinct input elements.
        assertEquals(perms.size(), 4*3*2*1);

        // There should be no duplicates
        assertEquals(perms.stream().distinct().count(), perms.size());
    }

    @Test
    public void permutationStream_parallel() {
        var input = List.of("one", "two", "three", "four");

        PermutationGenerator<String> gen = new PermutationGenerator<>(input);

        var perms = gen.stream()
                .parallel()
//                .peek(l -> System.err.println(l + " on " + Thread.currentThread()))
                .collect(Collectors.toList());

        assertEquals(input.size(), 4);

        // Should be 4! permutations, assuming 4 distinct input elements.
        assertEquals(perms.size(), 4*3*2*1);

        // There should be no duplicates
        assertEquals(perms.stream().distinct().count(), perms.size());
    }

    @Test
    public void permutationCollector_sequential() {
        var input = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);

        List<List<Integer>> perms = input.stream()
                .collect(PermutationGenerator.collector())
                .stream()
                .collect(Collectors.toList());

        assertEquals(perms.size(), 10*9*8*7*6*5*4*3*2*1);
    }

    @Test
    public void permutationCollector_parallel() {
        var input = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);

        List<List<Integer>> perms = input.stream()
                .parallel()
                .collect(PermutationGenerator.collector())
                .stream()
                .parallel()
                .collect(Collectors.toList());

        assertEquals(perms.size(), 10*9*8*7*6*5*4*3*2*1);
    }
}