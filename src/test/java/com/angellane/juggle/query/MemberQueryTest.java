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
package com.angellane.juggle.query;

import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.candidate.Param;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.match.TypeMatcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MemberQueryTest {
    TypeMatcher conv    = new TypeMatcher(true);
    TypeMatcher noConv  = new TypeMatcher(false);

    @Test
    public void testScoreParamsIntInt() {
        MemberQuery q = new MemberQuery();
        q.params = List.of(
                ParamSpec.param(null, 0, 0, BoundedType.exactType(Integer.TYPE), null),
                ParamSpec.param(null, 0, 0, BoundedType.exactType(Integer.TYPE), null)
        );

        assertEquals(OptionalInt.of(0),   q.scoreParams(noConv, List.of(new Param(Integer.TYPE,  "i"), new Param(Integer.TYPE,  "i"))));
        assertEquals(OptionalInt.empty(), q.scoreParams(noConv, List.of(new Param(Integer.TYPE,  "i"), new Param(Integer.class, "i"))));
        assertEquals(OptionalInt.empty(), q.scoreParams(noConv, List.of(new Param(Integer.class, "i"), new Param(Integer.TYPE,  "i"))));
        assertEquals(OptionalInt.empty(), q.scoreParams(noConv, List.of(new Param(Integer.class, "i"), new Param(Integer.class, "i"))));

        assertEquals(OptionalInt.of(0), q.scoreParams(conv, List.of(new Param(Integer.TYPE, "i"),  new Param(Integer.TYPE,  "i"))));
        assertEquals(OptionalInt.of(2), q.scoreParams(conv, List.of(new Param(Integer.TYPE, "i"),  new Param(Integer.class, "i"))));
        assertEquals(OptionalInt.of(2), q.scoreParams(conv, List.of(new Param(Integer.class, "i"), new Param(Integer.TYPE,  "i"))));
        assertEquals(OptionalInt.of(4), q.scoreParams(conv, List.of(new Param(Integer.class, "i"), new Param(Integer.class, "i"))));
    }

    @Test
    public void testScoreReturnInt() {
        MemberQuery q = new MemberQuery();
        q.returnType = BoundedType.exactType(Integer.TYPE);

        assertEquals(OptionalInt.of(0),   q.scoreReturn(noConv, Integer.TYPE));
        assertEquals(OptionalInt.empty(), q.scoreReturn(noConv, Integer.class));

        assertEquals(OptionalInt.of(0),   q.scoreReturn(conv, Integer.TYPE));
        assertEquals(OptionalInt.of(2),   q.scoreReturn(conv, Integer.class));
    }

    // Try to parse the MemberQuery that would result from "int(int,int)"
    // (see CmdLineTest) against the Candidate representing
    // Integer.compareTo(Integer)
    @Test
    public void testScoreIntIntToInt() {
        try {
            Method m = Integer.class.getMethod("compareTo", Integer.class);
            MemberCandidate c = MemberCandidate.memberFromMethod(m);

            MemberQuery q = new MemberQuery();
            q.setAccessibility(Accessibility.PUBLIC);
            q.returnType = BoundedType.exactType(Integer.TYPE);
            q.params = List.of(
                    ParamSpec.param(null, 0, 0, BoundedType.exactType(Integer.TYPE), null),
                    ParamSpec.param(null, 0, 0, BoundedType.exactType(Integer.TYPE), null)
            );

            assertEquals(OptionalInt.empty(), q.scoreCandidate(noConv, c));
            assertEquals(OptionalInt.of(4), q.scoreCandidate(conv, c));

        } catch (NoSuchMethodException e) {
            fail("Couldn't find method");
        }
    }
}
