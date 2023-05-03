package com.angellane.juggle.parser;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.Juggler;
import com.angellane.juggle.parser.DeclLexer;
import com.angellane.juggle.parser.DeclParser;
import com.angellane.juggle.parser.DeclParser.DeclContext;
import com.angellane.juggle.processor.DeclQuery;
import com.angellane.juggle.processor.DeclQuery.BoundedType;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Modifier;
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

    // TODO: add tests for array types

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

}
