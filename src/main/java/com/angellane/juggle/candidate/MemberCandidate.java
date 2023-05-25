package com.angellane.juggle.candidate;

import com.angellane.juggle.match.Accessibility;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This POJO contains the details of the candidate member (field, constructor or method).
 * <p>
 * The 'member' field is the member itself.  More interesting are the paramTypes, returnType and throwTypes
 * fields, which represent the type the member would have if considered as a static function.  For static
 * methods the paramTypes field includes an implicit first entry representing the type of 'this'.
 */
public record MemberCandidate(
        Member member,
        Accessibility accessibility,
        int otherModifiers,
        Set<Class<?>> annotationTypes,
        String declarationName,
        Class<?> returnType,
        List<Param> params,
        Set<Class<?>> throwTypes
)
        implements Candidate {

    // Constructor used by factory methods
    private MemberCandidate(Member member, Set<Class<?>> annotationTypes,
                            Class<?> returnType, List<Param> params,
                            Set<Class<?>> throwTypes) {
        this(member, Accessibility.fromModifiers(member.getModifiers()),
                member.getModifiers() & OTHER_MODIFIERS_MASK,
                annotationTypes, member.getName(),
                returnType, params, throwTypes);
    }

    // This constructor is used by parameter permutation generator
    public MemberCandidate(MemberCandidate other, List<Param> params) {
        this(other.member, other.accessibility, other.otherModifiers,
                other.annotationTypes, other.declarationName,
                other.returnType, params, other.throwTypes);
    }

    static Param paramFromParameter(Parameter p) {
        return new Param(p.getType(), p.getName());
    }
    static Param paramFromField(Field f) {
        return new Param(f.getType(), f.getName());
    }

    static List<Param> paramListFromParameters(Parameter[] ps) {
        return Arrays.stream(ps)
                .map(MemberCandidate::paramFromParameter)
                .toList();
    }

    @Override
    public String packageName() {
        return member().getDeclaringClass().getPackageName();
    }

    public static MemberCandidate memberFromMethod(Method m) {
        return new MemberCandidate(m,
                annotationClasses(m.getDeclaringClass().getAnnotations(), m.getAnnotations()),
                m.getReturnType(),
                paramsWithImplicitThis(m, paramListFromParameters(m.getParameters())),
                Set.of(m.getExceptionTypes())
        );
    }

    public static MemberCandidate memberFromConstructor(Constructor<?> c) {
        return new MemberCandidate(c,
                annotationClasses(c.getDeclaringClass().getAnnotations(), c.getAnnotations()),
                c.getDeclaringClass(),
                paramListFromParameters(c.getParameters()),
                Set.of(c.getExceptionTypes())
        );
    }

    public static List<MemberCandidate> membersFromField(Field f) {
        Set<Class<?>> as = annotationClasses(f.getDeclaringClass().getDeclaredAnnotations(), f.getDeclaredAnnotations());

        var getter = new MemberCandidate(f, as, f.getType(),
                paramsWithImplicitThis(f,
                        List.of()),                  Set.of());
        var setter = new MemberCandidate(f, as, Void.TYPE,
                paramsWithImplicitThis(f,
                        List.of(paramFromField(f))), Set.of());

        return List.of(getter, setter);
    }

    private static Set<Class<?>> annotationClasses(Annotation[] classAnnotations, Annotation[] memberAnnotations) {
        return Stream.concat(Arrays.stream(classAnnotations), Arrays.stream(memberAnnotations))
                .map(Annotation::annotationType)
                .collect(Collectors.toSet());
    }

    private static List<Param> paramsWithImplicitThis(Member m, List<Param> params) {
        if (Modifier.STATIC == (m.getModifiers() & Modifier.STATIC))
            return params;
        else {
            List<Param> ret = new LinkedList<>();
            ret.add(new Param(m.getDeclaringClass(), "this"));
            ret.addAll(params);
            return ret;
        }
    }
}
