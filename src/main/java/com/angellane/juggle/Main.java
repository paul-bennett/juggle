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

import com.angellane.juggle.formatter.AnsiColourFormatter;
import com.angellane.juggle.formatter.Formatter;
import com.angellane.juggle.formatter.PlaintextFormatter;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.processor.PermuteParams;
import com.angellane.juggle.query.MemberQuery;
import com.angellane.juggle.query.Query;
import com.angellane.juggle.query.QueryFactory;
import com.angellane.juggle.query.TypeQuery;
import com.angellane.juggle.sink.TextOutput;
import com.angellane.juggle.source.JarFile;
import com.angellane.juggle.source.Module;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Command( name="juggle"
        , description="An API search tool for Java"
        , mixinStandardHelpOptions=true
        , versionProvider=Main.Version.class
        )
public class Main implements Runnable {
    public static class Version implements IVersionProvider {
        @Override
        public String[] getVersion() {
            Package pkg = getClass().getPackage();
            String[] pkgComps = pkg.getName().split("\\.");

            return new String[] {
                    Optional.ofNullable(
                            getClass().getPackage().getImplementationVersion()
                    )
                            .orElse(pkgComps[pkgComps.length-1])
                    + " " +
                    Optional.ofNullable(
                            getClass().getPackage().getImplementationVersion()
                    )
                            .orElse("(unreleased version)")
                    ,
                    "Java Runtime " + Runtime.version().toString()
            };
        }
    }

    public Juggler juggler = new Juggler();


    // Command-line options

    @SuppressWarnings("unused")
    @Option(names={"-i", "--import"}, paramLabel="packageName", description="Imported package names")
    public void addImport(String importName) { juggler.addImportedPackageName(importName); }

    @SuppressWarnings("unused")
    @Option(names={"-j", "--jar"}, paramLabel="jarFilePath", description="JAR file to include in search")
    public void addJar(String jarName) { juggler.addSource(new JarFile(jarName)); }


    @SuppressWarnings("unused")
    @Option(names={"-m", "--module"}, paramLabel="moduleName", description="Modules to search")
    public void addModule(String arg) {
        Arrays.stream(arg.split(","))
                .filter(s -> !s.isEmpty())
                .forEach(m -> juggler.addSource(new Module(m)));
    }

    @SuppressWarnings("unused")
    @Option(names={"-c", "--conversions"},
            paramLabel="none|all|auto",
            description="Which conversions to apply")
    public void setConversions(Juggler.Conversions conversions) {
        juggler.setConversions(conversions);
    }

    @SuppressWarnings("unused")
    @Option(names={"-s", "--sort"},
            paramLabel="access|name|package|score|text",
            description="Sort criteria")
    public void addSortCriteria(SortCriteria criteria) {
        juggler.addSortCriteria(criteria);
    }

    @SuppressWarnings("unused")
    @Option(names={"-x", "--permute"}, negatable=true, description="Also match permutations of parameters")
    public void addPermutationProcessor(boolean permute) {
        if (permute)
            juggler.prependMemberCandidateProcessor(new PermuteParams());
    }

    @Option(names={"-f", "--format"}, paramLabel="auto|plain|colour|color",
            description="Output format")
    public FormatterOption formatterOption = FormatterOption.AUTO;

    public enum FormatterOption {
        PLAIN(new PlaintextFormatter()),
        COLOUR(new AnsiColourFormatter()),
        COLOR(COLOUR),
        AUTO(System.console() == null ? PLAIN : COLOUR);

        private final Formatter f;
        FormatterOption(Formatter f) { this.f = f; }
        FormatterOption(FormatterOption other) { this.f = other.f; }

        Formatter getFormatter() {
            return f;
        }
    }

    @Option(names={"--show-query"}, description="Show query")
    public boolean showQuery = false;

    @Option(names={"--dry-run"}, description="Dry run only")
    public boolean dryRun = false;

    @Parameters(paramLabel="declaration", description="A Java-style declaration to match against")
    List<String> queryParams = new ArrayList<>();

    public String getQueryString() {
        return String.join(" ", queryParams);
    }

    // Application logic follows.

    void parseDeclarationQuery(String queryString) {
        if (!queryString.isEmpty()) {
            QueryFactory factory = new QueryFactory(juggler);

            Query<?> query = factory.createQuery(queryString);

            if (query.getAccessibility() == null)
                query.setAccessibility(Accessibility.PUBLIC);

            if (query instanceof MemberQuery mq)
                juggler.setMemberQuery(mq);
            else if (query instanceof TypeQuery tq)
                juggler.setTypeQuery(tq);

            if (showQuery)
                System.err.println("QUERY: " + query);
        }
    }

    @Override
    public void run() {
        // Sources

        juggler.configureAllSources();      // Essential that sources are configured before getting param/return types

        // Processors

        juggler.addMemberFilter(m -> !m.member().getDeclaringClass().isAnonymousClass());       // anon and local classes ...
        juggler.addMemberFilter(m -> !m.member().getDeclaringClass().isLocalClass());           // ... are unutterable anyway

        juggler.addTypeFilter(t -> !t.clazz().isAnonymousClass());
        juggler.addTypeFilter(t -> !t.clazz().isLocalClass());

        // Declaration Query in remaining parameters

        String queryString = getQueryString();

        parseDeclarationQuery(queryString);

        if (!dryRun) {
            // Sinks

            juggler.setSink(new TextOutput(juggler.getImportedPackageNames(), System.out, formatterOption.getFormatter()));

            // Go!

            juggler.doJuggle();
        }
    }

    public static void main(String[] args) {
        try {
            new CommandLine(new Main())
                    .setCaseInsensitiveEnumValuesAllowed(true)
                    .setOverwrittenOptionsAllowed(true)
                    .execute(args);
        }
        catch (JuggleError ex) {
            System.err.println("*** Error: " + ex.getMessage());
        }
    }
}
