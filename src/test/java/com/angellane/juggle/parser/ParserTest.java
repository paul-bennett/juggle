package com.angellane.juggle.parser;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.Juggler;
import com.angellane.juggle.parser.DeclParser.MemberDeclContext;
import com.angellane.juggle.query.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
    Juggler juggler = new Juggler();
    QueryFactory factory = new QueryFactory(juggler);

    DeclParser parserForString(String input) {
        CharStream inputStream = CharStreams.fromString(input);

        DeclLexer lexer = new DeclLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return new DeclParser(tokenStream);
    }

    private MemberQuery memberQueryFor(String decl) {
        Query q = factory.createQuery(decl);

        if (q instanceof MemberQuery memberQuery)
            return memberQuery;
        else {
            fail("Query is not a MemberQuery");
            return null;
        }
    }

    @Test
    public void testCompleteInput() {
        DeclParser parser = parserForString("foo bar");
        MemberDeclContext tree = parser.memberDecl();

        Token tok = parser.getCurrentToken();

        assertNotNull(tree);
        assertEquals(Token.EOF, tok.getType(), "Consumed all input");
    }

    @Test
    public void testExtraInput() {
        DeclParser parser = parserForString("foo bar baz");
        MemberDeclContext tree = parser.memberDecl();

        Token tok = parser.getCurrentToken();

        assertNotNull(tree);
        assertNotEquals(Token.EOF, tok.getType(), "Consumed all input");
    }

    @Test
    public void testModifiers() {
        MemberQuery actualQuery = memberQueryFor(
                "@java.lang.SafeVarargs @java.lang.Deprecated"
                + " protected static"
        );

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.addAnnotationType(SafeVarargs.class);
        expectedQuery.addAnnotationType(Deprecated.class);
        expectedQuery.addModifier(Modifier.STATIC, true);
        expectedQuery.setAccessibility(Accessibility.PROTECTED);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testExactReturnType() {
        MemberQuery actualQuery = memberQueryFor("String");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.returnType = BoundedType.exactType(String.class);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testArrayReturnType() {
        MemberQuery actualQuery = memberQueryFor("Integer[][][]");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.returnType = BoundedType.exactType(
                Integer.class.arrayType().arrayType().arrayType());

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testEllipsisReturnType() {
        MemberQuery actualQuery = memberQueryFor("float[]...");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.returnType = BoundedType.exactType(
                Float.TYPE.arrayType().arrayType());

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testWildcardReturnType() {
        MemberQuery actualQuery = memberQueryFor("?");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.returnType = BoundedType.wildcardType();

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testSingleUpperBoundedReturnType() {
        MemberQuery actualQuery = memberQueryFor("? extends String");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.returnType = BoundedType.subtypeOf(String.class);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testMultipleUpperBoundedReturnType() {
        MemberQuery actualQuery = memberQueryFor(
                "? extends String & java.io.Serializable");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.returnType =
                BoundedType.subtypeOf(Set.of(String.class, Serializable.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testLowerBoundedReturnType() {
        MemberQuery actualQuery = memberQueryFor("? super Integer");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.returnType = BoundedType.supertypeOf(Integer.class);

        assertEquals(expectedQuery, actualQuery);
    }


    @Test
    public void testNameExact() {
        MemberQuery actualQuery = memberQueryFor("? memberName");

        MemberQuery expectedQuery = new MemberQuery();

        expectedQuery.returnType = BoundedType.wildcardType();
        expectedQuery.setNameExact("memberName");

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testNamePattern() {
        MemberQuery actualQuery = memberQueryFor("? /pattern/i");

        MemberQuery expectedQuery = new MemberQuery();

        expectedQuery.returnType = BoundedType.wildcardType();
        expectedQuery.setNamePattern(Pattern.compile("pattern", Pattern.CASE_INSENSITIVE));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testWildcardEllipsisParamType() {
        MemberQuery actualQuery = memberQueryFor("(,?,...)");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.params = List.of(
                ParamSpec.wildcard(),
                ParamSpec.wildcard(),
                ParamSpec.ellipsis());

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testBoundedParamType() {
        MemberQuery actualQuery = memberQueryFor(
                "(? extends java.net.InetAddress,"
                        + "? extends java.util.List & java.util.RandomAccess,"
                        + "? super Integer)");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.params = List.of(
                ParamSpec.unnamed(
                        BoundedType.subtypeOf(java.net.InetAddress.class)),
                ParamSpec.unnamed(
                        BoundedType.subtypeOf(
                                Set.of(java.util.List.class,
                                        java.util.RandomAccess.class))
                ),
                ParamSpec.unnamed(BoundedType.supertypeOf(Integer.class))
        );

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testExactParamType() {
        MemberQuery actualQuery = memberQueryFor("(Integer)");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.params = List.of(
                ParamSpec.unnamed(BoundedType.exactType(Integer.class)));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testEmptyParams() {
        MemberQuery actualQuery = memberQueryFor("()");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.params = List.of();

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testNoThrowsClause() {
        MemberQuery actualQuery = memberQueryFor("()");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.params = List.of();
        expectedQuery.exceptions = null;

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsNothing() {
        MemberQuery actualQuery = memberQueryFor("throws");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.exceptions = Set.of();

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsOneClass() {
        MemberQuery actualQuery = memberQueryFor("throws ArithmeticException");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.exceptions =
                Set.of(BoundedType.exactType(ArithmeticException.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsWildcard() {
        MemberQuery actualQuery = memberQueryFor("throws ?");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.exceptions = Set.of(BoundedType.wildcardType());

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsLowerBound() {
        MemberQuery actualQuery = memberQueryFor(
                "throws ? super java.io.FileNotFoundException");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.exceptions = Set.of(
                BoundedType.supertypeOf(java.io.FileNotFoundException.class)
        );

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsSingleUpperBound() {
        MemberQuery actualQuery = memberQueryFor(
                "throws ? extends java.io.IOException");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.exceptions =
                Set.of(BoundedType.subtypeOf(java.io.IOException.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsMultipleUpperBounds() {
        MemberQuery actualQuery = memberQueryFor(
                "throws ? extends Error & Exception & Throwable");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.exceptions =
                Set.of(BoundedType.subtypeOf(
                        Error.class, Exception.class, Throwable.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsTwoClasses() {
        MemberQuery actualQuery = memberQueryFor(
                "throws ArithmeticException, java.io.IOException");

        MemberQuery expectedQuery = new MemberQuery();
        expectedQuery.exceptions =
                Set.of(BoundedType.exactType(ArithmeticException.class),
                        BoundedType.exactType(java.io.IOException.class));

        assertEquals(expectedQuery, actualQuery);
    }

//    @Test
//    public void testThrowsClassPlusEllipsis() {
//        MemberQuery actualQuery = memberQueryFor(
//                "() throws NullPointerException, ...");
//
//        MemberQuery expectedQuery = new MemberQuery();
//        expectedQuery.exceptions =
//                Set.of(BoundedType.exactType(NullPointerException.class)
//                , DeclQuery.Ellipsis);
//
//        assertEquals(expectedQuery, actualQuery);
//    }


}
