/*
 *  Backport -- bringing future Java features to old JDKs
 *
 *  Copyright 2023 Paul Bennett
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
