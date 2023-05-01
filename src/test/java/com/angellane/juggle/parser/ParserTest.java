package com.angellane.juggle.parser;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.Juggler;
import com.angellane.juggle.parser.DeclLexer;
import com.angellane.juggle.parser.DeclParser;
import com.angellane.juggle.parser.DeclParser.DeclContext;
import com.angellane.juggle.processor.DeclQuery;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
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
        Juggler juggler = new Juggler();
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
    public void testNameExact() {
        Juggler juggler = new Juggler();
        DeclQuery actualQuery = new DeclQuery(juggler,
                "? memberName");

        DeclQuery expectedQuery = new DeclQuery();

        expectedQuery.setNameExact("memberName");

        assertEquals(expectedQuery, actualQuery);
    }

    @Test
    public void testNamePattern() {
        Juggler juggler = new Juggler();
        DeclQuery actualQuery = new DeclQuery(juggler,
                "? /pattern/i");

        DeclQuery expectedQuery = new DeclQuery();

        expectedQuery.setNamePattern(Pattern.compile("pattern", Pattern.CASE_INSENSITIVE));

        assertEquals(expectedQuery, actualQuery);
    }

}
