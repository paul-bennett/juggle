package com.angellane.juggle.parser;

import com.angellane.juggle.parser.DeclLexer;
import com.angellane.juggle.parser.DeclParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ParserTest {
    @Test
    public void testParser() {
        CharStream inputStream = CharStreams.fromString("foo bar baz");

        DeclLexer lexer = new DeclLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        DeclParser parser = new DeclParser(tokenStream);

        ParseTree tree = parser.decl();

        assertNotNull(tree);

        assert(lexer.nextToken().getType() == Token.EOF);

//        assert(parser.isMatchedEOF());
//        ParseTree expected = new ParseTree();
//
//        Visitor
    }
}
