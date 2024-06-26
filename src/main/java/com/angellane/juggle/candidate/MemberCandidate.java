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

import com.angellane.juggle.match.Accessibility;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This POJO contains the details of the candidate member (field, constructor or method).
 * <p>
 * The 'member' field is the member itself.  More interesting are the paramTypes, returnType and throwTypes
 * fields, which represent the type the member would have if considered as a static function.  For static
 * methods the paramTypes field includes an implicit first entry representing the type of 'this'.
 */
public record MemberCandidate(
        Member member,
        Accessibility accessibility,
        int otherModifiers,
        Set<Class<?>> annotationTypes,
        String simpleName,
        String canonicalName,
        Class<?> returnType,
        List<Param> params,
        Set<Class<?>> throwTypes
)
        implements Candidate {

    // Constructor used by factory methods
    private MemberCandidate(Member member, Set<Class<?>> annotationTypes,
                            Class<?> returnType, List<Param> params,
                            Set<Class<?>> throwTypes) {
        this(member, Accessibility.fromModifiers(member.getModifiers()),
                member.getModifiers() & OTHER_MODIFIERS_MASK,
                annotationTypes, member.getName(),
                member.getDeclaringClass().getCanonicalName()
                        + "." + member.getName(),
                returnType, params, throwTypes);
    }

    // This constructor is used by parameter permutation generator
    public MemberCandidate(MemberCandidate other, List<Param> params) {
        this(other.member, other.accessibility, other.otherModifiers,
                other.annotationTypes, other.simpleName, other.canonicalName,
                other.returnType, params, other.throwTypes);
    }

    @Override
    public String packageName() {
        return member().getDeclaringClass().getPackageName();
    }

    public static MemberCandidate memberFromMethod(Method m) {
        return new MemberCandidate(m,
                annotationClasses(m.getAnnotations()),
                m.getReturnType(),
                paramsWithImplicitThis(m,
                        Arrays.stream(m.getParameters())
                                .map(Param::new).toList()
                ),
                Set.of(m.getExceptionTypes())
        );
    }

    public static MemberCandidate memberFromConstructor(Constructor<?> c) {
        return new MemberCandidate(c,
                annotationClasses(c.getAnnotations()),
                c.getDeclaringClass(),
                Arrays.stream(c.getParameters()).map(Param::new).toList(),
                Set.of(c.getExceptionTypes())
        );
    }

    public static List<MemberCandidate> membersFromField(Field f) {
        Set<Class<?>> as = annotationClasses(
                f.getDeclaredAnnotations());

        var getter = new MemberCandidate(f, as, f.getType(),
                paramsWithImplicitThis(f, List.of()), Set.of()
        );
        var setter = new MemberCandidate(f, as, Void.TYPE,
                paramsWithImplicitThis(f, List.of(
                        new Param(f.getType(), f.getName()))
                ), Set.of()
        );

        return List.of(getter, setter);
    }

    private static Set<Class<?>> annotationClasses(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .map(Annotation::annotationType)
                .collect(Collectors.toSet());
    }

    private static Param thisParam(Member m) {
        return new Param(Set.of(), m.getModifiers(),
                m.getDeclaringClass(), "this");
    }

    private static List<Param> paramsWithImplicitThis(
            Member m, List<Param> params
    ) {
        if (Modifier.STATIC == (m.getModifiers() & Modifier.STATIC))
            return params;
        else
            return Stream.concat(Stream.of(thisParam(m)), params.stream())
                    .toList();
    }
}
