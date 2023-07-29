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

import com.angellane.backport.jdk11.java.lang.module.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ModuleLayer {
    private static Class<?> _moduleLayer        = null;
    private static Method   _boot               = null;
    private static Method   _configuration      = null;

    static {
        try {
            _moduleLayer = Class.forName("java.lang.ModuleLayer");
            _boot = _moduleLayer.getMethod("boot");
            _configuration = _moduleLayer.getMethod("configuration");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }

        assert !Module._modulesSupported() || null != _moduleLayer;
        assert !Module._modulesSupported() || null != _boot;
        assert !Module._modulesSupported() || null != _configuration;
    }

    private Object _wrapped = null;

    public ModuleLayer(Object wrap) {
        if (_moduleLayer.isAssignableFrom(wrap.getClass()))
            _wrapped = wrap;
        else
            throw new ClassCastException();
    }


    public static ModuleLayer boot() {
        try {
            return new ModuleLayer(_boot.invoke(null));
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {
        }
        return null;
    }

    public Configuration configuration() {
        try {
            if (_wrapped != null)
                return new Configuration(_configuration.invoke(_wrapped));
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}
        return null;
    }
}
