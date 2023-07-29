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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ResolvedModule {
    private static Class<?> _resolvedModule     = null;
    private static Method   _reference          = null;

    static {
        try {
            _resolvedModule = Class.forName("java.lang.module.ResolvedModule");
            _reference = _resolvedModule.getMethod("reference");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}

        assert !Module._modulesSupported() || null != _resolvedModule;
        assert !Module._modulesSupported() || null != _reference;
    }

    private final Object _wrapped;

    public ResolvedModule(Object wrap) {
        if (_resolvedModule.isAssignableFrom(wrap.getClass()))
            _wrapped = wrap;
        else
            throw new ClassCastException();
    }

    public ModuleReference reference() {
        try {
            return new ModuleReference(
                    _reference.invoke(_wrapped)
            );
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}

        return null;
    }
}
