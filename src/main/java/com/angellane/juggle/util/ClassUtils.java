package com.angellane.juggle.util;

import com.angellane.backport.jdk17.java.lang.ClassExtras;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

public final class ClassUtils {
    private ClassUtils() {}

    public static boolean classIsFinal(Class<?> c) {
        return (c.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
    }
    public static boolean classIsSealed(Class<?> c) {
        return ClassExtras.isSealed(c);
    }

    public static boolean classIsNonSealed(Class<?> c) {
        return !ClassExtras.isSealed(c)
                && !classIsFinal(c)
                && Stream.concat(
                        Stream.ofNullable(c.getSuperclass()),
                        Arrays.stream(c.getInterfaces())
                ).anyMatch(ClassExtras::isSealed);
    }
}
