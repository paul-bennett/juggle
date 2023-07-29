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
import java.util.Collection;
import java.util.Optional;

public final class Configuration {
    private static Class<?> _configuration  = null;
    private static Class<?> _moduleFinder   = null;
    private static Class<?> _findException  = null;
    private static Method   _findModule     = null;
    private static Method   _resolve        = null;

    static {
        try {
            _configuration = Class.forName("java.lang.module.Configuration");
            _moduleFinder  = Class.forName("java.lang.module.ModuleFinder");
            _findException = Class.forName("java.lang.module.FindException");
            _findModule = _configuration.getMethod("findModule", String.class);
            _resolve    = _configuration.getMethod(
                    "resolve", _moduleFinder, _moduleFinder, Collection.class
            );
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}

        assert !Module._modulesSupported() || null != _configuration;
        assert !Module._modulesSupported() || null != _moduleFinder;
        assert !Module._modulesSupported() || null != _findException;
        assert !Module._modulesSupported() || null != _findModule;
        assert !Module._modulesSupported() || null != _resolve;
    }

    private final Object _wrapped;     // Actual type: java.lang.module.Configuration

    public Configuration(Object wrap) {
        if (_configuration.isAssignableFrom(wrap.getClass()))
            _wrapped = wrap;
        else
            throw new ClassCastException();
    }


    public Optional<ResolvedModule> findModule(String name) {
        try {
            return ((Optional<Object>)_findModule.invoke(_wrapped, name))
                    .flatMap(t -> Optional.of(new ResolvedModule(t)));
        } catch (InvocationTargetException | IllegalAccessException
                | ClassCastException ignored) {}

        return Optional.empty();
    }

    public Configuration resolve(ModuleFinder before,
                                 ModuleFinder after,
                                 Collection<String> roots) {
        try {
            return new Configuration(
                    _resolve.invoke(_wrapped, before._unwrap(), after._unwrap(), roots)
            );
        } catch (InvocationTargetException ex) {
            Throwable thrown = ex.getTargetException();
            if (_findException.isAssignableFrom(thrown.getClass()))
                throw new FindException(thrown);
        } catch (IllegalAccessException | ClassCastException ignored) {}

        return null;
    }
}
