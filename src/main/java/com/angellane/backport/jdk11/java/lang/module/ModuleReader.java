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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class ModuleReader implements Closeable {
    private static Class<?> _moduleReader   = null;
    private static Method   _list           = null;
    private static Method   _close          = null;

    static {
        try {
            _moduleReader = Class.forName("java.lang.module.ModuleReader");
            _list = _moduleReader.getMethod("list");
            _close = _moduleReader.getMethod("close");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}

        assert !Module._modulesSupported() || null != _moduleReader;
        assert !Module._modulesSupported() || null != _list;
        assert !Module._modulesSupported() || null != _close;
    }

    private final Object _wrapped;

    public ModuleReader(Object wrap) {
        if (_moduleReader.isAssignableFrom(wrap.getClass()))
            _wrapped = wrap;
        else
            throw new ClassCastException();
    }

    public Stream<String> list() {
        try {
            return (Stream<String>) _list.invoke(_wrapped);
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}

        return null;
    }

    @Override
    public void close() throws IOException {
        try {
            _close.invoke(_wrapped);
        } catch (InvocationTargetException | IllegalAccessException
                 | ClassCastException ignored) {}
    }
}
