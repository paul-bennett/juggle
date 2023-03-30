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
                    switch(ch) {
                        case '\\':  return ESC;
                        case '\'':  return SGLQ;
                        case '"':   return DBLQ;
                        default:    addChar(ch);  return state;
                    }
                }

            case SGLQ: if (ch == '\'') return NORM; else { addChar(ch); return state; }
            case DBLQ: if (ch == '"')  return NORM; else { addChar(ch); return state; }
            case ESC:  addChar(ch); return NORM;
            case WS:   if (Character.isWhitespace(ch)) return state; else { finishArg(); return step(NORM, ch); }

            default:   throw new IllegalStateException("Unknown parser state");
        }
    }

    enum State {NORM, SGLQ, DBLQ, ESC, WS}
    private StringBuilder nextArg;
    private List<String> args;
}
