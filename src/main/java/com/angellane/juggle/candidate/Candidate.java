/*
 *  Juggle -- an API search tool for Java
 *
 *  Copyright 2020,2023 Paul Bennett
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
package com.angellane.juggle.candidate;

import com.angellane.juggle.match.Accessibility;

import java.lang.reflect.Modifier;
import java.util.Set;

public interface Candidate {
    // These are the "other" modifiers we're interested in. Why am I not
    // using `~(Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED)`?
    // Because there are some modifiers that aren't published via the public
    // API of java.lang.reflect.Modifier (for example 0x4000 = ENUM).  So
    // instead we'll limit ourselves just to those modifiers that Juggle
    // knows about.
    //
    // Public modifier bits we currently deliberately ignore:
    //   INTERFACE
    // Other modifier bits presently named in the Modifier source:
    //   BRIDGE, VARARGS, SYNTHETIC, ANNOTATION, ENUM, MANDATED
    //
    // Modifiers that are not represented in the Modifier bits:
    //   sealed, non-sealed
    //
    int OTHER_MODIFIERS_MASK
            = Modifier.STATIC   | Modifier.FINAL      | Modifier.SYNCHRONIZED
            | Modifier.VOLATILE | Modifier.TRANSIENT  | Modifier.NATIVE
            | Modifier.ABSTRACT | Modifier.STRICT
            ;

    Set<Class<?>>   annotationTypes();
    Accessibility   accessibility();
    int             otherModifiers();
    String          declarationName();
    String          packageName();
}
