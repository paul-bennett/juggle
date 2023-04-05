package com.angellane.juggle;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeSignature {
    public final List<Class<?>> paramTypes;
    public final Class<?> returnType;
    public final Set<Class<?>> throwTypes;
    public final Set<Class<?>> annotations;

    public TypeSignature(List<Class<?>> paramTypes, Class<?> returnType, Set<Class<?>> throwTypes,
                         Set<Class<?>> annotationTypes) {
        this.paramTypes  = paramTypes;
        this.returnType  = returnType;
        this.throwTypes  = throwTypes;
        this.annotations = annotationTypes;
    }

    static Set<Class<?>> annotationClasses(Annotation[] classAnnotations, Annotation[] memberAnnotations) {
        return Stream.concat(Arrays.stream(classAnnotations), Arrays.stream(memberAnnotations))
                .map(Annotation::getClass)
                .collect(Collectors.toSet());
    }

    public static List<TypeSignature> of(Member m) {
        Annotation[] classAnnotations = m.getDeclaringClass().getDeclaredAnnotations();

        List<Class<?>> implicitParams = new ArrayList<>();

        // Handle the 'this' pointer for non-static members.
        // Note that Constructors appear to be non-static, but they don't have a silent 'this'.
        if (!Modifier.isStatic(m.getModifiers()) && !(m instanceof Constructor<?>))
            implicitParams.add(m.getDeclaringClass());

        if (m instanceof Constructor<?>) {
            Constructor<?> c = (Constructor<?>)m;

            return List.of(new TypeSignature(
                    List.of(c.getParameterTypes()),
                    c.getDeclaringClass(),
                    Set.of(c.getExceptionTypes()),
                    annotationClasses(classAnnotations, c.getDeclaredAnnotations())
            ));
        }
        else if (m instanceof Method) {
            Method e = (Method)m;

            return List.of(new TypeSignature(
                    Stream.of(implicitParams.stream(), Arrays.stream(e.getParameterTypes()))
                            .flatMap(Function.identity())
                            .collect(Collectors.toList()),
                    e.getReturnType(),
                    Set.of(e.getExceptionTypes()),
                    annotationClasses(classAnnotations, e.getDeclaredAnnotations())
            ));
        }
        else {
            assert (m instanceof Field);

            Field f = (Field)m;

            Set<Class<?>> annotations = annotationClasses(classAnnotations, f.getDeclaredAnnotations());

            return List.of(
                    new TypeSignature(implicitParams, f.getType(), Set.of(), annotations),      // Getter
                    new TypeSignature(                                                          // Setter
                            Stream.of(implicitParams.stream(), Stream.<Class<?>>of(f.getType()))
                                    .flatMap(Function.identity())
                                    .collect(Collectors.toList()),
                            Void.TYPE,
                            Set.of(),
                            annotations)
            );
        }
    }
}
