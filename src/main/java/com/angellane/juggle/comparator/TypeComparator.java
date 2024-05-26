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

import java.util.Comparator;

/**
 * Compares types by specificity, with the most specific ordered first.
 */
public class TypeComparator implements Comparator<Class<?>> {
    @Override
    public int compare(Class<?> o1, Class<?> o2) {
        boolean assignableOneFromTwo = o1.isAssignableFrom(o2);
        boolean assignableTwoFromOne = o2.isAssignableFrom(o1);

        return (assignableOneFromTwo == assignableTwoFromOne)
                ? 0
                : assignableTwoFromOne ? -1 : 1;
    }
}
