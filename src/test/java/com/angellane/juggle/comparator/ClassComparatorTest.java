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
package com.angellane.juggle.comparator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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