package com.angellane.juggle.processor;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.CandidateMember;
import com.angellane.juggle.Juggler;
import com.angellane.juggle.parser.DeclBaseListener;
import com.angellane.juggle.parser.DeclLexer;
import com.angellane.juggle.parser.DeclParser;
import com.angellane.juggle.parser.DeclParser.DeclContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class represents a declaration query -- the result of parsing a pseudo-Java declaration that's
 * subsequently used as a template against which to match.
 */
public class DeclQuery {
    public record BoundedType(
            Set<Class<?>> upperBound,   // extends/implements
            Class<?> lowerBound         // super
    ) {
        public static BoundedType exactType(Class<?> c) {
            return new BoundedType(Set.of(c), c);
        }
        public static BoundedType subtypeOf(Class<?> c) {
            return new BoundedType(Set.of(c), null);
        }
        public static BoundedType subtypeOf(Set<Class<?>> cs) {
            return new BoundedType(cs, null);
        }
        public static BoundedType subtypeOf(Class<?>... cs) {
            return new BoundedType(Set.of(cs), null);
        }

        public static BoundedType supertypeOf(Class<?> c) {
            return new BoundedType(null, c);
        }

        public static BoundedType wildcardType() {
            return new BoundedType(null, null);
        }

        public boolean matchesClass(Class<?> candidate) {
            return (lowerBound  == null || candidate.isAssignableFrom(lowerBound)) &&
                    (upperBound == null || upperBound.stream().allMatch(b -> b.isAssignableFrom(candidate)));
        }
    }

    public sealed interface ParamSpec permits Ellipsis, SingleParam {
        static ParamSpec ellipsis() { return new Ellipsis(); }
        static ParamSpec wildcard() {
            return new SingleParam(
                    Pattern.compile(""), BoundedType.wildcardType());
        }
        static ParamSpec unnamed(BoundedType bt) {
            return new SingleParam(Pattern.compile(""), bt);
        }
        static ParamSpec untyped(Pattern pat) {
            return new SingleParam(pat, BoundedType.wildcardType());
        }
        static ParamSpec param(String name, Class<?> type) {
            return new SingleParam(Pattern.compile("^" + name + "$"),
                    new BoundedType(Set.of(type), type));
        }

    }
    public record Ellipsis() implements ParamSpec {}
    public record SingleParam(
            Pattern paramName,
            BoundedType paramType
    ) implements ParamSpec {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SingleParam that = (SingleParam) o;
            return patternsEqual(paramName, that.paramName) && Objects.equals(paramType, that.paramType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(paramName, paramType);
        }
    }

    public Juggler juggler;

    public Set<Class<?>> annotationTypes = null;
    public Accessibility accessibility = null;
    public int modifierMask = 0, modifiers = 0;
    public BoundedType returnType = null;

    public Pattern declarationPattern = null;

