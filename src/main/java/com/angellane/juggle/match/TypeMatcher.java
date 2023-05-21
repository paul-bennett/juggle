package com.angellane.juggle.match;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * An encapsulation of Java's type-matching & conversion rules.
 *
 * @param applyConversions whether to apply conversions when matching
 */
public record TypeMatcher(boolean applyConversions) {
    static final int IDENTITY_SCORE = 0;
    static final int WIDENING_SCORE = 1;
    static final int BOXING_SCORE   = 2;

    /**
     * <p>Is the target type compatible with the expression type?</p>
     * <p>
     *     This class checks whether or not the two types are compatible.
     *     If #applyConversions is false, the type check is based on type
     *     equality.  If conversions are allowed, the check is equivalent
     *     to the "loose invocation context" (which itself is the same as
     *     the "assignment context" in section 5 of the JLS.
     * </p>
     * <p>
     *     The score for a match allowed under the Strict Invocation Context
     *     will always be lower (better) than those allowed under the Loose
     *     Invocation Context.
     * </p>
     * @param targetType the target type (LHS of assignment)
     * @param exprType the expression type (RHS of assignment)
     * @return A score for the match, or empty if the types are incompatible
     */
    OptionalInt scoreTypeMatch(Class<?> targetType, Class<?> exprType) {
        if (targetType.equals(exprType))
            return OptionalInt.of(IDENTITY_SCORE);
        else if (!applyConversions)
            return OptionalInt.empty();
        else if (exprType.isPrimitive()) {
            if (targetType.isPrimitive())
                return scorePrimitiveToPrimitive(targetType, exprType);
            else
                return scorePrimitiveToReference(targetType, exprType);
        }
        else {
            if (targetType.isPrimitive())
                return scoreReferenceToPrimitive(targetType, exprType);
            else
                return scoreReferenceToReference(targetType, exprType);
        }
    }

    private static OptionalInt scorePrimitiveToPrimitive(
            Class<?> targetType, Class<?> exprType) {
        // PRIMITIVE -> PRIMITIVE
        // Allows: Widening Primitive
        return Optional.ofNullable(
                wideningPrimitiveConversions.get(exprType)
        ).orElse(Set.of()).contains(targetType)
                ? OptionalInt.of(WIDENING_SCORE)
                : OptionalInt.empty();
    }

    private static OptionalInt scorePrimitiveToReference(
            Class<?> targetType, Class<?> exprType) {
        // PRIMITIVE -> REFERENCE
        // Allows: Boxing, then optional Widening Reference
        Class<?> boxedType = boxingConversions.get(exprType);
        if (boxedType == null)
            return OptionalInt.empty();
        else if (targetType.equals(boxedType))
            return OptionalInt.of(BOXING_SCORE);
        else
            return targetType.isAssignableFrom(boxedType)
                    ? OptionalInt.of(BOXING_SCORE + WIDENING_SCORE)
                    : OptionalInt.empty();
    }

    private static OptionalInt scoreReferenceToPrimitive(
            Class<?> targetType, Class<?> exprType) {
        // REFERENCE -> PRIMITIVE
        // Allowed: Unboxing, then optional Widening Primitive
        Class<?> unboxedType = unboxingConversions.get(exprType);
        if (unboxedType == null)
            return OptionalInt.empty();
        else if (targetType.equals(unboxedType))
            return OptionalInt.of(BOXING_SCORE);
        else
            return Optional.ofNullable(
                    wideningPrimitiveConversions.get(unboxedType)
            ).orElse(Set.of()).contains(targetType)
                    ? OptionalInt.of(BOXING_SCORE + WIDENING_SCORE)
                    : OptionalInt.empty();
    }

    private static OptionalInt scoreReferenceToReference(
            Class<?> targetType, Class<?> exprType) {
        // REFERENCE -> REFERENCE
        // Allowed: Widening Reference
        return targetType.isAssignableFrom(exprType)
                ? OptionalInt.of(WIDENING_SCORE)
                : OptionalInt.empty();
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