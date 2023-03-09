package com.angellane.juggle;

import org.junit.Test;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;

public class TestRegressions {

    /* https://github.com/paul-bennett/juggle/issues/1
     * "Searching (with -p or -r) for an array of a primitive type falls back to Object"
     */
    @Test
    public void testParamDoubleArray() throws NoSuchMethodException {
        Juggler juggler = new Juggler(List.of(), List.of(), List.of());

        var query = new TypeSignature(
                Stream.of("double[]", "int", "int", "double")
                        .map(juggler::classForTypename)
                        .collect(Collectors.toList()
                        ),
                juggler.classForTypename("void"),
                Set.of(),
                Set.of()
                );
        var actual = juggler.findMembers(Accessibility.PUBLIC, query);

        var expected = new Member[] {
                Arrays.class.getMethod("fill",
                    Double.TYPE.arrayType(), Integer.TYPE, Integer.TYPE, Double.TYPE)
        };

        assertArrayEquals(expected, actual);
    }
}
