package com.angellane.juggle.query;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.candidate.CandidateMember;
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
public class TestMemberDecl {
    static CandidateMember cm;

    @BeforeAll
    public static void createCandidateMember()
            throws NoSuchMethodException {
        Class<?> c = ClassLoader.class;
        Method m = c.getDeclaredMethod("findResource", String.class);
        cm = CandidateMember.memberFromMethod(m);
    }

    private static void matchQueryAndCandidate(MemberQuery q, CandidateMember cm) {
        assertTrue(q.matchesAnnotations(cm.annotationTypes())
                , "Match annotations");
        assertTrue(q.matchesModifiers(cm.otherModifiers())
                , "Match modifiers");
        assertTrue(q.matchesAccessibility(cm.accessibility())
                , "Match accessibility");
        assertTrue(q.matchesReturn(cm.returnType())      , "Match return type");
        assertTrue(q.matchesName(cm.declarationName())   , "Match method name");
        assertTrue(q.matchesParams(cm.paramTypes())      , "Match parameters");
        assertTrue(q.matchesExceptions(cm.throwTypes())  , "Match exceptions");

        assertTrue(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testAllAttributes() {
        MemberQuery q = new MemberQuery();

        q.annotationTypes = Set.of();
        q.modifierMask = Modifier.STATIC;
        q.accessibility = Accessibility.PROTECTED;
        q.returnType = BoundedType.exactType(java.net.URL.class);
        q.declarationPattern = Pattern.compile("^findResource$");
        q.params = List.of(
                ParamSpec.param(null, ClassLoader.class),
                ParamSpec.param("name", String.class));
        q.exceptions = Set.of();

        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testEllipsis() {
        MemberQuery q = new MemberQuery();

        q.declarationPattern = Pattern.compile("^findResource$");
        q.params = List.of(ParamSpec.ellipsis());

        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testTooManyArgs() {
        MemberQuery q = new MemberQuery();

        q.declarationPattern = Pattern.compile("^findResource$");
        q.params = List.of(
                ParamSpec.param(null, ClassLoader.class),
                ParamSpec.param("name", String.class),
                ParamSpec.param("name", String.class),
                ParamSpec.ellipsis());

        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testTooFewArgs() {
        MemberQuery q = new MemberQuery();

        q.declarationPattern = Pattern.compile("^findResource$");
        q.params = List.of(ParamSpec.param(null, ClassLoader.class));

        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testNoAttributes() {
        MemberQuery q = new MemberQuery();
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectAnnotations() {
        MemberQuery q = new MemberQuery();
        q.annotationTypes = Set.of();
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectModifiers() {
        MemberQuery q = new MemberQuery();
        q.modifierMask = Modifier.STATIC;
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectAccessibility() {
        MemberQuery q = new MemberQuery();
        q.accessibility = Accessibility.PROTECTED;
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectReturnType() {
        MemberQuery q = new MemberQuery();
        q.returnType = BoundedType.exactType(java.net.URL.class);
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectDeclarationName() {
        MemberQuery q = new MemberQuery();
        q.declarationPattern = Pattern.compile("^findResource$");
        matchQueryAndCandidate(q, cm);
    }

    @Test
    @Disabled("Param name matching not implemented yet")
    public void testCorrectNamedParams() {
        MemberQuery q = new MemberQuery();
        q.params = List.of(ParamSpec.param("name", String.class));
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testCorrectExceptions() {
        MemberQuery q = new MemberQuery();
        q.exceptions = Set.of();
        matchQueryAndCandidate(q, cm);
    }

    @Test
    public void testWrongAnnotations() {
        MemberQuery q = new MemberQuery();
        q.annotationTypes = Set.of(Override.class);
        assertFalse(q.matchesAnnotations(cm.annotationTypes()),
                "Match annotations");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongModifiers() {
        MemberQuery q = new MemberQuery();

        q.modifierMask = Modifier.STATIC;
        q.modifiers = Modifier.STATIC;

        assertFalse(q.matchesModifiers(cm.otherModifiers()),
                "Match modifiers");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongAccessibility() {
        MemberQuery q = new MemberQuery();
        q.accessibility = Accessibility.PUBLIC;
        assertFalse(q.matchesAccessibility(cm.accessibility()),
                "Match accessibility");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongReturnType() {
        MemberQuery q = new MemberQuery();
        q.returnType = BoundedType.exactType(Integer.TYPE);
        assertFalse(q.matchesReturn(cm.returnType()), "Match return");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongDeclarationName() {
        MemberQuery q = new MemberQuery();
        q.declarationPattern = Pattern.compile("^barf$");
        assertFalse(q.matchesName(cm.declarationName()), "Match name");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongParamName() {
        MemberQuery q = new MemberQuery();
        q.params = List.of(ParamSpec.param("foo", String.class));
        assertFalse(q.matchesParams(cm.paramTypes()), "Match params");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongParamType() {
        MemberQuery q = new MemberQuery();
        q.params = List.of(ParamSpec.param("name", Integer.class));
        assertFalse(q.matchesParams(cm.paramTypes()), "Match params");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongParamArity() {
        MemberQuery q = new MemberQuery();
        q.params = List.of(
                ParamSpec.param("name", String.class),
                ParamSpec.param("name", String.class)
        );
        assertFalse(q.matchesParams(cm.paramTypes()), "Match params");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }

    @Test
    public void testWrongExceptions() {
        MemberQuery q = new MemberQuery();
        q.exceptions =
                Set.of(BoundedType.exactType(NoSuchMethodException.class));
        assertFalse(q.matchesExceptions(cm.throwTypes()), "Match exceptions");
        assertFalse(q.isMatchForCandidate(cm), "Match entire declaration");
    }
}
