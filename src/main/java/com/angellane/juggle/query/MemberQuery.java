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

import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.match.TypeMatcher;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

import static com.angellane.juggle.match.TypeMatcher.EXACT_MATCH;
import static com.angellane.juggle.match.TypeMatcher.NO_MATCH;

/**
 * This class represents a declaration query -- the result of parsing a
 * pseudo-Java declaration that's subsequently used as a template against which
 * to match.
 */
public final class MemberQuery extends Query<MemberCandidate> {
    public Boolean          isDefault   = null;
    public BoundedType      returnType  = null;
    public Set<BoundedType> exceptions  = null;

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (!(other instanceof MemberQuery q))
            return false;
        else
            return super.equals(q)
                    && Objects.equals(isDefault,  q.isDefault)
                    && Objects.equals(returnType, q.returnType)
                    && Objects.equals(exceptions, q.exceptions)
                    ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode()
                , isDefault
                , returnType
                , exceptions
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "annotationTypes=" + annotationTypes +
                ", accessibility=" + accessibility +
                ", modifierMask=" + modifierMask +
                ", modifiers=" + modifiers +
                ", isDefault=" + isDefault + 
                ", returnType=" + returnType +
                ", declarationPattern=" + declarationPattern +
                ", params=" + params +
                ", exceptions=" + exceptions +
                '}';
    }

    @Override
    public boolean hasBoundedWildcards() {
        boolean boundedParams = params != null && params.stream()
                .flatMap(ps -> ps instanceof SingleParam sp
                        ? Stream.of(sp) : Stream.empty())
                .anyMatch(sp -> sp.paramType().isBoundedWildcard());
        boolean boundedReturn =
                returnType != null && returnType.isBoundedWildcard();
        boolean boundedThrows =
                exceptions != null && exceptions.stream()
                        .anyMatch(BoundedType::isBoundedWildcard);

        return boundedParams || boundedReturn || boundedThrows;
    }

    @Override
    public <Q extends Query<MemberCandidate>, M extends Match<MemberCandidate, Q>>
    Stream<M> match(TypeMatcher tm, MemberCandidate candidate) {
        OptionalInt score = scoreCandidate(tm, candidate);

        if (score.isPresent()) {
            @SuppressWarnings("unchecked")
            M m = (M) new Match<>(candidate, this, score.getAsInt());
            return Stream.of(m);
        } else
            return Stream.empty();
    }

    OptionalInt scoreCandidate(TypeMatcher tm, MemberCandidate cm) {
        return totalScore(List.of(
                scoreAnnotations(cm.annotationTypes())
                , scoreAccessibility(cm.accessibility())
                , scoreModifiers(cm.otherModifiers())
                , scoreIsDefault(cm)
                , scoreReturn(tm, cm.returnType())
                , scoreName(cm.simpleName(), cm.canonicalName())
                , scoreParams(tm, cm.params())
                , scoreExceptions(tm, cm.throwTypes())
        ));
    }

    private OptionalInt scoreIsDefault(MemberCandidate cm) {
        if (this.isDefault == null)
            return EXACT_MATCH;
        else return (this.isDefault
                && cm.member() instanceof Method meth
                && meth.isDefault() == this.isDefault
        )
                ? EXACT_MATCH : NO_MATCH;
    }

    OptionalInt scoreReturn(TypeMatcher tm, Class<?> returnType) {
        if (this.returnType == null)
            return EXACT_MATCH;
        else
            return tm.scoreTypeMatch(this.returnType, returnType);
    }

    OptionalInt scoreExceptions(TypeMatcher tm,
                                Set<Class<?>> candidateExceptions) {
        // Need to check both ways:
        //  1. Is everything thrown by the query also thrown by the candidate?
        //  2. Is everything thrown by the candidate also thrown by the query?
        return this.exceptions == null
                || this.exceptions.stream()
                .allMatch(qx -> candidateExceptions.stream().anyMatch(
                        cx -> tm.scoreTypeMatch(qx, cx).isPresent())
                )
                && candidateExceptions.stream()
                .allMatch(cx -> this.exceptions.stream().anyMatch(
                        qx -> tm.scoreTypeMatch(qx, cx).isPresent())
                )
                ? EXACT_MATCH : NO_MATCH;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}