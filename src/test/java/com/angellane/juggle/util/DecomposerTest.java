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

import java.util.ArrayList;

import static com.angellane.juggle.util.Decomposer.decomposeIntoParts;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class DecomposerTest {
    private int[][] getDecompositions(int num, int parts) {
        ArrayList<int[]> acc = new ArrayList<>();

        decomposeIntoParts(num, parts, acc::add);

        return acc.toArray(new int[][]{});
    }

    @Test
    public void testDecompose1Into1Part1() {
        assertArrayEquals(new int[][]{
                {1}
        }, getDecompositions(1, 1));
    }

    @Test
    public void testDecompose1Into2Parts() {
        assertArrayEquals(new int[][]{
                {1, 0}, {0, 1}
        }, getDecompositions(1, 2));
    }

    @Test
    public void testDecompose1Into3Parts() {
        assertArrayEquals(new int[][]{
                {1, 0, 0}, {0, 1, 0}, {0, 0, 1}
        }, getDecompositions(1, 3));
    }

    @Test
    public void testDecompose1Into4Parts() {
        assertArrayEquals(new int[][]{
                {1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}
        }, getDecompositions(1, 4));
    }

    @Test
    public void testDecompose2Into1Part() {
        assertArrayEquals(new int[][]{
                {2}
        }, getDecompositions(2,1));
    }

    @Test
    public void testDecompose2Into2Parts() {
        assertArrayEquals(new int[][]{
                {2,0}, {1,1}, {0,2}
        }, getDecompositions(2,2));
    }

    @Test
    public void testDecompose2Into3Parts() {
        assertArrayEquals(new int[][]{
                {2,0,0}, {1,1,0}, {1,0,1}, {0,2,0}, {0,1,1},
                {0,0,2}
        }, getDecompositions(2,3));
    }

    @Test
    public void testDecompose2Into4Parts() {
        assertArrayEquals(new int[][]{
                {2,0,0,0}, {1,1,0,0}, {1,0,1,0}, {1,0,0,1}, {0,2,0,0},
                {0,1,1,0}, {0,1,0,1}, {0,0,2,0}, {0,0,1,1}, {0,0,0,2}
        }, getDecompositions(2,4));
    }

    @Test
    public void testDecompose3Into1Part() {
        assertArrayEquals(new int[][]{
                {3}
        }, getDecompositions(3,1));
    }

    @Test
    public void testDecompose3Into2Parts() {
        assertArrayEquals(new int[][]{
                {3,0}, {2,1}, {1,2}, {0,3}
        }, getDecompositions(3,2));
    }

    @Test
    public void testDecompose3Into3Parts() {
        assertArrayEquals(new int[][]{
                {3,0,0}, {2,1,0}, {2,0,1}, {1,2,0}, {1,1,1},
                {1,0,2}, {0,3,0}, {0,2,1}, {0,1,2}, {0,0,3}
        }, getDecompositions(3,3));
    }

    @Test
    public void testDecompose3Into4Parts() {
        assertArrayEquals(new int[][]{
                {3,0,0,0}, {2,1,0,0}, {2,0,1,0}, {2,0,0,1}, {1,2,0,0},
                {1,1,1,0}, {1,1,0,1}, {1,0,2,0}, {1,0,1,1}, {1,0,0,2},
                {0,3,0,0}, {0,2,1,0}, {0,2,0,1}, {0,1,2,0}, {0,1,1,1},
                {0,1,0,2}, {0,0,3,0}, {0,0,2,1}, {0,0,1,2}, {0,0,0,3}
        }, getDecompositions(3,4));
    }

    @Test
    public void testDecompose4Into1Part() {
        assertArrayEquals(new int[][]{
                {4}
        }, getDecompositions(4,1));
    }

    @Test
    public void testDecompose4Into2Parts() {
        assertArrayEquals(new int[][]{
                {4,0}, {3,1}, {2,2}, {1,3}, {0,4}
        }, getDecompositions(4,2));
    }

    @Test
    public void testDecompose4Into3Parts() {
        assertArrayEquals(new int[][]{
                {4,0,0}, {3,1,0}, {3,0,1}, {2,2,0}, {2,1,1},
                {2,0,2}, {1,3,0}, {1,2,1}, {1,1,2}, {1,0,3},
                {0,4,0}, {0,3,1}, {0,2,2}, {0,1,3}, {0,0,4}
        }, getDecompositions(4,3));
    }

    @Test
    public void testDecompose4Into4Parts() {
        assertArrayEquals(new int[][]{
                {4,0,0,0}, {3,1,0,0}, {3,0,1,0}, {3,0,0,1}, {2,2,0,0},
                {2,1,1,0}, {2,1,0,1}, {2,0,2,0}, {2,0,1,1}, {2,0,0,2},
                {1,3,0,0}, {1,2,1,0}, {1,2,0,1}, {1,1,2,0}, {1,1,1,1},
                {1,1,0,2}, {1,0,3,0}, {1,0,2,1}, {1,0,1,2}, {1,0,0,3},
                {0,4,0,0}, {0,3,1,0}, {0,3,0,1}, {0,2,2,0}, {0,2,1,1},
                {0,2,0,2}, {0,1,3,0}, {0,1,2,1}, {0,1,1,2}, {0,1,0,3},
                {0,0,4,0}, {0,0,3,1}, {0,0,2,2}, {0,0,1,3}, {0,0,0,4}
        }, getDecompositions(4,4));
    }
}
