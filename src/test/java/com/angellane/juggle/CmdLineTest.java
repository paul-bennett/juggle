package com.angellane.juggle;

import com.angellane.juggle.query.BoundedType;
import com.angellane.juggle.query.ParamSpec;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CmdLineTest {
    @Test
    public void testNoArgs() {
        String[] args = {};

        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);

        assertEquals(List.of(), result.unmatched());

        assertEquals(1, app.juggler.getImportedPackageNames().size());   // java.lang
        assertEquals(1, app.juggler.getSources().size());   // just the default source, java.base
    }

    @Test
    public void testJunkArgs() {
        String[] args = {"--bad-argument"};

        Main app = new Main();

        assertThrows(CommandLine.UnmatchedArgumentException.class, () -> new CommandLine(app).parseArgs(args));
    }

    @Test
    public void testNullParamsOption() {
        String[] args = {"()"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);
        app.parseDeclarationQuery(app.getQueryString());

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.juggler.memberQuery.params);
        assertEquals(0, app.juggler.memberQuery.params.size());
    }

    @Test
    public void testOneParamsOption() {
        String[] args = {"(Object))"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).parseArgs(args);
        app.parseDeclarationQuery(app.getQueryString());

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.juggler.memberQuery.params);
        assertEquals(1, app.juggler.memberQuery.params.size());
        assertEquals(ParamSpec.unnamed(BoundedType.exactType(Object.class)),
                app.juggler.memberQuery.params.get(0));
    }

    @Test
    public void testTwoParamsOptions() {
        String[] args = {"(String", ",", "Integer)"};
        Main app = new Main();
        ParseResult result = new CommandLine(app).setOverwrittenOptionsAllowed(true).parseArgs(args);
        app.parseDeclarationQuery(app.getQueryString());

        assertEquals(List.of(), result.unmatched());
        assertNotNull(app.juggler.memberQuery.params);
        assertEquals(2, app.juggler.memberQuery.params.size());
        assertEquals(ParamSpec.unnamed(BoundedType.exactType(String.class)),
                app.juggler.memberQuery.params.get(0));
        assertEquals(ParamSpec.unnamed(BoundedType.exactType(Integer.class)),
                app.juggler.memberQuery.params.get(1));
    }
}
