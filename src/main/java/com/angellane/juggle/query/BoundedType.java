package com.angellane.juggle.query;

import java.util.Set;

public record BoundedType(
        Set<Class<?>> upperBound,   // extends/implements
        Class<?> lowerBound         // super
) {
    public static BoundedType exactType(Class<?> c) {
        return new BoundedType(Set.of(c), c);
    }

    public static BoundedType subtypeOf(Class<?> c) {
        return new BoundedType(Set.of(c), null);
    }

    public static BoundedType subtypeOf(Set<Class<?>> cs) {
        return new BoundedType(cs, null);
    }

    public static BoundedType subtypeOf(Class<?>... cs) {
        return new BoundedType(Set.of(cs), null);
    }

    public static BoundedType supertypeOf(Class<?> c) {
        return new BoundedType(null, c);
    }

    public static BoundedType wildcardType() {
        return new BoundedType(null, null);
    }

    public boolean matchesClass(Class<?> candidate) {
        return candidate != null &&
                (lowerBound == null || candidate.isAssignableFrom(lowerBound)) &&
                (upperBound == null || upperBound.stream().allMatch(b -> b.isAssignableFrom(candidate)));
    }
}
