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

import com.angellane.juggle.candidate.Param;
import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.match.TypeMatcher;
import com.angellane.juggle.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.angellane.juggle.match.TypeMatcher.EXACT_MATCH;
import static com.angellane.juggle.match.TypeMatcher.NO_MATCH;

public final class TypeQuery extends Query<TypeCandidate> {
    public TypeFlavour          flavour             = null;
    public BoundedType          supertype           = null;
    public Set<BoundedType>     superInterfaces     = null;
    public BoundedType          subtype             = null;
    public Boolean              isSealed            = null;
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
                    && Objects.equals(subtype,           q.subtype)
                    && Objects.equals(isSealed,          q.isSealed)
                    && Objects.equals(permittedSubtypes, q.permittedSubtypes)
                    ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode()
                , flavour
                , supertype
                , superInterfaces
                , subtype
                , isSealed
                , permittedSubtypes
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
                + "flavour="              + flavour
                + ", annotationTypes="    + annotationTypes
                + ", accessibility="      + accessibility
                + ", modifierMask="       + modifierMask
                + ", modifiers="          + modifiers
                + ", declarationPattern=" + declarationPattern
                + ", supertype="          + supertype
                + ", superInterfaces="    + superInterfaces
                + ", subtype="            + subtype
                + ", isSealed="           + isSealed
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
        boolean boundedSubtype =
                subtype != null && subtype.isBoundedWildcard();
        boolean boundedPermittedSubtypes =
                permittedSubtypes != null && permittedSubtypes.stream()
                        .anyMatch(BoundedType::isBoundedWildcard);
        boolean boundedRecordComponents =
                params != null && params.stream()
                        .flatMap(ps -> ps instanceof SingleParam sp
                                ? Stream.of(sp) : Stream.empty())
                        .anyMatch(sp -> sp.paramType().isBoundedWildcard());

        return boundedSupertype || boundedSuperInterfaces || boundedSubtype
                || boundedPermittedSubtypes || boundedRecordComponents;
    }

    @Override
    public OptionalInt scoreCandidate(TypeMatcher tm, TypeCandidate ct) {
        return totalScore(List.of(
                scoreAnnotations(ct.annotationTypes())
                , scoreAccessibility(ct.accessibility())
                , scoreModifiers(ct.otherModifiers())
                , scoreName(ct.simpleName(), ct.canonicalName())
                , scoreFlavour(ct.flavour())
                , scoreSupertype(tm, ct.clazz())
                , scoreSuperInterfaces(tm, ct.clazz())
                , scoreSubtype(tm, ct.clazz())
                , scoreIsSealed(ct.clazz())
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

    public void setSubtype(BoundedType lowerBound) {
        this.subtype = lowerBound;
    }

    public void setIsSealed(Boolean isSealed) {
        this.isSealed = isSealed;
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

    private OptionalInt scoreSupertype(TypeMatcher tm, Class<?> c) {
        if (this.supertype == null)
            return EXACT_MATCH;
        else if (c.getSuperclass() == null)
            return NO_MATCH;
        else
            return tm.scoreTypeMatch(this.supertype, c.getSuperclass());
    }

    private OptionalInt scoreSuperInterfaces(TypeMatcher tm, Class<?> c)
    {
        Set<Class<?>> candidateSupertypes =
                Arrays.stream(c.getInterfaces()).collect(Collectors.toSet());
        if (c.getSuperclass() != null)
            candidateSupertypes.add(c.getSuperclass());

        if (this.superInterfaces == null)
            return EXACT_MATCH;
        else {
            // Does the candidate class implement all interfaces mentioned
            // in the query?  Note: we don't do the reverse check, to ensure
            // that all interfaces implemented by the candidate are listed
            // in the query since that is unexpectedly restrictive.

            return this.superInterfaces.stream()
                    .allMatch(qi -> candidateSupertypes.stream().anyMatch(
                            ci -> tm.scoreTypeMatch(qi, ci).isPresent())
                    )
                    ? EXACT_MATCH : NO_MATCH;
        }
    }

    private OptionalInt scoreSubtype(TypeMatcher tm, Class<?> c) {
        if (this.subtype == null)
            return EXACT_MATCH;
        else {
            Class<?> lowerBound = this.subtype.lowerBound();

            if (tm.applyConversions())
                return tm.scoreTypeMatch(BoundedType.supertypeOf(lowerBound), c);
            else {
                // Only allow direct matches
                return Stream.concat(
                        Stream.ofNullable(lowerBound.getSuperclass()),
                        Arrays.stream(lowerBound.getInterfaces())
                        )
                        .anyMatch(c::equals) ? EXACT_MATCH : NO_MATCH;
            }
        }
    }

    private OptionalInt scoreIsSealed(Class<?> c) {
        if (this.isSealed == null)
            return EXACT_MATCH;
        else return (this.isSealed && ClassUtils.classIsSealed(c))
                || (!this.isSealed && ClassUtils.classIsNonSealed(c))
                ? EXACT_MATCH : NO_MATCH;
    }

    private OptionalInt scorePermittedSubtypes(Set<Class<?>> cs) {
        return permittedSubtypes == null
                || permittedSubtypes.stream()
                .allMatch(bt -> cs.stream().anyMatch(bt::matchesClass))
                ? EXACT_MATCH : NO_MATCH;
    }

    OptionalInt scoreRecordComponents(TypeMatcher tm, List<RecordComponent> rcs) {
        List<Param> params = rcs.stream().map(
                rc -> new Param(Arrays.stream(rc.getAnnotations())
                            .map(Annotation::annotationType)
                            .collect(Collectors.toSet()
                            ),
                        0,
                        rc.getType(),
                        rc.getName()
                        )
        ).toList();
        return scoreParams(tm, params);
    }
}

