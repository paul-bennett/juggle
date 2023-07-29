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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQuery {
    @Test
    public void testTotalValid() {
        assertEquals(OptionalInt.of(10),
                Query.totalScore(Arrays.asList(
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
                Query.totalScore(Arrays.asList(
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
        assertEquals(OptionalInt.of(0), Query.totalScore(Collections.emptyList()));
    }

    @Test
    public void testTotalSingletonEmpty() {
        assertEquals(OptionalInt.empty(),
                Query.totalScore(Collections.singletonList(
                        OptionalInt.empty()
                )));
    }

    @Test
    public void testTotalSingletonValid() {
        assertEquals(OptionalInt.of(42),
                Query.totalScore(Collections.singletonList(
                        OptionalInt.of(42)
                )));
    }

}
