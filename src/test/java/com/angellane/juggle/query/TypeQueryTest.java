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

import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.match.TypeMatcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.angellane.juggle.match.TypeMatcher.EXACT_MATCH;
import static org.junit.jupiter.api.Assertions.*;

public class TypeQueryTest {
    TypeMatcher tm = new TypeMatcher(false);

    @Test
    public void testClassQuery() {
        TypeCandidate ct =
                TypeCandidate.candidateForType(java.lang.String.class);

        TypeQuery q = new TypeQuery();
        q.annotationTypes   = Set.of();
        q.accessibility     = Accessibility.PUBLIC;
        q.modifiers         = Modifier.FINAL;
        q.supertype         = BoundedType.exactType(java.lang.Object.class);
        q.superInterfaces   = Set.of(
                BoundedType.exactType(java.io.Serializable.class),
                BoundedType.exactType(java.lang.Comparable.class),
                BoundedType.exactType(java.lang.CharSequence.class),
                BoundedType.exactType(java.lang.constant.Constable.class),
                BoundedType.exactType(java.lang.constant.ConstantDesc.class));

        assertEquals(EXACT_MATCH, q.scoreCandidate(tm, ct));
    }

    @Test
    public void testInterfaceQuery() {
        TypeCandidate ct =
                TypeCandidate.candidateForType(java.util.List.class);

        TypeQuery q = new TypeQuery();
        q.accessibility     = Accessibility.PUBLIC;
        q.modifiers         = Modifier.ABSTRACT;
        q.declarationPattern = Pattern.compile("List", Pattern.LITERAL);
        q.superInterfaces   = Set.of(
                BoundedType.exactType(java.util.Collection.class));

        assertEquals(EXACT_MATCH, q.scoreCandidate(tm, ct));
    }

    @Test
    public void testEnumQuery() {
        TypeCandidate ct =
                TypeCandidate.candidateForType(java.lang.Thread.State.class);

        TypeQuery q = new TypeQuery();
        q.accessibility     = Accessibility.PUBLIC;
        q.modifiers         = Modifier.STATIC;  // also FINAL
        q.declarationPattern = Pattern.compile("State", Pattern.LITERAL);

        assertEquals(EXACT_MATCH, q.scoreCandidate(tm, ct));
    }

    @Test
    public void testRecordQuery() {
        // As of Java 20 there's only one record class in the JDK!
        TypeCandidate ct =
                TypeCandidate.candidateForType(
                        jdk.net.UnixDomainPrincipal.class);

        TypeQuery q = new TypeQuery();
        q.modifiers         = Modifier.FINAL;
        q.declarationPattern = Pattern.compile("Unix");
        q.params            = List.of(
                ParamSpec.param("user", java.nio.file.attribute.UserPrincipal.class),
                ParamSpec.param("group", java.nio.file.attribute.GroupPrincipal.class)
        );

        assertEquals(EXACT_MATCH, q.scoreCandidate(tm, ct));
    }

    @Test
    public void testPrimitiveQuery() {
        // It doesn't really make sense to use a primitive type as a candidate
        // but let's include a test for how that turns out anyway.

        TypeCandidate ct =
                TypeCandidate.candidateForType(java.lang.Integer.TYPE);

        TypeQuery q = new TypeQuery();
        q.annotationTypes   = Set.of();
        q.accessibility     = Accessibility.PUBLIC;
        q.modifiers         = Modifier.FINAL | Modifier.ABSTRACT;
        q.declarationPattern = Pattern.compile("int", Pattern.LITERAL);

        assertEquals(EXACT_MATCH, q.scoreCandidate(tm, ct));
    }
}
