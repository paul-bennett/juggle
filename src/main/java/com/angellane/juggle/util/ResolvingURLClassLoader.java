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
