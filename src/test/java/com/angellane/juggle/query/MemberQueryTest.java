package com.angellane.juggle.query;

import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.candidate.Param;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.match.TypeMatcher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MemberQueryTest {
    TypeMatcher conv    = new TypeMatcher(true);
    TypeMatcher noConv  = new TypeMatcher(false);

    @Test
    public void testScoreParamsIntInt() {
        MemberQuery q = new MemberQuery();
        q.params = Arrays.asList(
                ParamSpec.param(null, 0, 0, BoundedType.exactType(Integer.TYPE), null),
                ParamSpec.param(null, 0, 0, BoundedType.exactType(Integer.TYPE), null)
        );

        assertEquals(OptionalInt.of(0),   q.scoreParams(noConv, Arrays.asList(new Param(Integer.TYPE,  "i"), new Param(Integer.TYPE,  "i"))));
        assertEquals(OptionalInt.empty(), q.scoreParams(noConv, Arrays.asList(new Param(Integer.TYPE,  "i"), new Param(Integer.class, "i"))));
        assertEquals(OptionalInt.empty(), q.scoreParams(noConv, Arrays.asList(new Param(Integer.class, "i"), new Param(Integer.TYPE,  "i"))));
        assertEquals(OptionalInt.empty(), q.scoreParams(noConv, Arrays.asList(new Param(Integer.class, "i"), new Param(Integer.class, "i"))));

        assertEquals(OptionalInt.of(0), q.scoreParams(conv, Arrays.asList(new Param(Integer.TYPE, "i"),  new Param(Integer.TYPE,  "i"))));
        assertEquals(OptionalInt.of(2), q.scoreParams(conv, Arrays.asList(new Param(Integer.TYPE, "i"),  new Param(Integer.class, "i"))));
        assertEquals(OptionalInt.of(2), q.scoreParams(conv, Arrays.asList(new Param(Integer.class, "i"), new Param(Integer.TYPE,  "i"))));
        assertEquals(OptionalInt.of(4), q.scoreParams(conv, Arrays.asList(new Param(Integer.class, "i"), new Param(Integer.class, "i"))));
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
            q.params = Arrays.asList(
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
