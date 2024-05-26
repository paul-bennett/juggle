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
