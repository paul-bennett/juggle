package com.angellane.juggle.processor;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.CandidateMember;
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

        public static BoundedType supertypeOf(Class<?> c) {
            return new BoundedType(null, c);
        }

        public boolean matchesClass(Class<?> candidate) {
            // TODO: consider what to do about conversions here (esp boxing/unboxing)
            return (lowerBound  == null || candidate.isAssignableFrom(lowerBound)) &&
                    (upperBound == null || upperBound.stream().allMatch(b -> b.isAssignableFrom(candidate)));
        }
    }

    public sealed interface ParamSpec permits Ellipsis, SingleParam {
        static ParamSpec ellipsis() { return new Ellipsis(); }
        static ParamSpec param(String name, Class<?> type) {
            return new SingleParam(Pattern.compile("^" + name + "$"),
                    new BoundedType(Set.of(type), type));
        }

    }
    public record Ellipsis() implements ParamSpec {}
    public record SingleParam(
            Pattern paramName,
            BoundedType paramType
    ) implements ParamSpec {}

    public ClassLoader loader = getClass().getClassLoader();

    public DeclQuery(ClassLoader loader) {
        this.loader = loader;
    }

    public Set<Class<?>> annotationTypes = null;    // TODO: consider Set<Class<? extends Annotation>>
    public Accessibility accessibility = null;
    public int modifierMask = 0, modifiers = 0;
    public BoundedType returnType = null;

    public Pattern declarationName = null;

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
                    && Objects.equals(declarationName, declQuery.declarationName)
                    && Objects.equals(params, declQuery.params)
                    && Objects.equals(exceptions, declQuery.exceptions);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                annotationTypes,
                accessibility,
                modifierMask, modifiers,
                returnType,
                declarationName,
                params,
                exceptions);
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
        return this.declarationName == null
                || this.declarationName.matcher(cm.member().getName()).matches();
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
                    // TODO: check parameter names as well
                    // Cast is OK because we tested hasEllipsis
                    BoundedType bounds = ((SingleParam) ps).paramType();
                    Class<?> actualType = actualParamIter.next();
                    return bounds.matchesClass(actualType);
                });
            }
        }
        else
            // TODO: handle ellipsis properly; should check all actual params
            return numActualParams >= numParamSpecs;
    }

    boolean matchesExceptions(CandidateMember cm) {
        return this.exceptions == null
                || this.exceptions.stream().allMatch(ex -> cm.throwTypes().stream().anyMatch(ex::matchesClass));
    }

    public DeclQuery() {}

    public DeclQuery(final String declString) {
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

            try {
                // TODO: resolve imported package names (maybe lookup via Juggler?)
                Class<?> annotationType = loader.loadClass(typeName);
                addAnnotationType(annotationType);
            }
            catch (ClassNotFoundException ex) {
                System.err.println("*** Couldn't load annotation `" + typeName + "'; ignoring");
            }
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
    }
}

