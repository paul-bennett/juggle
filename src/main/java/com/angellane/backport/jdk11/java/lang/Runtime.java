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

public class Runtime {
    static Class<?> runtimeClass    = null;
    static Method versionMethod     = null;     // static Version version()

    static Class<?> versionClass    = null;
    static Method majorMethod       = null;     // int Version.major()
    static Method minorMethod       = null;     // int Version.minor()
    static Method featureMethod     = null;     // int Version.feature()
    static Method interimMethod     = null;     // int Version.interim()

    static Method getMethod(Class<?> c, String methodName) {
        try { return c.getMethod(methodName); }
        catch (NoSuchMethodException ignored) { return null; }
    }

    static {
        try {
            runtimeClass = Class.forName("java.lang.Runtime");

            versionMethod = getMethod(runtimeClass, "version");

            versionClass = Class.forName("java.lang.Runtime$Version");

            majorMethod   = getMethod(versionClass, "major");
            minorMethod   = getMethod(versionClass, "minor");

            // Previous two methods deprecated since JDK 10 in favour of these:
            featureMethod = getMethod(versionClass, "feature");
            interimMethod = getMethod(versionClass, "interim");
        }
        catch (ClassNotFoundException ignored) {}
    }
    public static class Version {
        private Object getVersion() {
            try {
                return versionMethod.invoke(null);
            }
            catch (NullPointerException
                   | IllegalAccessException
                   | InvocationTargetException ignored) {
                return null;
            }
        }

        /**
         * Grabs a specific component from a version structure.  Tries
         * to invoke each of the methods on the version object in turn,
         * returning the result of the first that can be cast to an int,
         * or the defaultValue if none are successful.
         */
        private int getVersionComponent(int defaultValue, Method... methods) {
            Object v = getVersion();
            if (v != null) {
                for (Method m : methods ) {
                    if (m != null)
                        try {
                            return (Integer)m.invoke(v);
                        } catch (InvocationTargetException
                                 | IllegalAccessException
                                 | ClassCastException
                                 | NullPointerException ignored) {}
                }
            }

            // Runtime.Version was introduced in JDK 9.  We don't support
            // anything less than JDK 8 so assume anything that gets this
            // far is running on 8.0

            return defaultValue;
        }

        public int feature() {
            return getVersionComponent(8, featureMethod, majorMethod);
        }

        public int interim() {
            return getVersionComponent(0, interimMethod, minorMethod);
        }
    }
    public static Version version() {
        return new Version();
    }
}
