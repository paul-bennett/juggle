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

import java.util.OptionalInt;
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

    public static BoundedType unboundedWildcardType() {
        return new BoundedType(null, null);
    }

    public boolean matchesClass(Class<?> candidate) {
        return candidate != null &&
                (lowerBound == null || candidate.isAssignableFrom(lowerBound)) &&
                (upperBound == null || upperBound.stream().allMatch(b -> b.isAssignableFrom(candidate)));
    }

    public OptionalInt scoreMatch(Class<?> candidate) {
        // Ugh, this is ugly... but does it work?
        if (candidate == null)
            return OptionalInt.of(0);
        else {
            int score = 0;
            if (lowerBound != null && lowerBound != candidate) {
                if (candidate.isAssignableFrom(lowerBound))
                    score++;
                else
                    return OptionalInt.empty();
            }
            if (upperBound != null && !upperBound.equals(Set.of(candidate))) {
                if (upperBound.stream().allMatch(b -> b.isAssignableFrom(candidate)))
                    score++;
                else
                    return OptionalInt.empty();
            }

            return OptionalInt.of(score);
        }
    }
}
