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

import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.match.TypeMatcher;

import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

import static com.angellane.juggle.match.TypeMatcher.EXACT_MATCH;
import static com.angellane.juggle.match.TypeMatcher.NO_MATCH;

public final class TypeQuery extends Query<TypeCandidate> {
    public TypeFlavour          flavour             = null;
    public BoundedType          supertype           = null;
    public Set<BoundedType>     superInterfaces     = null;
    public Set<BoundedType>     permittedSubtypes   = null;

    public TypeQuery() {}
    public TypeQuery(TypeFlavour flavour) { this.flavour = flavour; }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (!(other instanceof TypeQuery q))
            return false;
        else
            return super.equals(q)
                    && Objects.equals(flavour,           q.flavour)
                    && Objects.equals(supertype,         q.supertype)
                    && Objects.equals(superInterfaces,   q.superInterfaces)
                    && Objects.equals(permittedSubtypes, q.permittedSubtypes)
                    ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode()
                , flavour
                , supertype
                , superInterfaces
                , permittedSubtypes
        );
    }

    @Override
    public String toString() {
        return "ClassQuery{"
                + "flavour="              + flavour
                + ", annotationTypes="    + annotationTypes
                + ", accessibility="      + accessibility
                + ", modifierMask="       + modifierMask
                + ", modifiers="          + modifiers
                + ", declarationPattern=" + declarationPattern
                + ", supertype="          + supertype
                + ", superInterfaces="    + superInterfaces
                + ", permittedSubtypes="  + permittedSubtypes
                + ", recordComponents="   + params
                + '}';
    }

    @Override
    public boolean hasBoundedWildcards() {
        boolean boundedSupertype =
                supertype != null && supertype.isBoundedWildcard();
        boolean boundedSuperInterfaces =
                superInterfaces != null && superInterfaces.stream()
                        .anyMatch(BoundedType::isBoundedWildcard);
        boolean boundedPermittedSubtypes =
                permittedSubtypes != null && permittedSubtypes.stream()
                        .anyMatch(BoundedType::isBoundedWildcard);
        boolean boundedRecordComponents =
                params != null && params.stream()
                        .flatMap(ps -> ps instanceof SingleParam sp
                                ? Stream.of(sp) : Stream.empty())
                        .anyMatch(sp -> sp.paramType().isBoundedWildcard());

        return boundedSupertype || boundedSuperInterfaces
                || boundedPermittedSubtypes || boundedRecordComponents;
    }

    @Override
    public
    <Q extends Query<TypeCandidate>, M extends Match<TypeCandidate, Q>>
    Stream<M> match(TypeMatcher tm, TypeCandidate candidate) {
        OptionalInt score = scoreCandidate(tm, candidate);

        if (score.isPresent()) {
            @SuppressWarnings("unchecked")
            M m = (M)new Match<>(candidate, this, score.getAsInt());
            return Stream.of(m);
        }
        else
            return Stream.empty();
    }

    public OptionalInt scoreCandidate(TypeMatcher tm, TypeCandidate ct) {
        return totalScore(List.of(
                scoreAnnotations(ct.annotationTypes())
                , scoreAccessibility(ct.accessibility())
                , scoreModifiers(ct.otherModifiers())
                , scoreName(ct.declarationName())
                , scoreFlavour(ct.flavour())
                , scoreSupertype(ct.superClass())
                , scoreSuperInterfaces(ct.superInterfaces())
                , scorePermittedSubtypes(ct.permittedSubtypes())
                , scoreRecordComponents(tm, ct.recordComponents())
        ));
    }

    public void setSupertype(BoundedType supertype) {
        this.supertype = supertype;
    }

    public void setSuperInterfaces(Set<BoundedType> superInterfaces) {
        this.superInterfaces = superInterfaces;
    }

    public void setPermittedSubtypes(Set<BoundedType> permittedSubtypes) {
        this.permittedSubtypes = permittedSubtypes;
    }

    public void setRecordComponents(List<ParamSpec> components) {
        this.params = components;
    }

    private OptionalInt scoreFlavour(TypeFlavour f) {
        return flavour == null || flavour.equals(f)
                ? EXACT_MATCH : NO_MATCH;
    }

    private OptionalInt scoreSupertype(Class<?> c) {
        return supertype == null ||
                supertype.matchesClass(c)
                ? EXACT_MATCH : NO_MATCH;
    }

    private OptionalInt scoreSuperInterfaces(Set<Class<?>> cs) {
        return superInterfaces == null ||
                superInterfaces.stream()
                        .allMatch(bt -> cs.stream().anyMatch(bt::matchesClass))
                ? EXACT_MATCH : NO_MATCH;
    }

    private OptionalInt scorePermittedSubtypes(Set<Class<?>> cs) {
        return permittedSubtypes == null
                || permittedSubtypes.stream()
                .allMatch(bt -> cs.stream().anyMatch(bt::matchesClass))
                ? EXACT_MATCH : NO_MATCH;
    }

    OptionalInt scoreRecordComponents(TypeMatcher tm, List<RecordComponent> rcs) {
        List<? extends Class<?>> params = rcs.stream().map(RecordComponent::getType).toList();
        return scoreParams(tm, params);
    }
}

