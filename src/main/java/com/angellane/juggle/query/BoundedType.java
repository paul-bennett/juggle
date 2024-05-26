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
package com.angellane.juggle.query;

import java.util.List;
import java.util.Set;

public record BoundedType(
        Set<Class<?>> upperBound,   // extends/implements
        Class<?> lowerBound         // super
) {
    public static BoundedType exactType(Class<?> c) {
        return new BoundedType(Set.of(c), c);
    }

    public static BoundedType subtypeOf(Class<?>... cs) {
        return new BoundedType(Set.of(cs), null);
    }

    public static BoundedType subtypeOf(List<Class<?>> cs) {
        return subtypeOf(cs.toArray(new Class<?>[0]));
    }

    public static BoundedType supertypeOf(Class<?> c) {
        return new BoundedType(null, c);
    }

    public static BoundedType unboundedWildcardType() {
        return new BoundedType(null, null);
    }

    public boolean matchesClass(Class<?> candidate) {
        return candidate != null &&
                (lowerBound == null || candidate.isAssignableFrom(lowerBound)) &&
                (upperBound == null || upperBound.stream().allMatch(b -> b.isAssignableFrom(candidate)));
    }

    public boolean isPrimitive() {
        return lowerBound() != null
                && upperBound() != null
                && lowerBound().isPrimitive()
                && upperBound().stream().allMatch(Class::isPrimitive);
    }

    public boolean isExactType() {
        return lowerBound() != null
                && upperBound() != null
                && upperBound().equals(Set.of(lowerBound()));
    }

    public boolean isBoundedWildcard() {
        return upperBound() == null && lowerBound() != null
                || upperBound() != null && lowerBound() == null;
    }

    public boolean isUnboundedWildcard() {
        return upperBound() == null && lowerBound() == null;
    }

    public static boolean isUnboundedWildcard(BoundedType bt) {
        return bt == null || bt.isUnboundedWildcard();
    }
}
