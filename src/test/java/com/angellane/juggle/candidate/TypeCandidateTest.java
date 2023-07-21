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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import com.angellane.backport.jdk17.java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TypeCandidateTest {
    @Test
    public void testClassCandidate() {
        TypeCandidate ct =
                TypeCandidate.candidateForType(java.util.ArrayList.class);

        assertEquals(java.util.ArrayList.class,         ct.clazz());
        assertEquals(TypeFlavour.CLASS,                 ct.flavour());
        assertEquals(Accessibility.PUBLIC,              ct.accessibility());
        assertEquals(0,                                 ct.otherModifiers());
        assertEquals(Set.of(),                          ct.annotationTypes());
        assertEquals(java.util.AbstractList.class,      ct.superClass());
        assertEquals(
                Set.of(java.io.Serializable.class,
                    java.lang.Cloneable.class,
                    java.util.List.class,
                    java.util.RandomAccess.class
                ),                                      ct.superInterfaces());
        assertEquals(List.of(),                         ct.recordComponents());
    }

    @Test
    public void testInterfaceCandidate() {
        TypeCandidate ct =
                TypeCandidate.candidateForType(java.util.List.class);

        assertEquals(java.util.List.class,              ct.clazz());
        assertEquals(TypeFlavour.INTERFACE,             ct.flavour());
        assertEquals(Accessibility.PUBLIC,              ct.accessibility());
        assertEquals(Modifier.ABSTRACT,                 ct.otherModifiers());
        assertEquals(Set.of(),                          ct.annotationTypes());
        assertNull(                                     ct.superClass());
        assertEquals(Set.of(java.util.Collection.class),ct.superInterfaces());
        assertEquals(List.of(),                         ct.recordComponents());
    }

    @Test
    public void testEnumCandidate() {
        TypeCandidate ct =
                TypeCandidate.candidateForType(java.lang.Thread.State.class);

        assertEquals(java.lang.Thread.State.class,      ct.clazz());
        assertEquals(TypeFlavour.ENUM,                  ct.flavour());
        assertEquals(Accessibility.PUBLIC,              ct.accessibility());
        assertEquals(Modifier.STATIC | Modifier.FINAL,  ct.otherModifiers());
        assertEquals(Set.of(),                          ct.annotationTypes());
        assertEquals(java.lang.Enum.class,              ct.superClass());
        assertEquals(Set.of(),                          ct.superInterfaces());
        assertEquals(List.of(),                         ct.recordComponents());
    }

    @Test
    public void testRecordCandidate() {
        Class<?> unixDomainPrincipalClass   = null;
        Class<?> userPrincipalClass         = null;
        Class<?> groupPrincipalClass        = null;
        Class<?> recordClass                = null;

        try {
            unixDomainPrincipalClass =
                    Class.forName("jdk.net.UnixDomainPrincipal");
            userPrincipalClass =
                    Class.forName("java.nio.file.attribute.UserPrincipal");
            groupPrincipalClass =
                    Class.forName("java.nio.file.attribute.GroupPrincipal");
            recordClass =
                    Class.forName("java.lang.Record");
        } catch (ClassNotFoundException e) {
            // The classes we use in this test only exist since JDK 16
            // When running on earlier releases we should end up here

            Assumptions.assumeTrue(Runtime.version().feature() < 16);
            Assumptions.abort("Skipping test -- Record classes not available");
        }

        TypeCandidate ct =
                TypeCandidate.candidateForType(unixDomainPrincipalClass);

        assertEquals(unixDomainPrincipalClass,          ct.clazz());
        assertEquals(TypeFlavour.RECORD,                ct.flavour());
        assertEquals(Accessibility.PUBLIC,              ct.accessibility());
        assertEquals(Modifier.FINAL,                    ct.otherModifiers());
        assertEquals(Set.of(),                          ct.annotationTypes());
        assertEquals(recordClass,                       ct.superClass());
        assertEquals(Set.of(),                          ct.superInterfaces());

        assertEquals(List.of("user", "group"),
                ct.recordComponents().stream()
                        .map(RecordComponent::getName)
                        .collect(Collectors.toList()),
                "recordComponent names match");

        assertEquals(List.of(userPrincipalClass, groupPrincipalClass),
                ct.recordComponents().stream()
                        .map(RecordComponent::getType)
                        .collect(Collectors.toList()),
                "recordComponent types match");
    }

    @Test
    public void testPrimitiveCandidate() {
        // It doesn't really make sense to use a primitive type as a candidate
        // but let's include a test for how that turns out anyway.

        TypeCandidate ct =
                TypeCandidate.candidateForType(java.lang.Integer.TYPE);

        assertEquals(java.lang.Integer.TYPE,            ct.clazz());
        assertEquals(TypeFlavour.CLASS,                 ct.flavour());
        assertEquals(Accessibility.PUBLIC,              ct.accessibility());
        assertEquals(Modifier.FINAL | Modifier.ABSTRACT,ct.otherModifiers());
        assertEquals(Set.of(),                          ct.annotationTypes());
        assertNull(                                     ct.superClass());
        assertEquals(Set.of(),                          ct.superInterfaces());
        assertEquals(List.of(),                         ct.recordComponents());
    }
}
