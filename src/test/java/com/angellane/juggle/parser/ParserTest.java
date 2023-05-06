package com.angellane.juggle.parser;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.Juggler;
import com.angellane.juggle.parser.DeclParser.DeclContext;
import com.angellane.juggle.processor.DeclQuery;
import com.angellane.juggle.processor.DeclQuery.BoundedType;
import com.angellane.juggle.processor.DeclQuery.ParamSpec;
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

    DeclParser parserForString(String input) {
        CharStream inputStream = CharStreams.fromString(input);

        DeclLexer lexer = new DeclLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return new DeclParser(tokenStream);
    }

    @Test
    public void testCompleteInput() {
        DeclParser parser = parserForString("foo bar");
        DeclContext tree = parser.decl();

        Token tok = parser.getCurrentToken();

        assertNotNull(tree);
        assertEquals(Token.EOF, tok.getType(), "Consumed all input");
    }

    @Test
    public void testExtraInput() {
        DeclParser parser = parserForString("foo bar baz");
        DeclContext tree = parser.decl();

        Token tok = parser.getCurrentToken();

        assertNotNull(tree);
        assertNotEquals(Token.EOF, tok.getType(), "Consumed all input");
    }

    @Test
    public void testModifiers() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                        "@java.lang.SafeVarargs @java.lang.Deprecated"
                                + " protected static");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.addAnnotationType(SafeVarargs.class);
        expectedQuery.addAnnotationType(Deprecated.class);
        expectedQuery.addModifier(Modifier.STATIC, true);
        expectedQuery.setAccessibility(Accessibility.PROTECTED);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testExactReturnType() {
        DeclQuery actualQuery = new DeclQuery(juggler, "String");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.returnType = BoundedType.exactType(String.class);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testArrayReturnType() {
        DeclQuery actualQuery = new DeclQuery(juggler, "Integer[][][]");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.returnType = BoundedType.exactType(
                Integer.class.arrayType().arrayType().arrayType());

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testEllipsisReturnType() {
        DeclQuery actualQuery = new DeclQuery(juggler, "float[]...");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.returnType = BoundedType.exactType(
                Float.TYPE.arrayType().arrayType());

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testWildcardReturnType() {
        DeclQuery actualQuery = new DeclQuery(juggler, "?");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.returnType = BoundedType.wildcardType();

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testSingleUpperBoundedReturnType() {
        DeclQuery actualQuery = new DeclQuery(juggler, "? extends String");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.returnType = BoundedType.subtypeOf(String.class);

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testMultipleUpperBoundedReturnType() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "? extends String & java.io.Serializable");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.returnType =
                BoundedType.subtypeOf(Set.of(String.class, Serializable.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testLowerBoundedReturnType() {
        DeclQuery actualQuery = new DeclQuery(juggler, "? super Integer");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.returnType = BoundedType.supertypeOf(Integer.class);

        assertEquals(expectedQuery, actualQuery);
    }


    @Test
    public void testNameExact() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "? memberName");

        DeclQuery expectedQuery = new DeclQuery();

        expectedQuery.returnType = BoundedType.wildcardType();
        expectedQuery.setNameExact("memberName");

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testNamePattern() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "? /pattern/i");

        DeclQuery expectedQuery = new DeclQuery();

        expectedQuery.returnType = BoundedType.wildcardType();
        expectedQuery.setNamePattern(Pattern.compile("pattern", Pattern.CASE_INSENSITIVE));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testWildcardEllipsisParamType() {
        DeclQuery actualQuery = new DeclQuery(juggler, "(,?,...)");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.params = List.of(
                ParamSpec.wildcard(),
                ParamSpec.wildcard(),
                ParamSpec.ellipsis());

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testBoundedParamType() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "(? extends java.net.InetAddress,"
                        + "? extends java.util.List & java.util.RandomAccess,"
                        + "? super Integer)");

        DeclQuery expectedQuery = new DeclQuery();
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
        DeclQuery actualQuery = new DeclQuery(juggler, "(Integer)");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.params = List.of(
                ParamSpec.unnamed(BoundedType.exactType(Integer.class)));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testEmptyParams() {
        DeclQuery actualQuery = new DeclQuery(juggler, "()");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.params = List.of();

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testNoThrowsClause() {
        DeclQuery actualQuery = new DeclQuery(juggler, "()");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.params = List.of();
        expectedQuery.exceptions = null;

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsNothing() {
        DeclQuery actualQuery = new DeclQuery(juggler, "throws");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.exceptions = Set.of();

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsOneClass() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "throws ArithmeticException");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.exceptions =
                Set.of(BoundedType.exactType(ArithmeticException.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsWildcard() {
        DeclQuery actualQuery = new DeclQuery(juggler, "throws ?");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.exceptions = Set.of(BoundedType.wildcardType());

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsLowerBound() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "throws ? super java.io.FileNotFoundException");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.exceptions = Set.of(
                BoundedType.supertypeOf(java.io.FileNotFoundException.class)
        );

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsSingleUpperBound() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "throws ? extends java.io.IOException");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.exceptions =
                Set.of(BoundedType.subtypeOf(java.io.IOException.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsMultipleUpperBounds() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "throws ? extends Error & Exception & Throwable");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.exceptions =
                Set.of(BoundedType.subtypeOf(
                        Error.class, Exception.class, Throwable.class));

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testThrowsTwoClasses() {
        DeclQuery actualQuery = new DeclQuery(juggler,
                "throws ArithmeticException, java.io.IOException");

        DeclQuery expectedQuery = new DeclQuery();
        expectedQuery.exceptions =
                Set.of(BoundedType.exactType(ArithmeticException.class),
                        BoundedType.exactType(java.io.IOException.class));

        assertEquals(expectedQuery, actualQuery);
    }

//    @Test
//    public void testThrowsClassPlusEllipsis() {
//        DeclQuery actualQuery = new DeclQuery(
//                juggler, "() throws NullPointerException, ...");
//
//        DeclQuery expectedQuery = new DeclQuery();
//        expectedQuery.exceptions =
//                Set.of(BoundedType.exactType(NullPointerException.class)
//                , DeclQuery.Ellipsis);
//
//        assertEquals(expectedQuery, actualQuery);
//    }


}
