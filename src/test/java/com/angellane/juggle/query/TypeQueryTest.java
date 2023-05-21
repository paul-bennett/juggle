package com.angellane.juggle.query;

import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.candidate.TypeCandidate;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TypeQueryTest {
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

        assertTrue(q.isMatchForCandidate(ct));
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

        assertTrue(q.isMatchForCandidate(ct));
    }

    @Test
    public void testEnumQuery() {
        TypeCandidate ct =
                TypeCandidate.candidateForType(java.lang.Thread.State.class);

        TypeQuery q = new TypeQuery();
        q.accessibility     = Accessibility.PUBLIC;
        q.modifiers         = Modifier.STATIC;  // also FINAL
        q.declarationPattern = Pattern.compile("State", Pattern.LITERAL);

        assertTrue(q.isMatchForCandidate(ct));
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
        q.recordComponents  = List.of(
                ParamSpec.param("user", java.nio.file.attribute.UserPrincipal.class),
                ParamSpec.param("group", java.nio.file.attribute.GroupPrincipal.class)
        );

        assertTrue(q.isMatchForCandidate(ct));
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

        assertTrue(q.isMatchForCandidate(ct));
    }
}
