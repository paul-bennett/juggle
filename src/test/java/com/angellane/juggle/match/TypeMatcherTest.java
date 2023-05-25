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
package com.angellane.juggle.match;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static com.angellane.juggle.match.TypeMatcher.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeMatcherTest {
    TypeMatcher noConv  = new TypeMatcher(false);
    TypeMatcher conv    = new TypeMatcher(true);

    private void checkMatches(Integer noConvScore, Integer convScore,
                              Class<?> targetType, Class<?> exprType) {
        assertEquals(noConvScore == null
                        ? OptionalInt.empty() : OptionalInt.of(noConvScore),
                noConv.scoreTypeMatch(targetType, exprType));
        assertEquals(convScore == null
                        ? OptionalInt.empty() : OptionalInt.of(convScore),
                conv.scoreTypeMatch(targetType, exprType));
    }

    @Test
    public void testPrimitiveId() {
        checkMatches(IDENTITY_SCORE, IDENTITY_SCORE,
                Integer.TYPE, Integer.TYPE);
    }

    @Test
    public void testPrimitiveNarrowing() {
        checkMatches(null, null, Integer.TYPE, Long.TYPE);
    }

    @Test
    public void testPrimitiveWidening() {
        checkMatches(null, WIDENING_SCORE, Integer.TYPE, Byte.TYPE);
    }

    @Test
    public void testBoxing() {
        checkMatches(null, BOXING_SCORE, Integer.class, Integer.TYPE);
        checkMatches(null, BOXING_SCORE + WIDENING_SCORE,
                Number.class, Short.TYPE);
        checkMatches(null, null, Integer.class, Short.TYPE);
        checkMatches(null, null, Short.class, Integer.TYPE);
    }

    @Test
    public void testUnboxing() {
        checkMatches(null, BOXING_SCORE, Integer.TYPE, Integer.class);
        checkMatches(null, BOXING_SCORE + WIDENING_SCORE,
                Integer.TYPE, Short.class);
        checkMatches(null, null, Integer.TYPE, Number.class);
        checkMatches(null, null, Short.TYPE, Integer.class);
    }

    @Test
    public void testReferenceId() {
        checkMatches(IDENTITY_SCORE, IDENTITY_SCORE, Short.class, Short.class);
    }

    @Test
    public void testReferenceNarrowing() {
        checkMatches(null, null, Integer.class, Object.class);
    }

    @Test
    public void testReferenceWidening() {
        checkMatches(null, WIDENING_SCORE, Object.class, Integer.class);
    }

    @Test
    public void testReferenceUnrelated() {
        checkMatches(null, null, String.class, Integer.class);
        checkMatches(null, null, Integer.class, String.class);
    }

    @Test
    public void testVoid() {
        checkMatches(IDENTITY_SCORE, IDENTITY_SCORE, Void.TYPE, Void.TYPE);
        checkMatches(null, null, Object.class, Void.TYPE);
        checkMatches(null, null, Void.TYPE, Object.class);
    }

}
