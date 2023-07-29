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
package com.angellane.backport.jdk11.java.lang.module;

import com.angellane.backport.jdk11.java.lang.Module;
import com.angellane.backport.jdk17.java.lang.ClassExtras;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

public class ModuleFinder {
    private static Class<?> _moduleFinder   = null;
    private static Method   _ofSystem       = null;
    private static Method   _of             = null;

    static {
        try {
            _moduleFinder   = Class.forName("java.lang.module.ModuleFinder");
            _ofSystem       = _moduleFinder.getMethod("ofSystem");
            _of             = _moduleFinder.getMethod("of", ClassExtras.arrayType(Path.class));
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}

        assert !Module._modulesSupported() || null != _moduleFinder;
        assert !Module._modulesSupported() || null != _ofSystem;
        assert !Module._modulesSupported() || null != _of;
    }

    private final Object _wrapped;

    ModuleFinder(Object wrap) {
        if (_moduleFinder.isAssignableFrom(wrap.getClass()))
            _wrapped = wrap;
        else
            throw new ClassCastException();
    }

    Object _unwrap() { return _wrapped; }

    public static ModuleFinder ofSystem() {
        try {
            return new ModuleFinder(_ofSystem.invoke(null));
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}
        return null;
    }

    public static ModuleFinder of(Path... entries) {
        try {
            return new ModuleFinder(_of.invoke(null, (Object) entries));
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}
        return null;
    }
}
