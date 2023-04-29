package com.angellane.juggle.processor;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.CandidateMember;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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

    public Set<Class<?>> annotationTypes = null;    // TODO: consider Set<Class<? extends Annotation>>
    public Accessibility accessibility = null;
    public int modifierMask = 0, modifiers = 0;
    public BoundedType returnType = null;

    public Pattern declarationName = null;

    public List<ParamSpec> params = null;
    public Set<BoundedType> exceptions = null;

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

    DeclQuery() {}

    public DeclQuery(final String declString) {
        if (!declString.isEmpty())
            System.err.println("QUERY STRING: `" + declString + "'");

        // TODO: parse declString and fill out fields of this
    }
}
