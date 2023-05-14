package com.angellane.juggle.candidate;

import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.query.TypeFlavour;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CandidateTypeTest {
    @Test
    public void testClassCandidate() {
        CandidateType ct =
                CandidateType.candidateForType(java.lang.String.class);

        assertEquals(java.lang.String.class,            ct.clazz());
        assertEquals(TypeFlavour.CLASS,                 ct.flavour());
        assertEquals(Accessibility.PUBLIC,              ct.accessibility());
        assertEquals(Modifier.FINAL,                    ct.otherModifiers());
        assertEquals(Set.of(),                          ct.annotationTypes());
        assertEquals(java.lang.Object.class,            ct.superClass());
        assertEquals(
                Set.of(java.io.Serializable.class,
                    java.lang.Comparable.class,
                    java.lang.CharSequence.class,
                    java.lang.constant.Constable.class,
                    java.lang.constant.ConstantDesc.class
                ),                                      ct.superInterfaces());
        assertEquals(List.of(),                         ct.recordComponents());
    }

    @Test
    public void testInterfaceCandidate() {
        CandidateType ct =
                CandidateType.candidateForType(java.util.List.class);

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
        CandidateType ct =
                CandidateType.candidateForType(java.lang.Thread.State.class);

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
        // As of Java 20 there's only one record class in the JDK!
        CandidateType ct =
                CandidateType.candidateForType(
                        jdk.net.UnixDomainPrincipal.class);

        assertEquals(jdk.net.UnixDomainPrincipal.class, ct.clazz());
        assertEquals(TypeFlavour.RECORD,                ct.flavour());
        assertEquals(Accessibility.PUBLIC,              ct.accessibility());
        assertEquals(Modifier.FINAL,                    ct.otherModifiers());
        assertEquals(Set.of(),                          ct.annotationTypes());
        assertEquals(java.lang.Record.class,            ct.superClass());
        assertEquals(Set.of(),                          ct.superInterfaces());

        assertEquals(List.of("user", "group"),
                ct.recordComponents().stream()
                        .map(RecordComponent::getName)
                        .toList(),
                "recordComponent names match");

        assertEquals(List.of(java.nio.file.attribute.UserPrincipal.class,
                        java.nio.file.attribute.GroupPrincipal.class),
                ct.recordComponents().stream()
                        .map(RecordComponent::getType)
                        .toList(),
                "recordComponent types match");
    }

    @Test
    public void testPrimitiveCandidate() {
        // It doesn't really make sense to use a primitive type as a candidate
        // but let's include a test for how that turns out anyway.

        CandidateType ct =
                CandidateType.candidateForType(java.lang.Integer.TYPE);

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
