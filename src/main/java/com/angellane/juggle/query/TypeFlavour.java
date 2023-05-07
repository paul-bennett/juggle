package com.angellane.juggle.query;

/**
 * This enum represents the 'flavour' of a type.  Java reflects pretty much
 * everything as a java.lang.Class: classes, interfaces, annotations, enums
 * and records.  This gives us a way of distinguishing between them without
 * resorting to the `Class.isXxx()` methods.
 */
public enum TypeFlavour {
    CLASS, INTERFACE, ANNOTATION, ENUM, RECORD;

    public static TypeFlavour forClass(Class<?> c) {
        return c.isAnnotation() ? ANNOTATION
                : c.isInterface() ? INTERFACE
                : c.isEnum() ? ENUM
                : c.isRecord() ? RECORD
                : CLASS;
    }
}
