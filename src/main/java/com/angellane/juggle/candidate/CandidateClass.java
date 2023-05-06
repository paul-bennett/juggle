package com.angellane.juggle.candidate;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.query.ParamSpec;

import java.util.List;
import java.util.Set;

public record CandidateClass(
        Class<?> clazz,
        Kind kind,
        Accessibility accessibility,
        int otherModifiers,
        Set<Class<?>> annotationTypes,
        Class<?> superClass,
        Set<Class<?>> superInterfaces,
        List<ParamSpec> recordComponents
)
implements Candidate
{
    enum Kind { CLASS, INTERFACE, ANNOTATION, ENUM, RECORD }
}
