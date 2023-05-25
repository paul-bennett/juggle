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
package com.angellane.juggle.formatter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnsiColourFormatterTest {
    Formatter f = new AnsiColourFormatter();

    @Test
    void testFormatKeyword() {
        assertEquals("\033[2mprivate\033[0;22;23;24m", f.formatKeyword("private"));
    }

    @Test
    void testFormatPackageName() {
        assertEquals("\033[2m\033[32mcom.angellane.juggle\033[0;22;23;24m", f.formatPackageName("com.angellane.juggle"));
    }

    @Test
    void testFormatClassName() {
        assertEquals("\033[32mJuggler\033[0;22;23;24m", f.formatClassName("Juggler"));
    }

    @Test
    void testFormatMethodName() {
        assertEquals("\033[92mgoJuggle\033[0;22;23;24m", f.formatMethodName("goJuggle"));
    }

    @Test
    void testFormatType() {
        assertEquals("\033[94mint\033[0;22;23;24m", f.formatType("int"));
        assertEquals("\033[94mcom.angellane.juggle.Juggler\033[0;22;23;24m", f.formatType("com.angellane.juggle.Juggler"));
    }
}
