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
package com.angellane.juggle.match;

import com.angellane.juggle.query.BoundedType;

import java.util.*;

/**
 * <p>Is the target type compatible with the expression type?</p>
 * <p>
 *     This record checks whether or not two types are compatible.
 *     If #applyConversions is false, the type check is based on type
 *     fit to specified bounds.  If conversions are allowed, (un)boxing
 *     is allowed, and reference types are checked with inferred bounds
 *     when not specified.  This is equivalent to to the "loose invocation
 *     context" (which itself is the same as the "assignment context" in
 *     section 5 of the JLS.
 * </p>
 * <p>
 *     The score for a match allowed under the Strict Invocation Context
 *     will always be lower (better) than those allowed under the Loose
 *     Invocation Context.
 * </p>
 *
 * @param applyConversions whether to apply conversions when matching
 */
public class TypeMatcher {
    static final int IDENTITY_COST  = 0;
    static final int WIDENING_COST  = 1;
    static final int BOXING_COST    = 2;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final OptionalInt NO_MATCH       = OptionalInt.empty();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final OptionalInt EXACT_MATCH    = OptionalInt.of(IDENTITY_COST);
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final OptionalInt WIDENED_MATCH  = OptionalInt.of(WIDENING_COST);
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final OptionalInt BOXED_MATCH    = OptionalInt.of(BOXING_COST);
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final OptionalInt UNBOXED_MATCH  = OptionalInt.of(BOXING_COST);

    private final boolean applyConversions;

    public TypeMatcher(boolean applyConversions) {
        this.applyConversions = applyConversions;
    }

    public boolean applyConversions() { return applyConversions; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeMatcher that = (TypeMatcher) o;
        return applyConversions == that.applyConversions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(applyConversions);
    }

    /**
     * <p>
     *     Can an expression whose type is within the bounds of exprType be
     *     assigned to a variable of specific type targetType?
     * </p>
     * <p>
     *     This is used within Juggle queries to match parameter types.
     * </p>
     *
     * @param targetType the specific type of the LHS of an assignment
     * @param exprType the bounds of the type of the RHS of assignment
     * @return The score of the match, or empty if no match possible
     */
    public OptionalInt scoreTypeMatch(Class<?> targetType,
                                      BoundedType exprType) {
        if (BoundedType.isUnboundedWildcard(exprType))
            return EXACT_MATCH;
        else if (!applyConversions)
            return scoreBoundedType(exprType, targetType);
        else {
            Class<?> lowerBound = exprType.lowerBound();

            if (exprType.isPrimitive()) {
                if (targetType.isPrimitive())
                    return scorePrimitiveToPrimitive(targetType, lowerBound);
                else
                    return scorePrimitiveToReference(targetType, lowerBound);
            }
            else {
                if (targetType.isPrimitive())
                    return scoreReferenceToPrimitive(targetType, lowerBound);
                else
                    if (!exprType.isExactType())
                        return scoreBoundedType(exprType, targetType);
                    else
                        // Try with the target type's lower bound eliminated
                        return scoreBoundedType(
                                BoundedType.supertypeOf(lowerBound),
                                targetType);
            }
        }
    }

    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue"})
    static OptionalInt matchSum(OptionalInt a, OptionalInt b) {
        return a.isEmpty() || b.isEmpty()
                ? NO_MATCH : OptionalInt.of(a.getAsInt() + b.getAsInt());
    }

    /**
     * <p>
     *     Is there a type within the bounds of targetType that can be used
     *     as the target for assignment of an expression of specific type
     *     exprType?
     * </p>
     * <p>
     *     This is used within Juggle queries to match return types and
     *     thrown exceptions.
     * </p>
     *
     * @param targetType the bounds of the LHS of an assignment
     * @param exprType the specific type of the RHS of assignment
     * @return The score of the match, or empty if no match possible
     */
    public OptionalInt scoreTypeMatch(BoundedType targetType,
                                      Class<?> exprType) {
        if (BoundedType.isUnboundedWildcard(targetType))
            return EXACT_MATCH;
        else if (!applyConversions)
            return scoreBoundedType(targetType, exprType);
        else {
            Class<?> lowerBound = targetType.lowerBound();

            if (exprType.isPrimitive()) {
                if (targetType.isPrimitive())
                    return scorePrimitiveToPrimitive(lowerBound, exprType);
                else
                    return scorePrimitiveToReference(lowerBound, exprType);
            }
            else {
                if (targetType.isPrimitive())
                    return scoreReferenceToPrimitive(lowerBound, exprType);
                else
                    if (!targetType.isExactType())
                        return scoreBoundedType(targetType, exprType);
                    else
                        // Try with the target type's upper bound eliminated
                        return scoreBoundedType(
                               /* lowerBound == null
                                        ? BoundedType.unboundedWildcardType()
                                        :*/ BoundedType.subtypeOf(lowerBound),
                                exprType);
            }
        }
    }

