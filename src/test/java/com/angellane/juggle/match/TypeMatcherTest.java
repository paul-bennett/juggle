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

import com.angellane.juggle.query.BoundedType;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static com.angellane.juggle.match.TypeMatcher.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeMatcherTest {
    TypeMatcher noConv  = new TypeMatcher(false);
    TypeMatcher conv    = new TypeMatcher(true);

    private void checkMatches(Integer noConvRetScore, Integer convRetScore,
                              Integer noConvParamScore, Integer convParamScore,
                              Class<?> targetType, Class<?> exprType) {
        assertEquals(noConvRetScore == null
                        ? OptionalInt.empty() : OptionalInt.of(noConvRetScore),
                noConv.scoreTypeMatch(BoundedType.exactType(targetType), exprType),
                "Matching without conversions, Class -> BT");
        assertEquals(convRetScore == null
                        ? OptionalInt.empty() : OptionalInt.of(convRetScore),
                conv.scoreTypeMatch(BoundedType.exactType(targetType), exprType),
                "Matching with conversions, Class -> BT");

        assertEquals(noConvParamScore == null
                        ? OptionalInt.empty() : OptionalInt.of(noConvParamScore),
                noConv.scoreTypeMatch(targetType, BoundedType.exactType(exprType)),
                "Matching without conversions, BT -> Class");
        assertEquals(convParamScore == null
                        ? OptionalInt.empty() : OptionalInt.of(convParamScore),
                conv.scoreTypeMatch(targetType, BoundedType.exactType(exprType)),
                "Matching with conversions, BT -> Class");
    }


    @Test
    public void testPrimitiveId() {
        checkMatches(IDENTITY_COST, IDENTITY_COST,
                IDENTITY_COST, IDENTITY_COST,
                Integer.TYPE, Integer.TYPE);
    }

    @Test
    public void testPrimitiveNarrowing() {
        checkMatches(null, null,
                null, null,
                Integer.TYPE, Long.TYPE);
    }

    @Test
    public void testPrimitiveWidening() {
        checkMatches(null, WIDENING_COST,
                null, WIDENING_COST,
                Integer.TYPE, Byte.TYPE);
    }

    @Test
    public void testBoxingIntegerInt() {
        checkMatches(null, BOXING_COST,
                null, BOXING_COST,
                Integer.class, Integer.TYPE);
    }
    @Test
    public void testBoxingNumberShort() {
        checkMatches(null, BOXING_COST + WIDENING_COST,
                null, BOXING_COST + WIDENING_COST,
                Number.class, Short.TYPE);
    }
    @Test
    public void testBoxingIntegerShort() {
        checkMatches(null, null,
                null, null,
                Integer.class, Short.TYPE);
    }
    @Test
    public void testBoxingShortInteger() {
        checkMatches(null, null,
                null, null,
                Short.class, Integer.TYPE);
    }

    @Test
    public void testUnboxingIntInteger() {
        checkMatches(null, BOXING_COST,
                null, BOXING_COST,
                Integer.TYPE, Integer.class);
    }
    @Test
    public void testUnboxingIntShort() {
        checkMatches(null, BOXING_COST + WIDENING_COST,
                null, BOXING_COST + WIDENING_COST,
                Integer.TYPE, Short.class);
    }
    @Test
    public void testUnboxingIntNumber() {
        checkMatches(null, null,
                null, null,
                Integer.TYPE, Number.class);
    }
    @Test
    public void testUnboxingShortInteger() {
        checkMatches(null, null,
                null, null,
                Short.TYPE, Integer.class);
    }
    @Test
    public void testReferenceId() {
        checkMatches(IDENTITY_COST, IDENTITY_COST,
                IDENTITY_COST, IDENTITY_COST,
                Short.class, Short.class);
    }

    @Test
    public void testReferenceNarrowing() {
        checkMatches(null, null,
                null, null,
                Integer.class, Object.class);
    }

    @Test
    public void testReferenceWidening() {
        checkMatches(null, WIDENING_COST,
                null, WIDENING_COST,
                Object.class, Integer.class);
    }

    @Test
    public void testReferenceUnrelated() {
        checkMatches(null, null, null, null, String.class, Integer.class);
        checkMatches(null, null, null, null, Integer.class, String.class);
    }

    @Test
    public void testVoid() {
        checkMatches(IDENTITY_COST, IDENTITY_COST,
                IDENTITY_COST, IDENTITY_COST,
                Void.TYPE, Void.TYPE);
        checkMatches(null, null, null, null, Object.class, Void.TYPE);
        checkMatches(null, null, null, null, Void.TYPE, Object.class);
    }

}
