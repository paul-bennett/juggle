package com.angellane.backport.jdk17.java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class RecordComponent {
    private static Class<?> clazz = null;
    private static Method getAnnotations = null;
    private static Method getType = null;
    private static Method getName = null;

    static {
        try {
            clazz = Class.forName("java.lang.reflect.RecordComponent");
            getAnnotations = clazz.getMethod("getAnnotations");
            getType = clazz.getMethod("getType");
            getName = clazz.getMethod("getName");
        } catch (ClassNotFoundException
                 | NoSuchMethodException ignored
        ) {
        }
    }
    private Object wrapped;

    public RecordComponent(Object rc) {
        assert rc.getClass() == clazz;
        wrapped = rc;
    }

    public Annotation[] getAnnotations() {
        try {
            return (Annotation[])getAnnotations.invoke(wrapped);
        } catch (IllegalAccessException
                 | InvocationTargetException
                 | ClassCastException ignored
        ) {
            return new Annotation[0];
        }
    }

    public Class<?> getType() {
        try {
            return (Class<?>)getType.invoke(wrapped);
        } catch (IllegalAccessException
                 | InvocationTargetException
                 | ClassCastException ignored
        ) {
            return null;
        }
    }

    public String getName() {
        try {
            return (String)getName.invoke(wrapped);
        } catch (IllegalAccessException
                 | InvocationTargetException
                 | ClassCastException ignored
        ) {
            return "";
        }
    }
}
