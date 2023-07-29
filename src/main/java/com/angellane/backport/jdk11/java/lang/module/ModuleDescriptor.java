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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleDescriptor {
    private static Class<?> _moduleDescriptor   = null;
    private static Method   _requires           = null;

    static {
        try {
            _moduleDescriptor = Class.forName("java.lang.module.ModuleDescriptor");
            _requires = _moduleDescriptor.getMethod("requires");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}

        assert !Module._modulesSupported() || null != _moduleDescriptor;
        assert !Module._modulesSupported() || null != _requires;
    }

    private Object _wrapped = null;

    public ModuleDescriptor(Object wrap) {
        if (_moduleDescriptor.isAssignableFrom(wrap.getClass()))
            _wrapped = wrap;
        else
            throw new ClassCastException();
    }

    public static final class Requires {
        private static Class<?> _requires       = null;
        private static Method   _name           = null;
        private static Method   _modifiers      = null;

        static {
            try {
                _requires   = Class.forName("java.lang.module.ModuleDescriptor$Requires");
                _name       = _requires.getMethod("name");
                _modifiers  = _requires.getMethod("modifiers");
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {}
        }

        private final Object _wrapped;

        Requires(Object wrap) {
            if (_requires.isAssignableFrom(wrap.getClass()))
                _wrapped = wrap;
            else
                throw new ClassCastException();
        }

        public String name() {
            try {
                return (String)_name.invoke(_wrapped);
            } catch (InvocationTargetException | IllegalAccessException
                     | ClassCastException ignored) {
            }
            return null;
        }

        public enum Modifier {
            MANDATED, STATIC, SYNTHETIC, TRANSITIVE
        }

        public Set<Modifier> modifiers() {
            try {
                return ((Set<? extends Enum>)_modifiers.invoke(_wrapped)).stream()
                        .map(e -> Modifier.valueOf(e.name()))
                        .collect(Collectors.toSet());
            } catch (InvocationTargetException | IllegalAccessException
                     | ClassCastException ignored) {
            }
            return null;
        }
    }

    public Set<Requires> requires() {
        try {
            return ((Set<?>)_requires.invoke(_wrapped)).stream()
                    .map(Requires::new)
                    .collect(Collectors.toSet());
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}
        return null;
    }
}
