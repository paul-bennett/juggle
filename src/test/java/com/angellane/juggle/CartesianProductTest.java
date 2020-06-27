package com.angellane.juggle;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CartesianProductTest {
    /**
     * A Collector that concatenates characters into a String.
     */
    Collector<Character, StringBuilder, String> concatenator = new Collector<>() {
        @Override public Supplier<StringBuilder>                supplier()          { return StringBuilder::new;      }
        @Override public BiConsumer<StringBuilder,Character>    accumulator()       { return StringBuilder::append;   }
        @Override public BinaryOperator<StringBuilder>          combiner()          { return StringBuilder::append;   }
        @Override public Function<StringBuilder,String>         finisher()          { return StringBuilder::toString; }

        @Override public Set<Characteristics>                   characteristics()   { return Set.of();                }
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
        List<Character> l1 = List.of('a', 'b', 'c', 'd');

        List<String> product = CartesianProduct.of(l1)
                .stream()
                .map(l -> l.stream().collect(concatenator))
                .collect(Collectors.toList());

        assertEquals(l1.size(), product.size());
        assertEquals(List.of("a", "b", "c", "d"), product);
    }

    @Test
    public void testTwo() {
        List<Character> l1 = List.of('a', 'b', 'c');
        List<Character> l2 = List.of('1', '2', '3');

        List<String> product = CartesianProduct.of(l1, l2)
                .stream()
                .map(l -> l.stream().collect(concatenator))
                .collect(Collectors.toList());

        assertEquals(l1.size() * l2.size(), product.size());
        assertEquals(List.of("a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3"), product);
    }

    @Test
    public void testThree() {
        List<Character> l1 = List.of('a', 'b');
        List<Character> l2 = List.of('1', '2');
        List<Character> l3 = List.of('A', 'B');

        List<String> product = CartesianProduct.of(l1, l2, l3)
                .stream()
                .map(l -> l.stream().collect(concatenator))
//                .peek(s -> System.out.println("Result: " + s + ", on " + Thread.currentThread()))
                .collect(Collectors.toList());

        assertEquals(l1.size() * l2.size() * l3.size(), product.size());
        assertEquals(List.of("a1A", "a1B", "a2A", "a2B", "b1A", "b1B", "b2A", "b2B"), product);
    }

    @Test
    public void testThreeParallel() {
        List<Character> l1 = List.of('a', 'b');
        List<Character> l2 = List.of('1', '2');
        List<Character> l3 = List.of('A', 'B');

        List<String> product = CartesianProduct.of(l1, l2, l3)
                .stream()
                .parallel()
                .map(l -> l.stream().collect(concatenator))
//                .peek(s -> System.out.println("Result: " + s + ", on " + Thread.currentThread()))
                .collect(Collectors.toList());

        assertEquals(l1.size() * l2.size() * l3.size(), product.size());
        assertEquals(List.of("a1A", "a1B", "a2A", "a2B", "b1A", "b1B", "b2A", "b2B"), product);
    }
}