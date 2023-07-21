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
package com.angellane.juggle.testsupport;

import java.util.ArrayList;
import java.util.List;

import static com.angellane.juggle.testsupport.ShellParser.State.*;

public class ShellParser {
    /**
     * Performs basic shell-style parsing.  Treats the input as a series of whitespace-separated arguments.
     * An argument can be surrounded by quote marks (double or single), in which case internal whitespace is
     * legal.  The quote marks aren't included in the argument.  This also allows empty arguments -- by using
     * two quote marks in a row.  A double-quote within a single-quoted argument (and vice-versa) is not
     * interpreted as special.  Similarly, preceding any character by a backslash denies it any special
     * treatment.  To have a backslash on its own, escape it with another backslash.
     */

    public String[] parse(String input) {
        State state = WS;
        nextArg = null;
        args = new ArrayList<>();

        for (char ch : input.toCharArray())
            state = step(state, ch);

        finishArg();

        return args.toArray(new String[0]);
    }

    private void startArg()  { if (nextArg == null) nextArg = new StringBuilder(); }
    private void finishArg() { if (nextArg != null) args.add(nextArg.toString()); nextArg = null; }
    private void addChar(char ch)  { assert nextArg != null; nextArg.append(ch); }

    private State step(State state, char ch) {
        switch (state) {
            case NORM:
                if (Character.isWhitespace(ch))
                    return WS;
                else {
                    startArg(); // starts an arg if one not already being built
                    switch (ch) {
                        case '\\':                  return ESC;
                        case '\'':                  return SQ;
                        case '"':                   return DQ;
                        default:    addChar(ch);    return state;
                    }
                }

            case SQ:    if (ch == '\'') return NORM; else { addChar(ch); return state; }
            case DQ:    if (ch == '"')  return NORM; else { addChar(ch); return state; }
            case ESC:   addChar(ch);    return NORM;
            case WS:    if (Character.isWhitespace(ch)) return state; else { finishArg(); return step(NORM, ch); }

            default:    throw new IllegalStateException("Unknown parser state");
        }
    }

    enum State {NORM, SQ, DQ, ESC, WS}
    private StringBuilder nextArg;
    private List<String> args;
}
