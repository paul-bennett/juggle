package com.angellane.juggle.query;

import com.angellane.juggle.candidate.CandidateMember;

import java.util.*;

/**
 * This class represents a declaration query -- the result of parsing a
 * pseudo-Java declaration that's subsequently used as a template against which
 * to match.
 */
public final class MemberQuery extends Query {
    public BoundedType      returnType  = null;

    public List<ParamSpec>  params      = null;
    public Set<BoundedType> exceptions  = null;

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (!(other instanceof MemberQuery q))
            return false;
        else
            return super.equals(q)
                    && Objects.equals(returnType,   q.returnType)
                    && Objects.equals(params,       q.params)
                    && Objects.equals(exceptions,   q.exceptions)
                    ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode()
                , returnType
                , params
                , exceptions
        );
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

    public boolean isMatchForCandidate(CandidateMember cm) {
        return matchesAnnotations(cm.annotationTypes())
                && matchesAccessibility(cm.accessibility())
                && matchesModifiers(cm.otherModifiers())
                && matchesReturn(cm.returnType())
                && matchesName(cm.declarationName())
                && matchesParams(cm.paramTypes())
                && matchesExceptions(cm.throwTypes())
        ;
    }

    boolean matchesReturn(Class<?> returnType) {
        return this.returnType == null
                || this.returnType.matchesClass(returnType);
    }

    boolean matchesParams(List<Class<?>> paramTypes) {
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

        long numActualParams = paramTypes.size();

        if (!hasEllipsis) {
            if (numActualParams != numParamSpecs)
                return false;
            else {
                Iterator<? extends Class<?>> actualParamIter =
                        paramTypes.iterator();
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

    boolean matchesExceptions(Set<Class<?>> exceptions) {
        // Need to check both ways:
        //  1. Is everything thrown by the query also thrown by the candidate?
        //  2. Is everything thrown by the candidate also thrown by the query?
        return this.exceptions == null
                || this.exceptions.stream()
                    .allMatch(ex -> exceptions.stream()
                            .anyMatch(ex::matchesClass)
                    )
                && exceptions.stream()
                    .allMatch(ex1 -> this.exceptions.stream()
                            .anyMatch(ex2 -> ex2.matchesClass(ex1))
                    )
                ;
    }
}

