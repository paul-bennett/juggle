package com.angellane.juggle.candidate;

import com.angellane.juggle.Accessibility;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This POJO contains the details of the candidate member (field, constructor or method).
 * <p>
 * The 'member' field is the member itself.  More interesting are the paramTypes, returnType and throwTypes
 * fields, which represent the type the member would have if considered as a static function.  For static
 * methods the paramTypes field includes an implicit first entry representing the type of 'this'.
 */
public record CandidateMember(
        Member member,
        Accessibility accessibility,
        int otherModifiers,
        Set<Class<?>> annotationTypes,
        Class<?> returnType,
        List<Class<?>> paramTypes,
        Set<Class<?>> throwTypes
)
        implements Candidate {

    // Constructor used by factory methods
    private CandidateMember(Member member, Set<Class<?>> annotationTypes,
                             Class<?> returnType, List<Class<?>> paramTypes, Set<Class<?>> throwTypes) {
        this(member, Accessibility.fromModifiers(member.getModifiers()), member.getModifiers() & OTHER_MODIFIERS_MASK,
                annotationTypes, returnType, paramTypes, throwTypes);
    }

    // This constructor is used by parameter permutation generator
    public CandidateMember(CandidateMember other, List<Class<?>> params) {
        this(other.member, other.accessibility, other.otherModifiers,
                other.annotationTypes, other.returnType, params, other.throwTypes);
    }

    public static CandidateMember memberFromMethod(Method m) {
        return new CandidateMember(m,
                annotationClasses(m.getDeclaringClass().getAnnotations(), m.getAnnotations()),
                m.getReturnType(),
                paramsWithImplicitThis(m, Arrays.asList(m.getParameterTypes())),
                Set.of(m.getExceptionTypes())
        );
    }

    public static CandidateMember memberFromConstructor(Constructor<?> c) {
        return new CandidateMember(c,
                annotationClasses(c.getDeclaringClass().getAnnotations(), c.getAnnotations()),
                c.getDeclaringClass(),
                Arrays.asList(c.getParameterTypes()),
                Set.of(c.getExceptionTypes())
        );
    }

    public static List<CandidateMember> membersFromField(Field f) {
        Set<Class<?>> as = annotationClasses(f.getDeclaringClass().getDeclaredAnnotations(), f.getDeclaredAnnotations());

        var getter = new CandidateMember(f, as, f.getType(), paramsWithImplicitThis(f, List.of()),            Set.of());
        var setter = new CandidateMember(f, as, Void.TYPE,   paramsWithImplicitThis(f, List.of(f.getType())), Set.of());

        return List.of(getter, setter);
    }

    private static Set<Class<?>> annotationClasses(Annotation[] classAnnotations, Annotation[] memberAnnotations) {
        return Stream.concat(Arrays.stream(classAnnotations), Arrays.stream(memberAnnotations))
                .map(Annotation::annotationType)
                .collect(Collectors.toSet());
    }

    private static List<Class<?>> paramsWithImplicitThis(Member m, List<Class<?>> paramTypes) {
        if (Modifier.STATIC == (m.getModifiers() & Modifier.STATIC))
            return paramTypes;
        else {
            List<Class<?>> ret = new LinkedList<>();
            ret.add(m.getDeclaringClass());
            ret.addAll(paramTypes);
            return ret;
        }
    }



    public boolean matchesAccessibility(Accessibility queryAccessibility) {
        return queryAccessibility == null
                || this.accessibility.isAtLeastAsAccessibleAsOther(queryAccessibility);
    }

    public boolean matchesAnnotations(Set<Class<?>> queryAnnotationTypes) {
        return annotationTypes.containsAll(queryAnnotationTypes);
    }


    public boolean matchesModifiers(int queryMask, int queryModifiers) {
        final int mask = queryMask & OTHER_MODIFIERS_MASK;
        return (mask & queryModifiers) == (mask & this.otherModifiers);
    }

    public boolean matchesReturn(Class<?> queryReturnType) {
        return isTypeCompatibleForAssignment(queryReturnType, returnType);
    }

    public boolean matchesParams(List<? extends Class<?>> queryParamTypes) {
        Iterator<? extends Class<?>> queryTypeIter = queryParamTypes.iterator();
        return paramTypes.stream().allMatch(mpt -> isTypeCompatibleForInvocation(mpt, queryTypeIter.next()));
    }

    public boolean matchesThrows(Set<Class<?>> queryThrowTypes) {
        // Special case for a query for methods that throw nothing
        if (queryThrowTypes.size() == 0)
            return throwTypes.size() == 0;

        // A candidate's throws clause matches if the types it might throw are listed
        // in the query's set of caught exceptions
        for (var caughtType : queryThrowTypes) {
            if (throwTypes.stream().noneMatch(thrownType -> isTypeCompatibleForAssignment(caughtType, thrownType)))
                return false;
        }
        return true;
    }

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
    private boolean isTypeInstinctivelyCompatible(Class<?> writtenType, Class<?> readType) {
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
    private boolean isTypeCompatibleForInvocation(Class<?> parameterType, Class<?> argumentType) {
        return isTypeInstinctivelyCompatible(parameterType, argumentType);
    }

    // Assignment Context: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.2
    private boolean isTypeCompatibleForAssignment(Class<?> variableType, Class<?> returnType) {
        return isTypeInstinctivelyCompatible(variableType, returnType);
    }

    // The 19 Widening Primitive Conversions, documented in Java Language Specification (Java SE 14 edn) sect 5.1.2
    // https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.2
    private static final Map<Class<?>, Set<Class<?>>> wideningConversions = Map.ofEntries(
            Map.entry(Byte      .TYPE, Set.of(Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Short     .TYPE, Set.of(            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Character .TYPE, Set.of(            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Integer   .TYPE, Set.of(                          Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Long      .TYPE, Set.of(                                     Float.TYPE, Double.TYPE)),
            Map.entry(Float     .TYPE, Set.of(                                                 Double.TYPE))
    );

    // The boxing/unboxing conversions
    private static final Map<Class<?>, Class<?>> boxingConversions =
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
