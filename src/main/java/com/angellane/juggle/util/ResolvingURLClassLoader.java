/*
 *  Juggle -- a declarative search tool for Java
 *
 *  Copyright 2020,2024 Paul Bennett
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
package com.angellane.juggle.util;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Just like a regular URLClassLoader, but always resolves (links) classes at load time.
 */
public class ResolvingURLClassLoader extends URLClassLoader {
    public ResolvingURLClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name, true);
    }

    public void linkClass(Class<?> cls) {
        super.resolveClass(cls);

        // Javadoc for java.lang.ClassLoader in OpenJDK 14 states that classes are linked (resolved) either
        // at loadClass time if the second argument is true, or as a result of calling resolveClass.
        // In practice neither of these methods (tried above, just in case) works.
        //
        // *sigh*
        //
        // Linking /does/ seem to happen during Class.getDeclaredFields(), so we call that here.
        // Hopefully this won't be optimised away by the dead code analyser.

        var ignored = cls.getDeclaredFields();
    }
}
