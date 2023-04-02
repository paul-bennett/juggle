package com.angellane.juggle;

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
class CandidateMember {
    private final Member member;
    private final List<Class<?>> paramTypes;
    private final Class<?> returnType;
    private final Set<Class<?>> throwTypes;
    private final Set<Class<?>> annotationTypes;

    private CandidateMember(Member member, List<Class<?>> paramTypes, Class<?> returnType, Set<Class<?>> throwTypes,
                            Set<Class<?>> annotationTypes) {
        this.member = member;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
        this.throwTypes = throwTypes;
        this.annotationTypes = annotationTypes;
    }

    public Member getMember() { return member; }

    private static Set<Class<?>> annotationClasses(Annotation[] classAnnotations, Annotation[] memberAnnotations) {
        return Stream.concat(Arrays.stream(classAnnotations), Arrays.stream(memberAnnotations))
                .map(Annotation::annotationType)
                .collect(Collectors.toSet());
    }

    public static CandidateMember memberFromMethod(Method m) {
        return new CandidateMember(m,
                paramsWithImplicitThis(m, Arrays.asList(m.getParameterTypes())),
                m.getReturnType(),
                Set.of(m.getExceptionTypes()),
                annotationClasses(m.getDeclaringClass().getAnnotations(), m.getAnnotations())
        );
    }

    public static CandidateMember memberFromConstructor(Constructor<?> c) {
        return new CandidateMember(c,
                Arrays.asList(c.getParameterTypes()),
                c.getDeclaringClass(),
                Set.of(c.getExceptionTypes()),
                annotationClasses(c.getDeclaringClass().getAnnotations(), c.getAnnotations())
        );
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

    public static List<CandidateMember> membersFromField(Field f) {
        Set<Class<?>> as = annotationClasses(f.getDeclaringClass().getDeclaredAnnotations(), f.getDeclaredAnnotations());
        var getter = new CandidateMember(f, paramsWithImplicitThis(f, List.of()),            f.getType(), Set.of(), as);
        var setter = new CandidateMember(f, paramsWithImplicitThis(f, List.of(f.getType())), Void.TYPE,   Set.of(), as);

        return List.of(getter, setter);
    }

    public boolean matchesParams(List<? extends Class<?>> queryParamTypes, boolean permute) {
        List<List<? extends Class<?>>> paramPermutations = permute
                ? (new PermutationGenerator<>(queryParamTypes)).stream().distinct().collect(Collectors.toList())
                : List.of(queryParamTypes);

        for (var permutedParamTypes : paramPermutations) {
            Iterator<? extends Class<?>> queryTypeIter = permutedParamTypes.iterator();

            if (permutedParamTypes.size() == paramTypes.size()
                    && paramTypes.stream().allMatch(mpt -> isTypeCompatibleForInvocation(mpt, queryTypeIter.next())))
                return true;
        }
        return false;
    }

    public boolean matchesReturn(Class<?> queryReturnType) {
        return isTypeCompatibleForAssignment(queryReturnType, returnType);
    }

    public boolean matchesThrows(Set<Class<?>> queryThrowTypes) {
        // Special case for a query for methods that throw nothing
        if (queryThrowTypes.size() == 0)
            return throwTypes.size() == 0;

        // A candidate's throws clause matches if all of the types it might throw are listed
        // in the query's set of caught exceptions
        for (var caughtType : queryThrowTypes) {
            if (throwTypes.stream().noneMatch(thrownType -> isTypeCompatibleForAssignment(caughtType, thrownType)))
                return false;
        }
        return true;
    }

    public boolean matchesAnnotations(Set<Class<?>> queryAnnotationTypes) {
        return annotationTypes.containsAll(queryAnnotationTypes);
    }

    // An instinctive notion of whether two types are compatible.
    // May or may not be correct.  Written from memory, not the JLS
    //
    // Are the types of writtenType and readType compatible, as if:
    //    WrittenType w; ReadType r; w = r;
    // or
    //    ReadType r() {}
    //    WrittenType w = r();
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
            Map.entry(Byte.TYPE, Set.of(Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Short.TYPE, Set.of(Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Character.TYPE, Set.of(Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Integer.TYPE, Set.of(Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Long.TYPE, Set.of(Float.TYPE, Double.TYPE)),
            Map.entry(Float.TYPE, Set.of(Double.TYPE))
    );

    // The boxing/unboxing conversions
    private static final Map<Class<?>, Class<?>> boxingConversions = Map.ofEntries(
            // Boxing: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.7
            Map.entry(Boolean.class, Boolean.TYPE),
            Map.entry(Byte.class, Byte.TYPE),
            Map.entry(Short.class, Short.TYPE),
            Map.entry(Character.class, Character.TYPE),
            Map.entry(Integer.class, Integer.TYPE),
            Map.entry(Long.class, Long.TYPE),
            Map.entry(Float.class, Float.TYPE),
            Map.entry(Double.class, Double.TYPE),

            // Unboxing: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.8
            Map.entry(Boolean.TYPE, Boolean.class),
            Map.entry(Byte.TYPE, Byte.class),
            Map.entry(Short.TYPE, Short.class),
            Map.entry(Character.TYPE, Character.class),
            Map.entry(Integer.TYPE, Integer.class),
            Map.entry(Long.TYPE, Long.class),
            Map.entry(Float.TYPE, Float.class),
            Map.entry(Double.TYPE, Double.class)
    );
}
