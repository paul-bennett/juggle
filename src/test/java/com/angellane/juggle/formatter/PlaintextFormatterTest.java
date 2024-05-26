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
package com.angellane.juggle.formatter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlaintextFormatterTest {
    Formatter f = new PlaintextFormatter();

    @Test
    void testFormatKeyword() {
        assertEquals("private", f.formatKeyword("private"));
    }

    @Test
    void testFormatPackageName() {
        assertEquals("com.angellane.juggle", f.formatPackageName("com.angellane.juggle"));
    }

    @Test
    void testFormatClassName() {
        assertEquals("Juggler", f.formatClassName("Juggler"));
    }

    @Test
    void testFormatMethodName() {
        assertEquals("goJuggle", f.formatMethodName("goJuggle"));
    }

    @Test
    void testFormatType() {
        assertEquals("int", f.formatType("int"));
        assertEquals("com.angellane.juggle.Juggler", f.formatType("com.angellane.juggle.Juggler"));
    }
}
