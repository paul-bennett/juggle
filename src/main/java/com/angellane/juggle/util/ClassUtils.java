package com.angellane.juggle.util;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

public final class ClassUtils {
    private ClassUtils() {}

    public static boolean classIsFinal(Class<?> c) {
        return (c.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
    }
    public static boolean classIsSealed(Class<?> c) {
        return c.isSealed();
    }

    public static boolean classIsNonSealed(Class<?> c) {
        return !c.isSealed()
                && !classIsFinal(c)
                && Stream.concat(
                        Stream.ofNullable(c.getSuperclass()),
                        Arrays.stream(c.getInterfaces())
                ).anyMatch(Class::isSealed);
    }
}
