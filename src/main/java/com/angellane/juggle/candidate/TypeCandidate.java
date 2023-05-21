package com.angellane.juggle.candidate;

import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.query.TypeFlavour;

import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record TypeCandidate(
        Class<?>                clazz,
        TypeFlavour             flavour,

        Set<Class<?>>           annotationTypes,
        Accessibility           accessibility,
        int                     otherModifiers,
        String                  declarationName,

        Class<?>                superClass,
        Set<Class<?>>           superInterfaces,
        Set<Class<?>>           permittedSubtypes,
        List<RecordComponent>   recordComponents
)
implements Candidate
{
    public String packageName() {
        return clazz.getPackageName();
    }

    public static TypeCandidate candidateForType(Class<?> c) {
        TypeFlavour f                       = TypeFlavour.forClass(c);
        Accessibility access                =
                Accessibility.fromModifiers(c.getModifiers());
        int mods = c.getModifiers() & OTHER_MODIFIERS_MASK;
        Set<Class<?>> annotations           =
                Arrays.stream(c.getDeclaredAnnotations())
                        .map(Annotation::annotationType)
                        .collect(Collectors.toSet());
        Class<?> superClass                 = c.getSuperclass();
        Set<Class<?>> superInterfaces       = Set.of(c.getInterfaces());
        Set<Class<?>> permittedSubtypes     =
                c.getPermittedSubclasses() == null
                ? Set.of()
                : Set.of(c.getPermittedSubclasses());
        List<RecordComponent> recordComponents =
                c.getRecordComponents() == null
                ? List.of()
                : List.of(c.getRecordComponents());

        return new TypeCandidate(c, f, annotations, access, mods,
                c.getSimpleName(), superClass, superInterfaces,
                permittedSubtypes, recordComponents);
    }

}
