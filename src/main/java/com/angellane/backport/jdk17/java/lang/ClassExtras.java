package com.angellane.backport.jdk17.java.lang;

import com.angellane.backport.jdk17.java.lang.reflect.RecordComponent;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassExtras {
    static Method isSealed                  = null;
    static Method isRecord                  = null;
    static Method getPermittedSubclasses    = null;
    static Method getRecordComponents       = null;

    static {
        try {
            Class<?> c = Class.class;

            isSealed                = c.getMethod("isSealed");
            isRecord                = c.getMethod("isRecord");
            getPermittedSubclasses  = c.getMethod("getPermittedSubclasses");
            getRecordComponents     = c.getMethod("getRecordComponents");
        }
        catch (NoSuchMethodException ignored) {}
    }
    public static boolean isSealed(Class<?> c) {
        try {
            return (Boolean)isSealed.invoke(c);
        }
        catch (NullPointerException
               | IllegalAccessException
               | InvocationTargetException ignore
        ) {
            return false;
        }
    }

    public static boolean isRecord(Class<?> c) {
        try {
            return (Boolean)isRecord.invoke(c);
        }
        catch (NullPointerException
                | IllegalAccessException
                | InvocationTargetException ignore
        ) {
            return false;
        }
    }

    public static Class<?> arrayType(Class<?> c) {
        return Array.newInstance(c, 0).getClass();
    }

    public static RecordComponent[] getRecordComponents(Class<?> c) {
        try {
            Object[] rcs = (Object[])getRecordComponents.invoke(c);
            RecordComponent[] ret = new RecordComponent[rcs.length];
            for (int i=0; i < rcs.length; i++)
                ret[i] = new RecordComponent(rcs[i]);
            return ret;
        }
        catch (NullPointerException
               | IllegalAccessException
               | InvocationTargetException ignore
        ) {
            return null;
        }
    }

    public static Class<?>[] getPermittedSubclasses(Class<?> c) {
        try {
            return (Class<?>[])getPermittedSubclasses.invoke(c);
        }
        catch (NullPointerException
               | IllegalAccessException
               | InvocationTargetException ignore
        ) {
            return null;
        }
    }
}
