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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Optional;

public class ModuleReference {
    private static Class<?> _moduleReference    = null;
    private static Method   _location           = null;
    private static Method   _descriptor         = null;
    private static Method   _open               = null;

    static {
        try {
            _moduleReference = Class.forName("java.lang.module.ModuleReference");
            _location   = _moduleReference.getMethod("location");
            _descriptor = _moduleReference.getMethod("descriptor");
            _open       = _moduleReference.getMethod("open");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}

        assert !Module._modulesSupported() || null != _moduleReference;
        assert !Module._modulesSupported() || null != _location;
        assert !Module._modulesSupported() || null != _descriptor;
        assert !Module._modulesSupported() || null != _open;
    }

    private final Object _wrapped;

    public ModuleReference(Object wrap) {
        if (_moduleReference.isAssignableFrom(wrap.getClass()))
            _wrapped = wrap;
        else
            throw new ClassCastException();
    }


    public final Optional<URI> location() {
        try {
            return Optional.of((URI)
                    _location.invoke(_wrapped)
            );
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}

        return Optional.empty();
    }

    public final ModuleDescriptor descriptor() {
        try {
            return new ModuleDescriptor(
                    _descriptor.invoke(_wrapped)
            );
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}

        return null;
    }

    public ModuleReader open() throws IOException {
        try {
            return new ModuleReader(
                    _open.invoke(_wrapped)
            );
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}

        return null;
    }
}