    private static OptionalInt scorePrimitiveToPrimitive(
            Class<?> targetType, Class<?> exprType) {
        // PRIMITIVE -> PRIMITIVE
        // Optional Widening Primitive
        if (targetType.equals(exprType))
            return EXACT_MATCH;
        else
            return Optional.ofNullable(
                    wideningPrimitiveConversions.get(exprType)
            ).orElse(Set.of()).contains(targetType)
                    ? WIDENED_MATCH
                    : NO_MATCH;
    }

    private static OptionalInt scorePrimitiveToReference(
            Class<?> targetType, Class<?> exprType) {
        // PRIMITIVE -> REFERENCE
        // Boxing, then optional Widening Reference
        Class<?> boxedType = boxingConversions.get(exprType);
        if (boxedType == null)
            return NO_MATCH;
        else if (targetType == null)
            return BOXED_MATCH;
        else if (targetType.equals(boxedType))
            return BOXED_MATCH;
        else
            return targetType.isAssignableFrom(boxedType)
                    ? matchSum(BOXED_MATCH, WIDENED_MATCH)
                    : NO_MATCH;
    }

    private static OptionalInt scoreReferenceToPrimitive(
            Class<?> targetType, Class<?> exprType) {
        // REFERENCE -> PRIMITIVE
        // Unboxing, then optional Widening Primitive
        Class<?> unboxedType = unboxingConversions.get(exprType);
        if (unboxedType == null)
            return NO_MATCH;
        else if (targetType.equals(unboxedType))
            return UNBOXED_MATCH;
        else
            return Optional.ofNullable(
                    wideningPrimitiveConversions.get(unboxedType)
            ).orElse(Set.of()).contains(targetType)
                    ? matchSum(UNBOXED_MATCH, WIDENED_MATCH)
                    : NO_MATCH;
    }

    /**
     * Tries to match a candidate against a bounded type.  Returns empty()
     * if the types are incompatible, or a score value otherwise.
     *
     * @param bt The bounded type to try and match the candidate against
     * @param candidate The candidate type
     * @return MATCH_FAILED if no match was possible, a score value
     * otherwise.
     */
    public static OptionalInt scoreBoundedType(
            BoundedType bt, Class<?> candidate
    ) {
        // REFERENCE -> REFERENCE
        // Optional Widening Reference

        // This method is a bit different to the other scorers since it
        // takes <BoundedType,Class> rather than <Class,Class>.  It's also
        // agnostic about directionality.

        int score = IDENTITY_COST;    // Innocent until proven guilty

        if (bt.lowerBound() != null
                && bt.lowerBound() != candidate
        ) {
            if (candidate.isAssignableFrom(bt.lowerBound()))
                score += WIDENING_COST;
            else
                return NO_MATCH;
        }
        if (bt.upperBound() != null
                && !bt.upperBound().equals(Set.of(candidate))
        ) {
            if (bt.upperBound().stream()
                    .allMatch(b -> b.isAssignableFrom(candidate)))
                score += WIDENING_COST;
            else
                return NO_MATCH;
        }

        return OptionalInt.of(score);
    }


    // The 19 Widening Primitive Conversions, documented in Java Language Specification (Java SE 14 edn) sect 5.1.2
    // https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.2
    static Map<Class<?>, Set<Class<?>>> wideningPrimitiveConversions = Map.ofEntries(
            Map.entry(Byte      .TYPE, Set.of(Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Short     .TYPE, Set.of(            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Character .TYPE, Set.of(            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Integer   .TYPE, Set.of(                          Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Long      .TYPE, Set.of(                                     Float.TYPE, Double.TYPE)),
            Map.entry(Float     .TYPE, Set.of(                                                 Double.TYPE))
    );

    // Boxing Conversions: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.7
    static Map<Class<?>, Class<?>> boxingConversions =
            Map.ofEntries(
                    Map.entry(Boolean   .TYPE,  Boolean     .class),
                    Map.entry(Byte      .TYPE,  Byte        .class),
                    Map.entry(Short     .TYPE,  Short       .class),
                    Map.entry(Character .TYPE,  Character   .class),
                    Map.entry(Integer   .TYPE,  Integer     .class),
                    Map.entry(Long      .TYPE,  Long        .class),
                    Map.entry(Float     .TYPE,  Float       .class),
                    Map.entry(Double    .TYPE,  Double      .class)
            );

    // Unboxing Conversions: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.8
    static Map<Class<?>, Class<?>> unboxingConversions =
            Map.ofEntries(
                    Map.entry(Boolean   .class, Boolean     .TYPE),
                    Map.entry(Byte      .class, Byte        .TYPE),
                    Map.entry(Short     .class, Short       .TYPE),
                    Map.entry(Character .class, Character   .TYPE),
                    Map.entry(Integer   .class, Integer     .TYPE),
                    Map.entry(Long      .class, Long        .TYPE),
                    Map.entry(Float     .class, Float       .TYPE),
                    Map.entry(Double    .class, Double      .TYPE)
            );
}