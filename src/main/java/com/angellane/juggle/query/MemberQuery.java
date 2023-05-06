package com.angellane.juggle.query;

import com.angellane.juggle.candidate.CandidateMember;

import java.util.*;
import java.util.regex.Pattern;

/**
 * This class represents a declaration query -- the result of parsing a
 * pseudo-Java declaration that's subsequently used as a template against which
 * to match.
 */
public final class MemberQuery extends Query {
    public BoundedType returnType = null;

    public List<ParamSpec> params = null;
    public Set<BoundedType> exceptions = null;

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (!(other instanceof MemberQuery memberQuery)) {
            return false;
        } else {
            return modifierMask == memberQuery.modifierMask
                    && modifiers == memberQuery.modifiers
                    && Objects.equals(annotationTypes, memberQuery.annotationTypes)
                    && accessibility == memberQuery.accessibility
                    && Objects.equals(returnType, memberQuery.returnType)
                    && patternsEqual(this.declarationPattern,
                            memberQuery.declarationPattern)
                    && Objects.equals(params, memberQuery.params)
                    && Objects.equals(exceptions, memberQuery.exceptions);
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
    static boolean patternsEqual(Pattern a, Pattern b) {
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

    boolean matchesReturn(CandidateMember cm) {
        return this.returnType == null
                || this.returnType.matchesClass(cm.returnType());
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
                params.stream().anyMatch(p -> p instanceof ParamEllipsis);
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
}

