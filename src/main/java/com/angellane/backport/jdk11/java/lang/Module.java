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

public class Module {
    private static Class<?> _module     = null;
    private static Method _isExported   = null;
    private static Method _getName      = null;

    static {
        try {
            _module = Class.forName("java.lang.Module");
            _isExported = _module.getMethod("isExported", String.class);
            _getName = _module.getMethod("getName");
        } catch (ClassNotFoundException
                 | NoSuchMethodException ignored) {}

        assert !_modulesSupported() || Runtime.version().feature() >= 9;
        assert !_modulesSupported() || null != _isExported;
        assert !_modulesSupported() || null != _getName;
    }

    private final Object _wrapped;

    Module(Object wrap) {
        if (_module.isAssignableFrom(wrap.getClass()))
            _wrapped = wrap;
        else
            throw new ClassCastException();
    }

    public static boolean _modulesSupported() {
        return _module != null;
    }

    public boolean isExported(String pn) {
        try {
            return (Boolean)_isExported.invoke(_wrapped, pn);
        } catch (InvocationTargetException
                 | IllegalAccessException
                 | ClassCastException
                 | NullPointerException ignored) {}

        return false;
    }

    public String getName() {
        try {
            return (String)_getName.invoke(_wrapped);
        } catch (InvocationTargetException
        | IllegalAccessException
        | ClassCastException ignored) {}

        return null;
    }
}
