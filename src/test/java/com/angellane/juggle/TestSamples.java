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
package com.angellane.juggle;

import com.angellane.juggle.testsupport.ShellParser;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.angellane.juggle.TestSamples.State.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestSamples {
    @Test
    public void testSimpleRun() {
        // Confirm that our 'runTest' function does the right thing, with a simple example.
        runTest("java.time.LocalTime (java.time.Clock)",
                "public static java.time.LocalTime java.time.LocalTime.now(java.time.Clock)\n");
    }

    @TestFactory
    public Stream<DynamicNode> testSampleFiles() {
        ClassLoader cl = getClass().getClassLoader();

        return Stream.of( "README.md"
                        , "REGRESSIONS.md"
                        , "COVERAGE.md"
                        , "DECLARATIONS.md"
                        , "TYPE-SEARCH.md"
                )
                .map(fn -> DynamicContainer.dynamicContainer(fn, sampleTestStreamFrom(fn, cl.getResource(fn))));
    }

    enum State { SKIP, CMD, OUT }

    private Stream<DynamicNode> sampleTestStreamFrom(String filename, URL url) {
        assertNotNull(url, "Couldn't load test resource");

        List<DynamicNode> ret = new ArrayList<>();

        // Within the file, examples of running the command are on lines that start "$ juggle ".  The rest of the
        // line provides the arguments; these may spill onto subsequent lines if the last character before the end
        // of the line is a backslash.
        //
        // Following the command line is the output.  This runs up (but excludes) the next line containing just "$".
        //
        // Lines after the end of the output (and before the first command line) are treated as narrative text
        // to be ignored.

        try (InputStreamReader isr = new InputStreamReader(url.openStream());
             LineNumberReader br = new LineNumberReader(isr))
        {
            final String DISABLED_CMD = "% juggle ";
            final String DISABLED_EOT = "%";

            final String JUGGLE_CMD = "$ juggle ";
            final String CONTINUE = "\\";
            final String EOT = "$";

            State state = SKIP;

            boolean enabledTest   = false;
            int startLine = 0;

            StringBuilder command = new StringBuilder();
            StringBuilder output  = new StringBuilder();

            String line;
            while (null != (line = br.readLine()))
                // Note: line doesn't include a terminating \r, \n or \r\n

                switch (state) {
                    case SKIP:
                        if (line.startsWith(JUGGLE_CMD))
                            enabledTest = true;
                        else if (line.startsWith(DISABLED_CMD))
                            enabledTest = false;
                        else
                            break;

                        if (!line.startsWith(JUGGLE_CMD) &&
                                !line.startsWith(DISABLED_CMD))
                            break;

                        output  = new StringBuilder();
                        command = new StringBuilder();
                        startLine = br.getLineNumber();

                        // fall through
                    case CMD:
                        if (line.endsWith(CONTINUE)) {
                            state = CMD;
                            line = line.substring(0, line.length() - CONTINUE.length());
                        }
                        else
                            state = OUT;
                        command.append(line);
                        break;

                    case OUT:
                        if (enabledTest && line.equals(EOT)) {
                            String cmdStr = command.toString();
                            assertTrue(cmdStr.startsWith(JUGGLE_CMD));
                            String args = command.substring(JUGGLE_CMD.length());
                            String expected = output.toString();

                            String testName = "#" + startLine + ": " + args;
                            URI testURI = URI.create("classpath:/" + filename + "?line=" + startLine);

                            ret.add(DynamicTest.dynamicTest(testName, testURI,
                                    () -> runTest(args, expected)));
                            state = SKIP;
                        }
                        else if (!enabledTest && line.equals(DISABLED_EOT)) {
                            String args = command.substring(DISABLED_CMD.length());

                            String testName = "#" + startLine + ": " + args;
                            URI testURI = URI.create("classpath:/" + filename + "?line=" + startLine);

                            ret.add(DynamicTest.dynamicTest(testName, testURI,
                                    () -> Assumptions.abort("Test disabled with %")));
                        }
                        else
                            output.append(line).append('\n');
                        break;
                }
        } catch (IOException e) {
            fail(e);
        }

        return ret.stream();
    }


    private void runTest(String inputLine, String expectedOutput) {
        ShellParser p = new ShellParser();
        String[] args = p.parse(inputLine);

        PrintStream savedOut = System.out, savedErr = System.err;

        try (ByteArrayOutputStream bs = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(bs))
        {
            System.setOut(ps);
            System.setErr(ps);

            Main.main(args);

            String actualOutput = bs.toString();

            assertEquals(
                    expectedOutput.lines().map(s -> s + "\n").sorted()
                            .collect(StringBuilder::new,
                                    StringBuilder::append,
                                    StringBuilder::append).toString(),
                    actualOutput.lines().map(s -> s + "\n").sorted()
                            .collect(StringBuilder::new,
                                    StringBuilder::append,
                                    StringBuilder::append).toString(),
                    "Sorted output should match"
            );
            assertEquals(expectedOutput, actualOutput,
                    "Actual output should match");
        }
        catch (IOException ex) {
            fail(ex);
        }
        finally {
            System.setOut(savedOut);
            System.setErr(savedErr);
        }
    }
}

