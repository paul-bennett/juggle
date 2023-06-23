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

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.candidate.Param;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.match.TypeMatcher;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.angellane.juggle.match.TypeMatcher.EXACT_MATCH;
import static com.angellane.juggle.match.TypeMatcher.NO_MATCH;
import static com.angellane.juggle.util.Decomposer.decomposeIntoParts;

public abstract sealed class Query<C extends Candidate>
        permits TypeQuery, MemberQuery {
    protected Set<Class<?>> annotationTypes     = null;
    protected Accessibility accessibility       = null;
    protected int           modifierMask        = 0;
    protected int           modifiers           = 0;
    protected Pattern       declarationPattern  = null;

    // For TypeQuery, this specifies the recordComponents
    public List<ParamSpec>  params              = null;

    private static final int OTHER_MODIFIERS_MASK =
            Candidate.OTHER_MODIFIERS_MASK;


    // ABSTRACT METHODS =======================================================

    /**
     * Does this query use bounded wildcards?  Juggler uses this information
     * when evaluating type matches.  If a query doesn't use bounded wildcards
     * (i.e. ? extends Foo, or ? super Bar) then Juggler will implicitly
     * convert queries to take widening and boxing conversions into account.
     *
     * @return true iff all uses of wildcards in this query are bounded
     */
    public abstract boolean hasBoundedWildcards();

    /**
     * Tries to match the candidate against this query.
     *
     * @param candidate the candidate to try to match against this query
     * @return Match (+score), or empty if #candidate doesn't match.
     */
    public abstract
    <Q extends Query<C>, M extends Match<C,Q>>
    Stream<M> match(TypeMatcher tm, C candidate);


    // FRAMEWORK ==============================================================

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (!(other instanceof Query<?> q))
            return false;
        else
            return Objects.equals(annotationTypes,       q.annotationTypes)
                    && accessibility                  == q.accessibility
                    && modifierMask                   == q.modifierMask
                    && modifiers                      == q.modifiers
                    && patternsEqual(declarationPattern, q.declarationPattern)
                    && Objects.equals(params,            q.params)
                    ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationTypes
                , accessibility
                , modifierMask
                , modifiers
                , declarationPattern
                , params
        );
    }


    // UTILITIES --------------------------------------------------------------

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


    // SETTERS ================================================================

    public void setAnnotationTypes(Set<Class<?>> annotationTypes) {
        this.annotationTypes = annotationTypes == null
                ? null
                : new HashSet<>(annotationTypes);
    } 

    public void setModifiersAndMask(int modifiers, int modifierMask) {
        this.modifiers    = modifiers;
        this.modifierMask = modifierMask;
    }

    public void setAccessibility(Accessibility accessibility) {
        this.accessibility = accessibility;
    }

    public void setNameExact(final String name) {
        setNamePattern(Pattern.compile("^" + Pattern.quote(name) + "$"));
    }

    public void setNamePattern(Pattern pattern) {
        this.declarationPattern = pattern;
    }


    // GETTERS ================================================================

    public Accessibility getAccessibility() { return this.accessibility; }


    // MATCHERS ===============================================================

    protected boolean matchesAnnotations(Set<Class<?>> annotationTypes) {
        return this.annotationTypes == null
                || annotationTypes.containsAll(this.annotationTypes);
    }

    protected boolean matchesAccessibility(Accessibility access) {
        return this.accessibility == null
                || access.isAtLeastAsAccessibleAsOther(this.accessibility);
    }

    protected boolean matchesModifiers(int otherMods) {
        final int mask = modifierMask & OTHER_MODIFIERS_MASK;
        return (mask & modifiers) == (mask & otherMods);
    }

    protected boolean matchesName(String simpleName, String canonicalName) {
        return this.declarationPattern == null
                || simpleName != null && this.declarationPattern.matcher(simpleName).find()
                || canonicalName != null && this.declarationPattern.matcher(canonicalName).find();
    }


    // SCORING ================================================================

    /**
     * Computes a total score from a list of scores.  If any of the scores
     * are empty, the total is empty.  Otherwise, it's the sum of all scores.
     *
     * @param scores the component scores to add up
     * @return Optional.empty() if any score is empty, or the sum otherwise.
     */
    public static OptionalInt totalScore(
            List<OptionalInt> scores
    ) {
        return scores.stream()
                .reduce(OptionalInt.of(0),
                        (a,b) -> a.isEmpty() || b.isEmpty()
                                ? OptionalInt.empty()
                                : OptionalInt.of(a.getAsInt() + b.getAsInt())
                );
    }

    protected OptionalInt scoreAnnotations(Set<Class<?>> annotationTypes) {
        return matchesAnnotations(annotationTypes)
                ? OptionalInt.of(0) : OptionalInt.empty();
    }

    protected OptionalInt scoreAccessibility(Accessibility access) {
        return matchesAccessibility(access)
                ? OptionalInt.of(0) : OptionalInt.empty();
    }

    protected OptionalInt scoreModifiers(int otherMods) {
        return matchesModifiers(otherMods)
                ? OptionalInt.of(0) : OptionalInt.empty();
    }

    protected OptionalInt scoreName(String simpleName, String canonicalName) {
        return matchesName(simpleName, canonicalName)
                ? OptionalInt.of(0) : OptionalInt.empty();
    }

    protected OptionalInt scoreParams(
            TypeMatcher tm, List<Param> candidateParams) {
        if (params == null)
            return EXACT_MATCH;

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
            return scoreParamSpecs(tm,
                    params.stream()
                            .filter(ps -> ps instanceof SingleParam)
                            .map(ps -> (SingleParam)ps)
                            .toList(),
                    candidateParams
            );
        else if (numEllipses == 0)
            // No ellipses over which to distribute spare params
            return NO_MATCH;
        else if (spareParams < 0)
            // More specified params than candidate params
            return NO_MATCH;
        else {
            // Nasty: using a 1-element array so we can set inside lambda
            final OptionalInt [] ret = new OptionalInt[]{ NO_MATCH };

            decomposeIntoParts(spareParams, numEllipses, distribution -> {
                int ix = 0;  // index into distribution
                List<SingleParam> queryParams = new ArrayList<>();

                for (ParamSpec ps : params) {
                    if (ps instanceof SingleParam singleParam)
                        queryParams.add(singleParam);
                    else
                        for (int numWildcards = distribution[ix++];
                             numWildcards > 0; numWildcards--)
                            queryParams.add(ParamSpec.wildcard());
                }

                OptionalInt  thisScore =
                        scoreParamSpecs(tm, queryParams, candidateParams);

                ret[0] = IntStream.concat(ret[0].stream(), thisScore.stream())
                        .max();
            });

            return ret[0];
        }
    }

    private final static String thisPattern =
            QueryFactory.patternFromLiteral("this").toString();

    private static OptionalInt scoreParamSpecs(
            TypeMatcher       tm,
            List<SingleParam> queryParams,
            List<Param>       candidateParams
    ) {
        if (queryParams.size() != candidateParams.size())
            return NO_MATCH;
        else {
            Iterator<Param> actualParamIter = candidateParams.iterator();

            return totalScore(
                    queryParams.stream()
                            .map(queryParam -> {
                                Param actualParam = actualParamIter.next();

                                if (queryParam.annotations() != null &&
                                    !actualParam.annotations()
                                            .containsAll(
                                                    queryParam.annotations())
                                )
                                    return NO_MATCH;

                                if ((actualParam.otherModifiers()
                                        & queryParam.modifiersMask())
                                        != queryParam.modifiers())
                                    return NO_MATCH;

                                if (actualParam.name() != null) {
                                    if (!queryParam.paramName()
                                            .matcher(actualParam.name()).find())
                                        return NO_MATCH;
                                }
                                else if (thisPattern.equals(
                                            queryParam.paramName().toString())
                                    )
                                    // User was looking for `this`, but actual
                                    // param name missing, so we fail it.
                                    // (Actual name should always be present
                                    // for `this` since we set it when creating
                                    // candidate
                                    return NO_MATCH;

                                // Now check on the type

                                BoundedType bounds = queryParam.paramType();
                                return tm.scoreTypeMatch(
                                        actualParam.type(), bounds);
                            })
                            .toList()
            );
        }
    }
}
