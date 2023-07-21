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

import com.angellane.backport.jdk17.java.lang.ClassExtras;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.query.TypeFlavour;

import java.lang.annotation.Annotation;
import com.angellane.backport.jdk17.java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeCandidate
implements Candidate
{
    private final Class<?>                clazz;
    private final TypeFlavour             flavour;

    private final Set<Class<?>>           annotationTypes;
    private final Accessibility           accessibility;
    private final int                     otherModifiers;
    private final String                  simpleName;
    private final String                  canonicalName;

    private final Class<?>                superClass;
    private final Set<Class<?>>           superInterfaces;
    private final Set<Class<?>>           permittedSubtypes;
    private final List<RecordComponent>   recordComponents;

    public TypeCandidate(
            Class<?>                clazz,
            TypeFlavour             flavour,
            Set<Class<?>>           annotationTypes,
            Accessibility           accessibility,
            int                     otherModifiers,
            String                  simpleName,
            String                  canonicalName,
            Class<?>                superClass,
            Set<Class<?>>           superInterfaces,
            Set<Class<?>>           permittedSubtypes,
            List<RecordComponent>   recordComponents) {
        this.clazz              = clazz;
        this.flavour            = flavour;
        this.annotationTypes    = annotationTypes;
        this.accessibility      = accessibility;
        this.otherModifiers     = otherModifiers;
        this.simpleName         = simpleName;
        this.canonicalName      = canonicalName;
        this.superClass         = superClass;
        this.superInterfaces    = superInterfaces;
        this.permittedSubtypes  = permittedSubtypes;
        this.recordComponents   = recordComponents;
    }

    public Class<?>                clazz()      { return clazz; }
    public TypeFlavour             flavour()    { return flavour; }
    public Set<Class<?>>           annotationTypes()    { return annotationTypes; }
    public Accessibility           accessibility()      { return accessibility; }
    public int                     otherModifiers()     { return otherModifiers; }
    public String                  simpleName()         { return simpleName; }
    public String                  canonicalName()      { return canonicalName; }
    public Class<?>                superClass()         { return superClass; }
    public Set<Class<?>>           superInterfaces()    { return superInterfaces; }
    public Set<Class<?>>           permittedSubtypes()  { return permittedSubtypes; }
    public List<RecordComponent>   recordComponents()   { return recordComponents; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeCandidate that = (TypeCandidate) o;
        return otherModifiers == that.otherModifiers
                && Objects.equals(clazz, that.clazz)
                && flavour == that.flavour
                && Objects.equals(annotationTypes, that.annotationTypes)
                && accessibility == that.accessibility
                && Objects.equals(simpleName, that.simpleName)
                && Objects.equals(canonicalName, that.canonicalName)
                && Objects.equals(superClass, that.superClass)
                && Objects.equals(superInterfaces, that.superInterfaces)
                && Objects.equals(permittedSubtypes, that.permittedSubtypes)
                && Objects.equals(recordComponents, that.recordComponents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clazz,
                flavour,
                annotationTypes,
                accessibility,
                otherModifiers,
                simpleName,
                canonicalName,
                superClass,
                superInterfaces,
                permittedSubtypes,
                recordComponents
        );
    }

    @Override
    public String toString() {
        return clazz().toString();
    }

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
                ClassExtras.getPermittedSubclasses(c) == null
                ? Set.of()
                : Set.of(ClassExtras.getPermittedSubclasses(c));
        List<RecordComponent> recordComponents =
                ClassExtras.getRecordComponents(c) == null
                ? List.of()
                : List.of(ClassExtras.getRecordComponents(c));

        return new TypeCandidate(c, f, annotations, access, mods,
                c.getSimpleName(), c.getCanonicalName(),
                superClass, superInterfaces,
                permittedSubtypes, recordComponents);
    }
}
