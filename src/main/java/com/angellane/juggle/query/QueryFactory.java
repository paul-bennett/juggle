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

import com.angellane.juggle.JuggleError;
import com.angellane.juggle.Juggler;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.parser.DeclBaseListener;
import com.angellane.juggle.parser.DeclLexer;
import com.angellane.juggle.parser.DeclParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryFactory {
    Juggler juggler;

    public QueryFactory(Juggler juggler) {
        this.juggler = juggler;
    }

    public Query<?> createQuery(final String declString) {
        CharStream inputStream = CharStreams.fromString(declString);

        Lexer lexer = new Lexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        DeclParser parser = new DeclParser(tokenStream);
        parser.setErrorHandler(new AbortErrorStrategy(declString));

        DeclParser.OneDeclContext tree = parser.oneDecl();
        if (parser.getCurrentToken().getType() != Token.EOF)
            throw new JuggleError("Extra input after query: " + parser.getCurrentToken());

        ParseTreeWalker walker = new ParseTreeWalker();
        Listener listener = new Listener();

        walker.walk(listener, tree.decl());

        return listener.tempQuery;
    }

    static class Lexer extends DeclLexer {
        public Lexer(CharStream input) { super(input); }
        public void recover(LexerNoViableAltException ex) {
            throw new JuggleError("Couldn't parse query");

        }
    }

    static class AbortErrorStrategy extends DefaultErrorStrategy {
        String input;
        AbortErrorStrategy(String input) { this.input = input; }

        private String errorMessage(Parser parser) {
            Token errorToken = parser.getCurrentToken();
//            var expectedTokens = parser.getExpectedTokens();

            int row = errorToken.getLine();
            int col = errorToken.getCharPositionInLine();

            StringBuilder msg = new StringBuilder(
                    "Couldn't parse query at %d:%d\n".formatted(row,col));

            List<String> inputLines = input.lines().toList();
            for (int i = 0; i < inputLines.size(); ++i) {
                msg.append(inputLines.get(i)).append("\n");
                if (i == row - 1)
                    msg.append(" ".repeat(col))
                            .append("^".repeat(errorToken.getText().length()))
                            .append("\n");
            }
//            msg.append("Expected: ").append(expectedTokens);
            return msg.toString();
        }

        @Override
        public void recover(Parser parser, RecognitionException ex) {
            throw new JuggleError(errorMessage(parser));
        }

        @Override
        public Token recoverInline(Parser parser) throws RecognitionException {
            throw new JuggleError(errorMessage(parser));
        }

        @Override
        public void sync(Parser parser) throws RecognitionException {
            // Don't even try to recover from sub-rule problems!
        }
    }

    Class<?> classForTypename(String className) {
        return juggler.classForTypename(className);
    }

    class Listener extends DeclBaseListener {
        // This is the object that the listener is constructing
        Query<?> tempQuery;

        // tempQuery is either a ClassQuery or a MemberQuery; one of these
        // two fields will be the same as tempQuery, the other will be null.
        // This is just a convenient way of avoiding a lot of type casts.
        TypeQuery tempTypeQuery;
        MemberQuery tempMemberQuery;


        // DECLARATIONS =======================================================

        @Override
        public void enterClassDecl(DeclParser.ClassDeclContext ctx) {
            tempQuery = tempTypeQuery = new TypeQuery(TypeFlavour.CLASS);
            tempQuery.accessibility = Accessibility.PUBLIC;
        }

        @Override
        public void exitClassDecl(DeclParser.ClassDeclContext ctx) {
            super.exitClassDecl(ctx);
        }

        @Override
        public void enterInterfaceDecl(DeclParser.InterfaceDeclContext ctx) {
            tempQuery = tempTypeQuery = new TypeQuery(TypeFlavour.INTERFACE);
        }

        @Override
        public void enterAnnotationDecl(DeclParser.AnnotationDeclContext ctx) {
            tempQuery = tempTypeQuery = new TypeQuery(TypeFlavour.ANNOTATION);
        }

        @Override
        public void enterEnumDecl(DeclParser.EnumDeclContext ctx) {
            tempQuery = tempTypeQuery = new TypeQuery(TypeFlavour.ENUM);
        }

        @Override
        public void enterRecordDecl(DeclParser.RecordDeclContext ctx) {
            tempQuery = tempTypeQuery = new TypeQuery(TypeFlavour.RECORD);
            tempParams = null;
        }

        @Override
        public void exitRecordDecl(DeclParser.RecordDeclContext ctx) {
            tempTypeQuery.setRecordComponents(tempParams);
        }

        @Override
        public void enterMemberDecl(DeclParser.MemberDeclContext ctx) {
            tempQuery = tempMemberQuery = new MemberQuery();
            tempParams = null;
        }

        @Override
        public void exitMemberDecl(DeclParser.MemberDeclContext ctx) {
            tempMemberQuery.params = tempParams;
        }


        // MODIFIERS ==========================================================

        @Override
        public void enterAnnotation(DeclParser.AnnotationContext ctx) {
            String typeName = ctx.qname().IDENT().stream()
                    .map(TerminalNode::getSymbol)
                    .map(Token::getText)
                    .collect(Collectors.joining("."));

            Class<?> c = classForTypename(typeName);
            if (c != null)
                tempQuery.addAnnotationType(c);
        }

        @Override
        public void enterClassModifier(DeclParser.ClassModifierContext ctx) {
            if (ctx.annotation() != null)
                return;             // Annotations are handled elsewhere
            handleModifier(ctx.getText());
        }

        @Override
        public void enterInterfaceModifier(DeclParser.InterfaceModifierContext ctx) {
            if (ctx.annotation() != null)
                return;             // Annotations are handled elsewhere
            handleModifier(ctx.getText());
        }

        @Override
        public void enterAnnotationModifier(DeclParser.AnnotationModifierContext ctx) {
            if (ctx.annotation() != null)
                return;             // Annotations are handled elsewhere
            handleModifier(ctx.getText());
        }

        @Override
        public void enterMemberModifier(DeclParser.MemberModifierContext ctx) {
            if (ctx.annotation() != null)
                return;             // Annotations are handled elsewhere
            handleModifier(ctx.getText());
        }

        private void handleModifier(String text) {
            switch (text) {
                case "private", "package", "protected", "public" ->
                        tempQuery.setAccessibility(Accessibility.fromString(text));

                case "static"       -> tempQuery.addModifier(Modifier.STATIC);
                case "final"        -> tempQuery.addModifier(Modifier.FINAL);
                case "synchronized" -> tempQuery.addModifier(Modifier.SYNCHRONIZED);
                case "volatile"     -> tempQuery.addModifier(Modifier.VOLATILE);
                case "transient"    -> tempQuery.addModifier(Modifier.TRANSIENT);
                case "native"       -> tempQuery.addModifier(Modifier.NATIVE);
                case "abstract"     -> tempQuery.addModifier(Modifier.ABSTRACT);
                case "strictfp"     -> tempQuery.addModifier(Modifier.STRICT);

                default ->
                        System.err.println("*** Warning: unknown modifier `"
                                + text + "'; ignoring");
            }
        }


        // NAMES ==============================================================

        // This is populated by other listeners – either as a parameter name
        // or a method name
        private Pattern tempName = null;

        @Override
        public void enterUname(DeclParser.UnameContext ctx) {
            tempName = ctx.IDENT() != null
                    ? patternFromLiteral(ctx.IDENT().getText())
                    : patternFromRegex(ctx.REGEX().getText());
        }

        @Override
        public void exitDeclName(DeclParser.DeclNameContext ctx) {
            tempQuery.setNamePattern(tempName);
        }

        private Pattern patternFromLiteral(String s) {
            return Pattern.compile("^" + Pattern.quote(s) + "$");
        }

        private Pattern patternFromRegex(String re) {
            boolean caseInsensitive = re.endsWith("i");

            if (caseInsensitive)
                re = re.substring(0, re.length() - 1);

            assert(re.startsWith("/"));
            assert(re.endsWith("/"));

            re = re.substring(1, re.length() - 1);

            return Pattern.compile(re,
                    caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
        }


        // RELATED TYPES=======================================================

        @Override
        public void exitClassExtendsClause(DeclParser.ClassExtendsClauseContext ctx) {
            tempTypeQuery.setSupertype(tempType);
        }

        @Override
        public void enterImplementsClause(DeclParser.ImplementsClauseContext ctx) {
            tempTypeList.clear();
        }

        @Override
        public void exitImplementsClause(DeclParser.ImplementsClauseContext ctx) {
            tempTypeQuery.setSuperInterfaces(new HashSet<>(tempTypeList));
        }

        @Override
        public void enterInterfaceExtendsClause(DeclParser.InterfaceExtendsClauseContext ctx) {
            tempTypeList.clear();
        }

        @Override
        public void exitInterfaceExtendsClause(DeclParser.InterfaceExtendsClauseContext ctx) {
            tempTypeQuery.setSuperInterfaces(new HashSet<>(tempTypeList));
        }

        @Override
        public void enterPermitsClause(DeclParser.PermitsClauseContext ctx) {
            tempTypeList.clear();
        }

        @Override
        public void exitPermitsClause(DeclParser.PermitsClauseContext ctx) {
            tempTypeQuery.setPermittedSubtypes(new HashSet<>(tempTypeList));
        }


        // TYPE ===============================================================

        // This is populated by the type listeners, gathering information about
        // a return type, a parameter's type or an exception's type.
        private BoundedType             tempType        = null;

        // As types are seen they're added to this list too.  It's only used
        // by some rules (e.g. for collecting class supertypes)
        private final List<BoundedType> tempTypeList    = new ArrayList<>();

        @Override
        public void exitExactType(DeclParser.ExactTypeContext ctx) {
            Class<?> cls = juggler.classForTypename(ctx.qname().getText());

            // Now turn it into an array with as many dimensions as []s (dims),
            // plus one if there was an ellipsis

            for (var dims = ctx.dim().size() + (ctx.ELLIPSIS() != null ? 1 : 0);
                 dims > 0; --dims) {
                cls = cls.arrayType();
            }

            tempType = BoundedType.exactType(cls);
            tempTypeList.add(tempType);
        }

        @Override
        public void exitUpperBoundedType(DeclParser.UpperBoundedTypeContext ctx) {
            tempType = BoundedType.subtypeOf(
                    ctx.qname().stream()
                            .map(DeclParser.QnameContext::getText)
                            .map(juggler::classForTypename)
                            .collect(Collectors.toSet())
            );
            tempTypeList.add(tempType);
        }

        @Override
        public void exitLowerBoundedType(DeclParser.LowerBoundedTypeContext ctx) {
            tempType = BoundedType.supertypeOf(
                    juggler.classForTypename(ctx.qname().getText())
            );
            tempTypeList.add(tempType);
        }

        @Override
        public void exitUnboundedType(DeclParser.UnboundedTypeContext ctx) {
            tempType = BoundedType.unboundedWildcardType();
            tempTypeList.add(tempType);
        }


        // RETURN =============================================================

        @Override
        public void exitReturnType(DeclParser.ReturnTypeContext ctx) {
            tempMemberQuery.returnType = tempType;
        }


        // PARAMS =============================================================

        List<ParamSpec> tempParams;

        @Override
        public void enterParams(DeclParser.ParamsContext ctx) {
            // We've seen parentheses, so we're matching parameters
            tempParams = new ArrayList<>();
        }

        @Override
        public void exitEllipsisParam(DeclParser.EllipsisParamContext ctx) {
            tempParams.add(ParamSpec.ellipsis());
        }

        @Override
        public void exitWildcardParam(DeclParser.WildcardParamContext ctx) {
            tempParams.add(ParamSpec.wildcard());
        }

        @Override
        public void exitUnnamedParam(DeclParser.UnnamedParamContext ctx) {
            tempParams.add(ParamSpec.unnamed(tempType));
        }

        @Override
        public void exitUntypedParam(DeclParser.UntypedParamContext ctx) {
            tempParams.add(ParamSpec.untyped(tempName));
        }


        // EXCEPTIONS =========================================================


        @Override
        public void enterThrowsClause(DeclParser.ThrowsClauseContext ctx) {
            tempMemberQuery.exceptions = new HashSet<>();
        }

        @Override
        public void exitException(DeclParser.ExceptionContext ctx) {
            tempMemberQuery.exceptions.add(tempType);
        }
    }

}
