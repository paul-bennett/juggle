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
package com.angellane.juggle.candidate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents a single parameter of a method declaration,
 * or record component of a record class.
 */
public record Param(Set<Class<?>> annotations,
                    int otherModifiers,
                    Class<?> type,
                    String name
                    ) {
    public Param(Parameter p) {
        this(Arrays.stream(p.getAnnotations())
                        .map(Annotation::annotationType)
                        .collect(Collectors.toSet()),
                p.getModifiers(),
                p.getType(),
                p.isNamePresent() ? p.getName() : null);
    }

    public Param(Class<?> c, String name) {
        this(Set.of(), 0, c, name);
    }
}
