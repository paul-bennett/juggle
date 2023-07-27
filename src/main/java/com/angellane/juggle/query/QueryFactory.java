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

import com.angellane.backport.jdk11.java.lang.StringExtras;
import com.angellane.backport.jdk17.java.lang.ClassExtras;
import com.angellane.juggle.JuggleError;
import com.angellane.juggle.Juggler;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.parser.DeclBaseListener;
import com.angellane.juggle.parser.DeclLexer;
import com.angellane.juggle.parser.DeclParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.util.*;
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
                    "Couldn't parse query at " + row + ":" + col + "\n");

            List<String> inputLines = Arrays.asList(input.split("[\r\n]"));
            for (int i = 0; i < inputLines.size(); ++i) {
                msg.append(inputLines.get(i)).append("\n");
                if (i == row - 1)
                    msg.append(StringExtras.repeat(" ", col))
                            .append(StringExtras.repeat("^", errorToken.getText().length()))
                            .append("\n");
            }
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

    public static Pattern patternFromLiteral(String s) {
        return Pattern.compile("^" + Pattern.quote(s) + "$");
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

        private Set<Class<?>> tempAnnotations = null;

        private void clearAnnotations() { tempAnnotations = null; }

        private void addAnnotationType(Class<?> annotationType) {
            if (tempAnnotations == null)
                tempAnnotations = new HashSet<>();
            tempAnnotations.add(annotationType);

            if (!Annotation.class.isAssignableFrom(annotationType))
                juggler.warn(
                        "`" + annotationType.getCanonicalName()
                                + "' is not an annotation interface;"
                                + " won't match anything"
                        );
            else {
                RetentionPolicy rp =
                        Arrays.stream(annotationType.getAnnotations())
                                .filter(a -> a instanceof Retention)
                                .map(a -> ((Retention) a).value())
                                .findFirst()
                                .orElse(RetentionPolicy.CLASS);

                RetentionPolicy requiredPolicy = RetentionPolicy.RUNTIME;
                if (rp != requiredPolicy)
                    juggler.warn(
                            "`@interface " + annotationType.getCanonicalName()
                                    + "' has `" + rp + "' retention policy;"
                                    + " won't match anything"
                                    + " (only `" + requiredPolicy + "' policy works)"
                    );
            }
        }

        private int tempModifiers, tempModifiersMask;
        private void clearOtherModifiers() {
            tempModifiers = tempModifiersMask = 0;
        }

        private void addOtherModifier(int modifier,
                                      @SuppressWarnings("SameParameterValue")
                                      boolean val) {
            // First clear this modifier bit, then add it if necessary
            this.tempModifiers = (this.tempModifiers & ~modifier) | (val ? modifier : 0);
            this.tempModifiersMask |= modifier;
        }

        private void addOtherModifier(int modifier) {
            addOtherModifier(modifier, true);
        }

        // Clears all modifiers (other modifiers & annotations)
        private void clearAllModifiers() {
            clearAnnotations();
            clearOtherModifiers();
        }

        // Copy modifiers & annotations from this class to the relevant query
        private void attachModifiersToQuery() {
            this.tempQuery.setModifiersAndMask(
                    this.tempModifiers, this.tempModifiersMask);
            this.tempQuery.setAnnotationTypes(this.tempAnnotations);
        }


        @Override
        public void exitAnnotationModifiers(DeclParser.AnnotationModifiersContext ctx) {
            attachModifiersToQuery();
        }

        @Override
        public void exitClassModifiers(DeclParser.ClassModifiersContext ctx) {
            attachModifiersToQuery();
        }

        @Override
        public void exitInterfaceModifiers(DeclParser.InterfaceModifiersContext ctx) {
            attachModifiersToQuery();
        }

        @Override
        public void exitMemberModifiers(DeclParser.MemberModifiersContext ctx) {
            attachModifiersToQuery();
        }

        @Override
        public void enterAnnotation(DeclParser.AnnotationContext ctx) {
            String typeName = ctx.qname().IDENT().stream()
                    .map(TerminalNode::getSymbol)
                    .map(Token::getText)
                    .collect(Collectors.joining("."));

            Class<?> c = classForTypename(typeName);
            if (c != null)
                addAnnotationType(c);
        }

        @Override
        public void enterClassModifier(DeclParser.ClassModifierContext ctx) {
            if (ctx.annotation() == null) handleModifier(ctx.getText());
        }

        @Override
        public void enterInterfaceModifier(DeclParser.InterfaceModifierContext ctx) {
            if (ctx.annotation() == null) handleModifier(ctx.getText());
        }

        @Override
        public void enterAnnotationModifier(DeclParser.AnnotationModifierContext ctx) {
            if (ctx.annotation() == null) handleModifier(ctx.getText());
        }

        @Override
        public void enterMemberModifier(DeclParser.MemberModifierContext ctx) {
            if (ctx.annotation() == null) handleModifier(ctx.getText());
        }

        @Override
        public void enterParamModifier(DeclParser.ParamModifierContext ctx) {
            if (ctx.annotation() == null) handleModifier(ctx.getText());
        }

        @Override
        public void enterRecordCompModifier(DeclParser.RecordCompModifierContext ctx) {
            if (ctx.annotation() == null) handleModifier(ctx.getText());
        }

        private void handleModifier(String text) {
            switch (text) {
                case "private":
                case "package":
                case "protected":
                case "public":
                    tempQuery.setAccessibility(Accessibility.fromString(text)); break;

                // This group of modifiers apply to members and parameters
                case "static"       : addOtherModifier(Modifier.STATIC);        break;
                case "final"        : addOtherModifier(Modifier.FINAL);         break;
                case "synchronized" : addOtherModifier(Modifier.SYNCHRONIZED);  break;
                case "volatile"     : addOtherModifier(Modifier.VOLATILE);      break;
                case "transient"    : addOtherModifier(Modifier.TRANSIENT);     break;
                case "native"       : addOtherModifier(Modifier.NATIVE);        break;
                case "abstract"     : addOtherModifier(Modifier.ABSTRACT);      break;
                case "strictfp"     : addOtherModifier(Modifier.STRICT);        break;

                // The grammar restricts these to type queries only
                case "sealed"       : tempTypeQuery.setIsSealed(true);          break;
                case "non-sealed"   : tempTypeQuery.setIsSealed(false);         break;

                default:
                        juggler.warn("Unknown modifier `" + text + "'; ignoring");
            }
        }


        // NAMES ==============================================================

        // This is populated by other listeners – either as a parameter name
        // or a method name
        private Pattern tempName = null;

        private void clearName() {
            tempName = null;
        }

        @Override
        public void enterUname(DeclParser.UnameContext ctx) {
            tempName = ctx.IDENT() != null
                    ? patternFromLiteral(ctx.IDENT().getText())
                    : patternFromRegex(ctx.REGEX().getText());
        }

        @Override
        public void exitTypeDeclName(DeclParser.TypeDeclNameContext ctx) {
            if (ctx.REGEX() != null)
                tempQuery.setNamePattern(patternFromRegex(ctx.REGEX().getText()));
            else {
                String nameText = ctx.IDENT().stream()
                        .map(TerminalNode::getText)
                        .collect(Collectors.joining("."));
                tempQuery.setNamePattern(patternFromLiteral(nameText));
            }
        }

        @Override
        public void exitMemberDeclName(DeclParser.MemberDeclNameContext ctx) {
            tempQuery.setNamePattern(tempName);
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
        public void exitSuperClause(DeclParser.SuperClauseContext ctx) {
            // Yes, terminology is confusing here.  "class super Foo" is a
            // query to show types that are supertypes of Foo, i.e. types
            // that have Foo as a subtype.

            tempTypeQuery.setSubtype(tempType);
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

            int numDims = ctx.dim().size() + (ctx.ELLIPSIS() != null ? 1 : 0);

            if (numDims > 0 && cls.equals(Void.TYPE))
                throw new JuggleError("Can't have an array with element type `"
                        + cls.getCanonicalName() + "'"
                );

            for (int dims = numDims; dims > 0; --dims) {
                cls = ClassExtras.arrayType(cls);
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
                            .collect(Collectors.toList())
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

        private void clearParamList() { tempParams = new ArrayList<>(); }

        @Override
        public void enterParams(DeclParser.ParamsContext ctx) {
            clearParamList();
        }

        @Override
        public void enterRecordComps(DeclParser.RecordCompsContext ctx) {
            clearParamList();
        }

        @Override
        public void enterParam(DeclParser.ParamContext ctx) {
            clearAllModifiers();
            clearName();
        }

        @Override
        public void enterRecordComp(DeclParser.RecordCompContext ctx) {
            clearAllModifiers();
            clearName();
        }

        @Override
        public void exitEllipsisType(DeclParser.EllipsisTypeContext ctx) {
            tempParams.add(ParamSpec.ellipsis());
        }

        @Override
        public void exitWildcardType(DeclParser.WildcardTypeContext ctx) {
            tempParams.add(ParamSpec.wildcard(tempAnnotations, tempModifiers, tempModifiersMask));
        }

        private void addParam() {
            // This is actually a parameter or a record component.
            // We can't use `void` in this context

            if (tempType != null) {
                Class<?> lb = tempType.lowerBound();
                Set<Class<?>> ub = tempType.upperBound();

                if ((lb != null && lb == Void.TYPE) ||
                        (ub != null && ub.stream().anyMatch(
                                c -> c.equals(Void.TYPE)
                        ))
                )
                    throw new JuggleError(
                            "Can't use `" + Void.TYPE.getCanonicalName()
                                    + "' in a parameter"
                                    + " or record component type"
                    );
            }

            tempParams.add(ParamSpec.param(tempAnnotations,
                    tempModifiers, tempModifiersMask, tempType, tempName));
        }

        @Override
        public void exitUnnamedType(DeclParser.UnnamedTypeContext ctx) {
            addParam();
        }

        @Override
        public void exitUntypedName(DeclParser.UntypedNameContext ctx) {
            addParam();
        }


        // EXCEPTIONS =========================================================


        @Override
        public void enterThrowsClause(DeclParser.ThrowsClauseContext ctx) {
            tempMemberQuery.exceptions = new HashSet<>();
        }

        @Override
        public void exitException(DeclParser.ExceptionContext ctx) {
            Class<?> lb = tempType.lowerBound();
            Set<Class<?>> ub = tempType.upperBound();

            // Check that:
            // 1. Lower bound is a class that extends Throwable
            // 2. All non-interface upper bounds extend Throwable
            // Be sure to only emit one warning if UB=LB

            if (lb != null && !Throwable.class.isAssignableFrom(lb))
                juggler.warn(
                        "`" + lb.getCanonicalName() + "' is not a Throwable;"
                                + " won't match anything"
                );
            else if (ub != null)
                ub.forEach(c -> {
                    if (c != null
                            && !c.isInterface()
                            && !Throwable.class.isAssignableFrom(c))
                        juggler.warn(
                                "`" + c.getCanonicalName() +  "' is not a Throwable;"
                                        + " won't match anything"
                        );
                });

            tempMemberQuery.exceptions.add(tempType);
        }
    }
}
