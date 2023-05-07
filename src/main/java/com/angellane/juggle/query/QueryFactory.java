package com.angellane.juggle.query;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.Juggler;
import com.angellane.juggle.parser.DeclBaseListener;
import com.angellane.juggle.parser.DeclLexer;
import com.angellane.juggle.parser.DeclParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
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

    public Query createQuery(final String declString) {
        CharStream inputStream = CharStreams.fromString(declString);

        DeclLexer lexer = new DeclLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        DeclParser parser = new DeclParser(tokenStream);

        DeclParser.DeclContext tree = parser.decl();

        ParseTreeWalker walker = new ParseTreeWalker();
        Listener listener = new Listener();

        walker.walk(listener, tree);

        return listener.tempQuery;
    }

    Class<?> classForTypename(String className) {
        return juggler.classForTypename(className);
    }

    class Listener extends DeclBaseListener {
        // This is the object that the listener is constructing
        Query tempQuery;

        // tempQuery is either a ClassQuery or a MemberQuery; one of these
        // two fields will be the same as tempQuery, the other will be null.
        // This is just a convenient way of avoiding a lot of type casts.
        TypeQuery tempTypeQuery;
        MemberQuery tempMemberQuery;


        // DECLARATIONS =======================================================

        @Override
        public void enterClassDecl(DeclParser.ClassDeclContext ctx) {
            tempQuery = tempTypeQuery = new TypeQuery(TypeFlavour.CLASS);
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
        }

        @Override
        public void enterMemberDecl(DeclParser.MemberDeclContext ctx) {
            tempQuery = tempMemberQuery = new MemberQuery();
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
        public void enterMemberModifier(DeclParser.MemberModifierContext ctx) {
            if (ctx.annotation() != null)
                return;             // Annotations are handled elsewhere

            String text = ctx.getText();

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
                        System.err.println("*** Unknown modifier `" + ctx.getText() + "'; ignoring");
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
            return Pattern.compile(s, Pattern.LITERAL);
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


        // SUPERTYPES =========================================================

        @Override
        public void exitClassExtendsClause(DeclParser.ClassExtendsClauseContext ctx) {
            tempTypeQuery.setSupertype(tempType);
        }

        @Override
        public void enterClassImplementsClause(DeclParser.ClassImplementsClauseContext ctx) {
            tempTypeList.clear();
        }

        @Override
        public void exitClassImplementsClause(DeclParser.ClassImplementsClauseContext ctx) {
            tempTypeQuery.setSuperInterfaces(new HashSet<>(tempTypeList));
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
            tempType = BoundedType.wildcardType();
            tempTypeList.add(tempType);
        }


        // RETURN =============================================================

        @Override
        public void exitReturnType(DeclParser.ReturnTypeContext ctx) {
            tempMemberQuery.returnType = tempType;
        }


        // PARAMS =============================================================

        @Override
        public void enterParams(DeclParser.ParamsContext ctx) {
            // We've seen parentheses, so we're matching parameters
            tempMemberQuery.params = new ArrayList<>();
        }

        @Override
        public void exitEllipsisParam(DeclParser.EllipsisParamContext ctx) {
            tempMemberQuery.params.add(ParamSpec.ellipsis());
        }

        @Override
        public void exitWildcardParam(DeclParser.WildcardParamContext ctx) {
            tempMemberQuery.params.add(ParamSpec.wildcard());
        }

        @Override
        public void exitUnnamedParam(DeclParser.UnnamedParamContext ctx) {
            tempMemberQuery.params.add(ParamSpec.unnamed(tempType));
        }

        @Override
        public void exitUntypedParam(DeclParser.UntypedParamContext ctx) {
            tempMemberQuery.params.add(ParamSpec.untyped(tempName));
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
