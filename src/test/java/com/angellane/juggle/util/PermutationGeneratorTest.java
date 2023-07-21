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
package com.angellane.juggle.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.angellane.juggle.util.PermutationGenerator.PermSpliterator.permutationAfter;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PermutationGeneratorTest {

    /**
     * Test permutationAfter with degenerate empty array
     */
    @Test
    public void permutationAfter_0() {
        assertArrayEquals(new int[] {}, permutationAfter(new int[] {}));
    }

    /**
     * Test permutationAfter with single element array
     */
    @Test
    public void permutationAfter_1() {
        assertArrayEquals(new int[] {42}, permutationAfter(new int[] {42}));
    }

    /**
     * All permutations of a 2-element array
     */
    @Test
    public void permutationAfter_2() {
        assertArrayEquals(new int[] {2,1}, permutationAfter(new int[] {1,2}));
        assertArrayEquals(new int[] {1,2}, permutationAfter(new int[] {2,1}));
    }

    /**
     * Tests the permutationAfter method by cycling through all permutations of a three-member array.
     */
    @Test
    public void permutationAfter_3() {
        assertArrayEquals(new int[] {1,3,2}, permutationAfter(new int[] {1,2,3}));
        assertArrayEquals(new int[] {2,1,3}, permutationAfter(new int[] {1,3,2}));
        assertArrayEquals(new int[] {2,3,1}, permutationAfter(new int[] {2,1,3}));
        assertArrayEquals(new int[] {3,1,2}, permutationAfter(new int[] {2,3,1}));
        assertArrayEquals(new int[] {3,2,1}, permutationAfter(new int[] {3,1,2}));
        assertArrayEquals(new int[] {0,1,2}, permutationAfter(new int[] {2,1,0}));  // Note: should wrap to start
    }

    /**
     * Test example Knuth gives for Algorithm L:
     * 1223, 1232, 1322, 2123, 2132, 2213, 2231, 2312, 2321, 3122, 3212, 3221
     */
    @Test
    public void permutationAfter_Knuth() {
        assertArrayEquals(new int[] {1,2,3,2}, permutationAfter(new int[] {1,2,2,3}));
        assertArrayEquals(new int[] {1,3,2,2}, permutationAfter(new int[] {1,2,3,2}));
        assertArrayEquals(new int[] {2,1,2,3}, permutationAfter(new int[] {1,3,2,2}));
        assertArrayEquals(new int[] {2,1,3,2}, permutationAfter(new int[] {2,1,2,3}));
        assertArrayEquals(new int[] {2,2,1,3}, permutationAfter(new int[] {2,1,3,2}));
        assertArrayEquals(new int[] {2,2,3,1}, permutationAfter(new int[] {2,2,1,3}));
        assertArrayEquals(new int[] {2,3,1,2}, permutationAfter(new int[] {2,2,3,1}));
        assertArrayEquals(new int[] {2,3,2,1}, permutationAfter(new int[] {2,3,1,2}));
        assertArrayEquals(new int[] {3,1,2,2}, permutationAfter(new int[] {2,3,2,1}));
        assertArrayEquals(new int[] {3,2,1,2}, permutationAfter(new int[] {3,1,2,2}));
        assertArrayEquals(new int[] {3,2,2,1}, permutationAfter(new int[] {3,2,1,2}));
        assertArrayEquals(new int[] {1,2,2,3}, permutationAfter(new int[] {3,2,2,1}));  // Cycle back to start
    }

    @Test
    public void permutationStream_sequential() {
        var input = List.of("one", "two", "three", "four");

        PermutationGenerator<String> gen = new PermutationGenerator<>(input);

        var perms = gen.stream().collect(Collectors.toList());

        assertEquals(4, input.size());

        // Should be size! permutations, assuming distinct input elements.
        assertEquals(factorial(input.size()), perms.size());

        // There should be no duplicates
        assertEquals(perms.size(), perms.stream().distinct().count());
    }

    @Test
    public void permutationStream_parallel() {
        var input = List.of("one", "two", "three", "four");

        PermutationGenerator<String> gen = new PermutationGenerator<>(input);

        var perms = gen.stream()
                .parallel()
//                .peek(l -> System.err.println(l + " on " + Thread.currentThread()))
                .collect(Collectors.toList());

        assertEquals(4, input.size());

        // Should be 4! permutations, assuming 4 distinct input elements.
        assertEquals(factorial(input.size()), perms.size());

        // There should be no duplicates
        assertEquals(perms.size(), perms.stream().distinct().count());
    }

    @Test
    public void permutationCollector_sequential() {
        var input = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);

        List<List<Integer>> perms = input.stream()
                .collect(PermutationGenerator.collector())
                .stream()
                .collect(Collectors.toList());

        assertEquals(factorial(input.size()), perms.size());
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

        assertEquals(factorial(input.size()), perms.size());
    }

    /** Compute the factorial of the argument */
    private int factorial(int i) { return i == 1 ? 1 : i * factorial(i-1); }
}