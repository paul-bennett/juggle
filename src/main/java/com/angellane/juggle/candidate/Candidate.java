package com.angellane.juggle.candidate;

import com.angellane.juggle.Accessibility;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface Candidate {
    int ACCESS_MODIFIERS_MASK =
            Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;

    // These are the "other" modifiers we're interested in. Why am I not
    // using ~ACCESS_MODIFIERS_MASK?  Because there are some modifiers that
    // aren't published via the public API of java.lang.reflect.Modifier
    // (for example 0x4000 = ENUM).  So instead we'll limit ourselves just
    // to those modifiers that Juggle knows about.
    //
    // Public modifier bits we currently deliberately ignore:
    //   INTERFACE
    // Other modifier bits presently named in the Modifier source:
    //   BRIDGE, VARARGS, SYNTHETIC, ANNOTATION, ENUM, MANDATED
    //
    // Modifiers that are not represented in the Modifier bits:
    //   sealed, non-sealed
    //
    int OTHER_MODIFIERS_MASK
            = Modifier.STATIC   | Modifier.FINAL      | Modifier.SYNCHRONIZED
            | Modifier.VOLATILE | Modifier.TRANSIENT  | Modifier.NATIVE
            | Modifier.ABSTRACT | Modifier.STRICT
            ;


    Set<Class<?>>   annotationTypes();
    Accessibility   accessibility();
    int             otherModifiers();
    String          declarationName();


    // An instinctive notion of whether two types are compatible.
    // May or may not be correct.  Written from memory, not the JLS
    //
    // Are the types of writtenType and readType compatible, as if:
    // <pre>
    //    WrittenType w; ReadType r; w = r;
    // </pre>
    // or
    // <pre>
    //    ReadType r() {}
    //    WrittenType w = r();
    // </pre>
    static boolean isTypeInstinctivelyCompatible(Class<?> writtenType, Class<?> readType) {
        // Three cases:
        // 1. Primitive widening conversions
        // 2. Boxing/unboxing conversions
        // 3. Reference conversions
        return Optional.ofNullable(wideningConversions.get(readType)).orElse(Set.of()).contains(writtenType)
                || writtenType.equals(boxingConversions.get(readType))
                || writtenType.isAssignableFrom(readType);
    }


    // These next few methods implement the conversions described in the Java Language Specification
    // (Java SE 14 edition) chapter 5 "Conversions and Contexts":
    //    https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html

    // Invocation Context: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.3
    static boolean isTypeCompatibleForInvocation(Class<?> parameterType, Class<?> argumentType) {
        return isTypeInstinctivelyCompatible(parameterType, argumentType);
    }

    // Assignment Context: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.2
    static boolean isTypeCompatibleForAssignment(Class<?> variableType, Class<?> returnType) {
        return isTypeInstinctivelyCompatible(variableType, returnType);
    }

    // The 19 Widening Primitive Conversions, documented in Java Language Specification (Java SE 14 edn) sect 5.1.2
    // https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.2
    Map<Class<?>, Set<Class<?>>> wideningConversions = Map.ofEntries(
            Map.entry(Byte      .TYPE, Set.of(Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Short     .TYPE, Set.of(            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Character .TYPE, Set.of(            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Integer   .TYPE, Set.of(                          Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Long      .TYPE, Set.of(                                     Float.TYPE, Double.TYPE)),
            Map.entry(Float     .TYPE, Set.of(                                                 Double.TYPE))
    );

    // The boxing/unboxing conversions
    Map<Class<?>, Class<?>> boxingConversions =
            Map.ofEntries(
                    // Boxing: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.7
                    Map.entry(Boolean   .class, Boolean     .TYPE),
                    Map.entry(Byte      .class, Byte        .TYPE),
                    Map.entry(Short     .class, Short       .TYPE),
                    Map.entry(Character .class, Character   .TYPE),
                    Map.entry(Integer   .class, Integer     .TYPE),
                    Map.entry(Long      .class, Long        .TYPE),
                    Map.entry(Float     .class, Float       .TYPE),
                    Map.entry(Double    .class, Double      .TYPE),

                    // Unboxing: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.8
                    Map.entry(Boolean   .TYPE,  Boolean     .class),
                    Map.entry(Byte      .TYPE,  Byte        .class),
                    Map.entry(Short     .TYPE,  Short       .class),
                    Map.entry(Character .TYPE,  Character   .class),
                    Map.entry(Integer   .TYPE,  Integer     .class),
                    Map.entry(Long      .TYPE,  Long        .class),
                    Map.entry(Float     .TYPE,  Float       .class),
                    Map.entry(Double    .TYPE,  Double      .class)
            );

}
