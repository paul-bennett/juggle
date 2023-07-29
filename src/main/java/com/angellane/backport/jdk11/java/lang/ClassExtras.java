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
package com.angellane.backport.jdk11.java.lang;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClassExtras {
    private static Class<?> _class      = Class.class;
    private static Method   _getModule  = null;

    static {
        try {
            _getModule = _class.getMethod("getModule");
        } catch (NoSuchMethodException ignored) {}

        assert !Module._modulesSupported() || null != _getModule;
    }

    public static Module getModule(Class<?> c) {
        try {
            return new Module(_getModule.invoke(c));
        } catch (InvocationTargetException
                 | IllegalAccessException
                 | ClassCastException ignored) { }

        return null;
    }
}
