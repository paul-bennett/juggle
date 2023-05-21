package com.angellane.juggle.query;

import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.match.Match;

import java.util.*;
import java.util.stream.Stream;

import static com.angellane.juggle.util.Decomposer.decomposeIntoParts;

/**
 * This class represents a declaration query -- the result of parsing a
 * pseudo-Java declaration that's subsequently used as a template against which
 * to match.
 */
public final class MemberQuery extends Query<MemberCandidate> {
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

    @Override
    public
    <Q extends Query<MemberCandidate>, M extends Match<MemberCandidate,Q>>
    Stream<M> match(MemberCandidate candidate) {
        if (isMatchForCandidate(candidate)) {
            // TODO: implement scoring
            @SuppressWarnings("unchecked")      // TODO: remove this nasty cast
            M m = (M)new Match<>(candidate, this, 0);
            return Stream.of(m);
        }
        else
            return Stream.empty();
    }

    // TODO: make this method private, or remove it
    public boolean isMatchForCandidate(MemberCandidate cm) {
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

    boolean matchesParams(List<Class<?>> candidateParams) {
        if (params == null)
            return true;

        // params :: [ParamSpec]
        // type ParamSpec = ZeroOrMoreParams | SingleParam name type

        // Intent is to construct a number of alternative queries of type
        // List<SingleParam> by replacing the ZeroOrMoreParams objects with
        // a number of wildcard SingleParam objects.

        int numParamSpecs = (int)params.stream()
                .filter(p -> p instanceof SingleParam).count();
        int numEllipses = params.size() - numParamSpecs;
        int spareParams = candidateParams.size() - numParamSpecs;

        if (spareParams == 0)
            // No ellipses, correct #params
            return matchesParamSpecs(params.stream()
                            .filter(ps -> ps instanceof SingleParam)
                            .map(ps -> (SingleParam)ps).toList(),
                    candidateParams);
        else if (numEllipses == 0)
            // No ellipses over which to distribute spare params
            return false;
        else if (spareParams < 0)
            // More specified params than candidate params
            return false;
        else {
            // Nasty: using a 1-element array so we can set inside lambda
            final boolean[] ret = {false};

            decomposeIntoParts(spareParams, numEllipses, distribution -> {
                int i = 0;  // index into distribution
                List<SingleParam> queryParams = new ArrayList<>();

                for (ParamSpec ps : params) {
                    if (ps instanceof SingleParam singleParam)
                        queryParams.add(singleParam);
                    else
                        for (int numWildcards = distribution[i++];
                             numWildcards > 0; numWildcards--)
                            queryParams.add(ParamSpec.wildcard());
                }

                ret[0] |= matchesParamSpecs(queryParams, candidateParams);
            });

            return ret[0];
        }
    }

    private boolean matchesParamSpecs(List<SingleParam> queryParams,
                                      List<Class<?>>    candidateParams) {
        if (queryParams.size() != candidateParams.size())
            return false;
        else {
            Iterator<? extends Class<?>> actualParamIter =
                    candidateParams.iterator();
            return queryParams.stream().allMatch(ps -> {
                BoundedType bounds = ps.paramType();
                Class<?> actualType = actualParamIter.next();
                return bounds.matchesClass(actualType);
            });
        }
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

