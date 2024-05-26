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

import java.util.Arrays;

import static com.angellane.juggle.formatter.AnsiColourFormatter.EscapeSequence.*;

public class AnsiColourFormatter implements Formatter {
    enum EscapeSequence {
        // These magic numbers come from https://en.wikipedia.org/wiki/ANSI_escape_code
        // But I should really study https://www.ecma-international.org/publications-and-standards/standards/ecma-48/.
        NORMAL("0;22;23;24"),
        FAINT("2"), ITALIC("3"), UNDERLINE("4"),
        BLACK("30"),        RED("31"),              GREEN("32"),        YELLOW("33"),
        BLUE("34"),         MAGENTA("35"),          CYAN("36"),         WHITE("37"),
        BRIGHT_BLACK("90"), BRIGHT_RED("91"),       BRIGHT_GREEN("92"), BRIGHT_YELLOW("93"),
        BRIGHT_BLUE("94"),  BRIGHT_MAGENTA("95"),   BRIGHT_CYAN("96"),  BRIGHT_WHITE("97");


        private static final String CSI = "\033[";

        private final String code;
        EscapeSequence(String code) { this.code = code; }

        public String toString() { return CSI + code + "m"; }
    }

    String format(String s, EscapeSequence... formats) {
        StringBuilder b = new StringBuilder();
        Arrays.stream(formats).forEach(b::append);
        b.append(s);
        b.append(NORMAL);
        return b.toString();
    }

    @Override public String formatPunctuation   (String s)  { return format(s, FAINT        ); }
    @Override public String formatKeyword       (String s)  { return format(s, FAINT        ); }
    @Override public String formatPackageName   (String s)  { return format(s, FAINT, GREEN ); }
    @Override public String formatClassName     (String s)  { return format(s, GREEN        ); }
    @Override public String formatMethodName    (String s)  { return format(s, BRIGHT_GREEN ); }
    @Override public String formatType          (String s)  { return format(s, BRIGHT_BLUE  ); }

    @Override public String formatInfo          (String s)  { return format(s, BRIGHT_CYAN  ); }
    @Override public String formatWarning       (String s)  { return format(s, BRIGHT_YELLOW); }
    @Override public String formatError         (String s)  { return format(s, BRIGHT_RED   ); }
}
