package com.angellane.juggle.processor;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.CandidateMember;
import com.angellane.juggle.processor.DeclQuery.BoundedType;
import com.angellane.juggle.processor.DeclQuery.ParamSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class runs tests against the method
 *    `protected java.net.URL ClassLoader.findResource(String name)`
 * using various forms of DeclQuery
 */
public class TestMatch_ClassLoader_findResource {
    static CandidateMember cm;

    @BeforeAll
    public static void createCandidateMember()
            throws NoSuchMethodException {
        Class<?> c = ClassLoader.class;
        Method m = c.getDeclaredMethod("findResource", String.class);
        cm = CandidateMember.memberFromMethod(m);
    }

    private static void matchQueryAndCandidate(DeclQuery q, CandidateMember cm) {
        assertTrue(q.matchesAnnotations(cm) , "Match annotations");
        assertTrue(cm.matchesModifiers(q.modifierMask, q.modifiers)
                , "Match modifiers");
        assertTrue(cm.matchesAccessibility(q.accessibility)
                , "Match accessibility");
        assertTrue(q.matchesReturn(cm)      , "Match return type");
        assertTrue(q.matchesName(cm)        , "Match method name");
        assertTrue(q.matchesParams(cm)      , "Match parameters");
        assertTrue(q.matchesExceptions(cm)  , "Match exceptions");

        assertTrue(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testAllAttributes() {
        DeclQuery q = new DeclQuery();

        q.annotationTypes = Set.of();
        q.modifierMask = Modifier.STATIC;
        q.accessibility = Accessibility.PROTECTED;
        q.returnType = BoundedType.exactType(java.net.URL.class);
        q.declarationName = Pattern.compile("^findResource$");
        q.params = List.of(ParamSpec.param("name", String.class));
        q.exceptions = Set.of();

        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testNoAttributes() {
        DeclQuery q = new DeclQuery();
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectAnnotations() {
        DeclQuery q = new DeclQuery();
        q.annotationTypes = Set.of();
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectModifiers() {
        DeclQuery q = new DeclQuery();
        q.modifierMask = Modifier.STATIC;
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectAccessibility() {
        DeclQuery q = new DeclQuery();
        q.accessibility = Accessibility.PROTECTED;
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectReturnType() {
        DeclQuery q = new DeclQuery();
        q.returnType = BoundedType.exactType(java.net.URL.class);
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectDeclarationName() {
        DeclQuery q = new DeclQuery();
        q.declarationName = Pattern.compile("^findResource$");
        matchQueryAndCandidate(q, cm);
    }

    @Test
    @Disabled("Param matching not implemented yet")
    public void testCorrectParams() {
        DeclQuery q = new DeclQuery();
        q.params = List.of(ParamSpec.param("name", String.class));
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectExceptions() {
        DeclQuery q = new DeclQuery();
        q.exceptions = Set.of();
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testWrongAnnotations() {
        DeclQuery q = new DeclQuery();
        q.annotationTypes = Set.of(Override.class);
        assertFalse(q.matchesAnnotations(cm), "Match annotations");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongModifiers() {
        DeclQuery q = new DeclQuery();

        q.modifierMask = Modifier.STATIC;
        q.modifiers = Modifier.STATIC;

        assertFalse(cm.matchesModifiers(q.modifierMask, q.modifiers),
                "Match modifiers");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongAccessibility() {
        DeclQuery q = new DeclQuery();
        q.accessibility = Accessibility.PUBLIC;
        assertFalse(cm.matchesAccessibility(q.accessibility),
                "Match accessibility");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongReturnType() {
        DeclQuery q = new DeclQuery();
        q.returnType = BoundedType.exactType(Integer.TYPE);
        assertFalse(q.matchesReturn(cm), "Match return");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongDeclarationName() {
        DeclQuery q = new DeclQuery();
        q.declarationName = Pattern.compile("^barf$");
        assertFalse(q.matchesName(cm), "Match name");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    @Disabled("Param matching not implemented yet")
    public void testWrongParamName() {
        DeclQuery q = new DeclQuery();
        q.params = List.of(ParamSpec.param("foo", String.class));
        assertFalse(q.matchesParams(cm), "Match params");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    @Disabled("Param matching not implemented yet")
    public void testWrongParamType() {
        DeclQuery q = new DeclQuery();
        q.params = List.of(ParamSpec.param("name", Integer.class));
        assertFalse(q.matchesParams(cm), "Match params");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    @Disabled("Param matching not implemented yet")
    public void testWrongParamArity() {
        DeclQuery q = new DeclQuery();
        q.params = List.of(
                ParamSpec.param("name", String.class),
                ParamSpec.param("name", String.class)
        );
        assertFalse(q.matchesParams(cm), "Match params");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongExceptions() {
        DeclQuery q = new DeclQuery();
        q.exceptions =
                Set.of(BoundedType.exactType(NoSuchMethodException.class));
        assertFalse(q.matchesExceptions(cm), "Match exceptions");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }
}
