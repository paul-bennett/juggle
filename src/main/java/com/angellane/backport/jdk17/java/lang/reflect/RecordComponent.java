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
