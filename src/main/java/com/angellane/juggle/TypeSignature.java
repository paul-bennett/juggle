package com.angellane.juggle;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeSignature {
    public final List<Class<?>> paramTypes;
    public final Class<?> returnType;

    public TypeSignature(List<Class<?>> paramTypes, Class<?> returnType) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    public static List<TypeSignature> of(Member m) {
        List<Class<?>> implicitParams = new ArrayList<>();

        // Handle the 'this' pointer for non-static members.
        // Note that Constructors appear to be non-static but they don't have a silent 'this'.
        if (!Modifier.isStatic(m.getModifiers()) && !(m instanceof Constructor<?>))
            implicitParams.add(m.getDeclaringClass());

        if (m instanceof Constructor<?>) {
            Constructor<?> c = (Constructor<?>)m;

            return List.of(new TypeSignature(
                    List.of(c.getParameterTypes()),
                    c.getDeclaringClass()
            ));
        }
        if (m instanceof Method) {
            Method e = (Method)m;

            return List.of(new TypeSignature(
                    Stream.of(implicitParams.stream(), Arrays.stream(e.getParameterTypes()))
                            .flatMap(Function.identity())
                            .collect(Collectors.toList()),
                    e.getReturnType()
            ));
        }
        else if (m instanceof Field) {
            Field f = (Field)m;

            return List.of(
                    new TypeSignature(implicitParams, f.getType()),                             // Getter
                    new TypeSignature(                                                          // Setter
                            Stream.concat(implicitParams.stream(), Stream.of(f.getType()))
                                    .collect(Collectors.toList()),
                            Void.TYPE
                    )
            );
        }
        else
            return List.of();
    }
}
