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

import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.query.TypeFlavour;

import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record TypeCandidate(
        Class<?>                clazz,
        TypeFlavour             flavour,

        Set<Class<?>>           annotationTypes,
        Accessibility           accessibility,
        int                     otherModifiers,
        String                  declarationName,

        Class<?>                superClass,
        Set<Class<?>>           superInterfaces,
        Set<Class<?>>           permittedSubtypes,
        List<RecordComponent>   recordComponents
)
implements Candidate
{
    public String packageName() {
        return clazz.getPackageName();
    }

    public static TypeCandidate candidateForType(Class<?> c) {
        TypeFlavour f                       = TypeFlavour.forClass(c);
        Accessibility access                =
                Accessibility.fromModifiers(c.getModifiers());
        int mods = c.getModifiers() & OTHER_MODIFIERS_MASK;
        Set<Class<?>> annotations           =
                Arrays.stream(c.getDeclaredAnnotations())
                        .map(Annotation::annotationType)
                        .collect(Collectors.toSet());
        Class<?> superClass                 = c.getSuperclass();
        Set<Class<?>> superInterfaces       = Set.of(c.getInterfaces());
        Set<Class<?>> permittedSubtypes     =
                c.getPermittedSubclasses() == null
                ? Set.of()
                : Set.of(c.getPermittedSubclasses());
        List<RecordComponent> recordComponents =
                c.getRecordComponents() == null
                ? List.of()
                : List.of(c.getRecordComponents());

        return new TypeCandidate(c, f, annotations, access, mods,
                c.getSimpleName(), superClass, superInterfaces,
                permittedSubtypes, recordComponents);
    }

}