    public List<ParamSpec> params = null;
    public Set<BoundedType> exceptions = null;

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (!(other instanceof DeclQuery declQuery)) {
            return false;
        } else {
            return modifierMask == declQuery.modifierMask
                    && modifiers == declQuery.modifiers
                    && Objects.equals(annotationTypes, declQuery.annotationTypes)
                    && accessibility == declQuery.accessibility
                    && Objects.equals(returnType, declQuery.returnType)
                    && patternsEqual(this.declarationPattern,
                            declQuery.declarationPattern)
                    && Objects.equals(params, declQuery.params)
                    && Objects.equals(exceptions, declQuery.exceptions);
        }
    }

    @Override
    public String toString() {
        return "DeclQuery{" +
                "annotationTypes=" + annotationTypes +
                ", accessibility=" + accessibility +
                ", modifierMask=" + modifierMask +
                ", modifiers=" + modifiers +
                ", returnType=" + returnType +
                ", declarationPattern=" + declarationPattern +
                ", params=" + params +
                ", exceptions=" + exceptions +
                '}';
    }

    /*
     * java.util.regex.Pattern doesn't provide a meaningful equality
     * test, so we convert both sides to Strings and hope for the best
     */
    private static boolean patternsEqual(Pattern a, Pattern b) {
        return (a != null && b != null)
                ? (Objects.equals(a.pattern(), b.pattern())
                    && (a.flags() == b.flags()))
                : (a == b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                annotationTypes,
                accessibility,
                modifierMask, modifiers,
                returnType,
                declarationPattern,
                params,
                exceptions);
    }

    Class<?> classForTypename(String className) {
        return juggler.classForTypename(className);
    }

    public void addAnnotationType(Class<?> annotationType) {
        if (annotationTypes == null)
            annotationTypes = new HashSet<>();
        annotationTypes.add(annotationType);
    }

    public void addModifier(int modifier, boolean val) {
        // First clear this modifier bit, then add it if necessary
        this.modifiers = (this.modifiers & ~modifier) | (val ? modifier : 0);
        this.modifierMask |= modifier;
    }

    public void addModifier(int modifier) { addModifier(modifier, true); }

    public void setAccessibility(Accessibility accessibility) {
        this.accessibility = accessibility;
    }

    public void setNameExact(final String name) {
        setNamePattern(Pattern.compile(name, Pattern.LITERAL));
    }

    public void setNamePattern(Pattern pattern) {
        this.declarationPattern = pattern;
    }

    public boolean isMatchForCandidate(CandidateMember cm) {
        return cm.matchesAccessibility(accessibility)
                && cm.matchesModifiers(modifierMask, modifiers)
                && matchesAnnotations(cm)
                && matchesReturn(cm)
                && matchesName(cm)
                && matchesParams(cm)
                && matchesExceptions(cm)
        ;
    }

    boolean matchesAnnotations(CandidateMember cm) {
        return this.annotationTypes == null
                || cm.annotationTypes().containsAll(this.annotationTypes);
    }

    boolean matchesReturn(CandidateMember cm) {
        return this.returnType == null
                || this.returnType.matchesClass(cm.returnType());
    }

    boolean matchesName(CandidateMember cm) {
        return this.declarationPattern == null
                || this.declarationPattern.matcher(cm.member().getName()).find();
    }

    boolean matchesParams(CandidateMember cm) {
        if (params == null)
            return true;

        // params :: [ParamSpec]
        // type ParamSpec = Ellipsis | SingleParam name type
        // "Ellipsis" stands for zero or more actual parameters

        // Right now, we'll just do the simplest: check that the candidate has
        // at least as many actual parameters as we have SingleParams.

        boolean hasEllipsis =
                params.stream().anyMatch(p -> p instanceof Ellipsis);
        long numParamSpecs =
                params.stream().filter(p -> p instanceof SingleParam).count();

        long numActualParams = cm.paramTypes().size();

        if (!hasEllipsis) {
            if (numActualParams != numParamSpecs)
                return false;
            else {
                Iterator<? extends Class<?>> actualParamIter =
                        cm.paramTypes().iterator();
                return params.stream().allMatch(ps -> {
                    // Cast is OK because we tested hasEllipsis
                    BoundedType bounds = ((SingleParam) ps).paramType();
                    Class<?> actualType = actualParamIter.next();
                    return bounds.matchesClass(actualType);
                });
            }
        }
        else
            return numActualParams >= numParamSpecs;
    }

    boolean matchesExceptions(CandidateMember cm) {
        // Need to check both ways:
        //  1. Is everything thrown by the query also thrown by the candidate?
        //  2. Is everything thrown by the candidate also thrown by the query?
        return this.exceptions == null
                || this.exceptions.stream()
                    .allMatch(ex -> cm.throwTypes().stream()
                            .anyMatch(ex::matchesClass)
                    )
                && cm.throwTypes().stream()
                    .allMatch(ex1 -> this.exceptions.stream()
                            .anyMatch(ex2 -> ex2.matchesClass(ex1))
                    )
                ;
    }

    public DeclQuery() {}

    public DeclQuery(final Juggler juggler) {
        this.juggler = juggler;
    }

    public DeclQuery(final Juggler juggler, final String declString) {
        this(juggler);

        CharStream inputStream = CharStreams.fromString(declString);

        DeclLexer lexer = new DeclLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        DeclParser parser = new DeclParser(tokenStream);

        DeclContext tree = parser.decl();

        ParseTreeWalker walker = new ParseTreeWalker();
        Listener listener = new Listener();

        walker.walk(listener, tree);    // Fills out the fields of this
    }

    class Listener extends DeclBaseListener {
        // Add methods here that attach elements to this

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
        public void enterModifier(DeclParser.ModifierContext ctx) {
            String text = ctx.getText();

            switch (text) {
                case "private", "package", "protected", "public" ->
                        setAccessibility(Accessibility.fromString(text));

                case "abstract"     -> addModifier(Modifier.ABSTRACT);
                case "static"       -> addModifier(Modifier.STATIC);
                case "final"        -> addModifier(Modifier.FINAL);
                case "native"       -> addModifier(Modifier.NATIVE);
                case "strictfp"     -> addModifier(Modifier.STRICT);
                case "synchronized" -> addModifier(Modifier.SYNCHRONIZED);

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
        public void exitMethodName(DeclParser.MethodNameContext ctx) {
            setNamePattern(tempName);
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


        // TYPE ===============================================================

        // This is populated by the type listeners, gathering information about
        // a return type, a parameter's type or an exception's type.
        private BoundedType tempType = null;

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
        }

        @Override
        public void exitUpperBoundedType(DeclParser.UpperBoundedTypeContext ctx) {
            tempType = BoundedType.subtypeOf(
                    ctx.qname().stream()
                            .map(DeclParser.QnameContext::getText)
                            .map(juggler::classForTypename)
                            .collect(Collectors.toSet())
            );
        }

        @Override
        public void exitLowerBoundedType(DeclParser.LowerBoundedTypeContext ctx) {
            tempType = BoundedType.supertypeOf(
                    juggler.classForTypename(ctx.qname().getText())
            );
        }

        @Override
        public void exitUnboundedType(DeclParser.UnboundedTypeContext ctx) {
            tempType = BoundedType.wildcardType();
        }


        // RETURN =============================================================

        @Override
        public void exitReturnType(DeclParser.ReturnTypeContext ctx) {
            returnType = tempType;
        }


        // PARAMS =============================================================

        @Override
        public void enterParams(DeclParser.ParamsContext ctx) {
            // We've seen parentheses, so we're matching parameters
            params = new ArrayList<>();
        }

        @Override
        public void exitEllipsisParam(DeclParser.EllipsisParamContext ctx) {
            params.add(ParamSpec.ellipsis());
        }

        @Override
        public void exitWildcardParam(DeclParser.WildcardParamContext ctx) {
            params.add(ParamSpec.wildcard());
        }

        @Override
        public void exitUnnamedParam(DeclParser.UnnamedParamContext ctx) {
            params.add(ParamSpec.unnamed(tempType));
        }

        @Override
        public void exitUntypedParam(DeclParser.UntypedParamContext ctx) {
            params.add(ParamSpec.untyped(tempName));
        }


        // EXCEPTIONS =========================================================


        @Override
        public void enterThrowsClause(DeclParser.ThrowsClauseContext ctx) {
            exceptions = new HashSet<>();
        }

        @Override
        public void exitException(DeclParser.ExceptionContext ctx) {
            exceptions.add(tempType);
        }
    }
}

