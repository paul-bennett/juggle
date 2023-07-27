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
package com.angellane.juggle.candidate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents a single parameter of a method declaration,
 * or record component of a record class.
 */
public class Param {
    private final Set<Class<?>> annotations;
    private final int otherModifiers;
    private final Class<?> type;
    private final String name;

    public Param(Set<Class<?>> annotations,
                 int otherModifiers,
                 Class<?> type,
                 String name) {
        this.annotations = annotations;
        this.otherModifiers = otherModifiers;
        this.type = type;
        this.name = name;
    }

    public Param(Parameter p) {
        this(Arrays.stream(p.getAnnotations())
                        .map(Annotation::annotationType)
                        .collect(Collectors.toSet()),
                p.getModifiers(),
                p.getType(),
                p.isNamePresent() ? p.getName() : null);
    }

    public Param(Class<?> c, String name) {
        this(Collections.emptySet(), 0, c, name);
    }

    public Set<Class<?>> annotations()  { return annotations; }
    public int otherModifiers()         { return otherModifiers; }
    public Class<?> type()              { return type; }
    public String name()                { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Param param = (Param) o;
        return otherModifiers == param.otherModifiers
                && Objects.equals(annotations, param.annotations)
                && Objects.equals(type, param.type)
                && Objects.equals(name, param.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotations, otherModifiers, type, name);
    }
}
