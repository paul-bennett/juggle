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
package com.angellane.juggle.comparator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator that delegates comparisons to a list of child comparators.
 *
 * @param <T>
 */
public class MultiComparator<T> implements Comparator<T> {
    private final List<? extends Comparator<T>> comparators;

    MultiComparator(List<? extends Comparator<T>> comparators) {
        this.comparators = Collections.unmodifiableList(comparators);
    }

    /**
     * Ts are ordered by the first comparator that is able to distinguish them,
     * or 0 if all comparators return 0.
     *
     * @param o1 First object to compare
     * @param o2 Second object to compare
     * @return -1 if first should be ordered before the second, +1 if it should be ordered after, or 0 otherwise.
     */
    @Override
    public int compare(T o1, T o2) {
        return comparators.stream()
                .mapToInt(c -> c.compare(o1, o2))
                .filter(v -> v != 0)
                .findFirst()
                .orElse(0);
    }

    public static<T> MultiComparator<T> of(List<? extends Comparator<T>> cs) {
        return new MultiComparator<>(cs);
    }
}
