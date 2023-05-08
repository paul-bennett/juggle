package com.angellane.juggle.candidate;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.query.TypeFlavour;

import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record CandidateType(
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
    public static CandidateType candidateForType(Class<?> c) {
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

        return new CandidateType(c, f, annotations, access, mods,
                c.getSimpleName(), superClass, superInterfaces,
                permittedSubtypes, recordComponents);
    }

}
