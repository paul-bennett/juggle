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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartesianProductTest {
    /**
     * A Collector that concatenates characters into a String.
     */
    final Collector<Character, StringBuilder, String> concatenator = new Collector<Character, StringBuilder, String>() {
        @Override public Supplier<StringBuilder>                supplier()          { return StringBuilder::new;      }
        @Override public BiConsumer<StringBuilder,Character>    accumulator()       { return StringBuilder::append;   }
        @Override public BinaryOperator<StringBuilder>          combiner()          { return StringBuilder::append;   }
        @Override public Function<StringBuilder,String>         finisher()          { return StringBuilder::toString; }

        @Override public Set<Characteristics>                   characteristics()   { return Collections.emptySet();  }
    };

    @Test
    public void testNone() {
        long count = CartesianProduct.of()
                .stream()
                .count();

        assertEquals(0L, count);
    }

    @Test
    public void testOne() {
        List<Character> l1 = Arrays.asList('a', 'b', 'c', 'd');

        List<String> product = CartesianProduct.of(l1)
                .stream()
                .map(l -> l.stream().collect(concatenator))
                .collect(Collectors.toList());

        assertEquals(l1.size(), product.size());
        assertEquals(Arrays.asList("a", "b", "c", "d"), product);
    }

    @Test
    public void testTwo() {
        List<Character> l1 = Arrays.asList('a', 'b', 'c');
        List<Character> l2 = Arrays.asList('1', '2', '3');

        List<String> product = CartesianProduct.of(l1, l2)
                .stream()
                .map(l -> l.stream().collect(concatenator))
                .collect(Collectors.toList());

        assertEquals(l1.size() * l2.size(), product.size());
        assertEquals(Arrays.asList("a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3"), product);
    }

    @Test
    public void testThree() {
        List<Character> l1 = Arrays.asList('a', 'b');
        List<Character> l2 = Arrays.asList('1', '2');
        List<Character> l3 = Arrays.asList('A', 'B');

        List<String> product = CartesianProduct.of(l1, l2, l3)
                .stream()
                .map(l -> l.stream().collect(concatenator))
//                .peek(s -> System.out.println("Result: " + s + ", on " + Thread.currentThread()))
                .collect(Collectors.toList());

        assertEquals(l1.size() * l2.size() * l3.size(), product.size());
        assertEquals(Arrays.asList("a1A", "a1B", "a2A", "a2B", "b1A", "b1B", "b2A", "b2B"), product);
    }

    @Test
    public void testThreeParallel() {
        List<Character> l1 = Arrays.asList('a', 'b');
        List<Character> l2 = Arrays.asList('1', '2');
        List<Character> l3 = Arrays.asList('A', 'B');

        List<String> product = CartesianProduct.of(l1, l2, l3)
                .stream()
                .parallel()
                .map(l -> l.stream().collect(concatenator))
//                .peek(s -> System.out.println("Result: " + s + ", on " + Thread.currentThread()))
                .collect(Collectors.toList());

        assertEquals(l1.size() * l2.size() * l3.size(), product.size());
        assertEquals(Arrays.asList("a1A", "a1B", "a2A", "a2B", "b1A", "b1B", "b2A", "b2B"), product);
    }
}