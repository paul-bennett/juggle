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
import com.angellane.juggle.source.FileSource;
import com.angellane.juggle.source.Module;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Command( name="juggle"
        , description="A declarative search tool for Java"
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
                    "Java Runtime %d.%d".formatted(Runtime.version().feature(),
						   Runtime.version().interim())
            };
        }
    }

    public Juggler juggler = new Juggler();


    // Command-line options

    private Stream<String> paths(String arg) {
        return Arrays.stream(arg.split(File.pathSeparator))
                .filter(Predicate.not(String::isEmpty));
    }

    @SuppressWarnings("unused")
    @Option(names={"-i", "--import"}, paramLabel="packageName", description="Imported package names")
    public void addImport(String importName) { juggler.addImportedPackageName(importName); }

    @SuppressWarnings("unused")
    @Option(names={"-cp", "--classpath", "--class-path"}, paramLabel="path", description="JAR file or directory to include in search")
    public void addToClassPath(String arg) {
        paths(arg).forEach(p -> juggler.addSource(new FileSource(p)));
    }

    @SuppressWarnings("unused")
    @Option(names={"-p", "--module-path"}, paramLabel="modulePath", description="Where to look for modules")
    public void addModulePath(String arg) {
        paths(arg).forEach(juggler::addModulePath);
    }

    @SuppressWarnings("unused")
    @Option(names={"-m", "--module", "--add-modules"}, paramLabel="moduleName", description="Modules to search")
    public void addModule(String arg) {
        Arrays.stream(arg.split(","))
                .filter(s -> !s.isEmpty())
                .forEach(m -> juggler.addSource(
                        new Module(juggler.getModulePaths(), m)));
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
            paramLabel="access|hierarchy|name|package|score|text",
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
                juggler.info("QUERY: %s".formatted(query));
        }
    }

    @Override
    public void run() {
        // Formatting

        Formatter f = formatterOption.getFormatter();
        juggler.setFormatter(f);

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

            juggler.setSink(new TextOutput(juggler.getImportedPackageNames(), System.out, f));

            // Go!

            juggler.doJuggle();
        }
    }

    public static void main(String[] args) {
        Main m = new Main();
        try {
            new CommandLine(m)
                    .setCaseInsensitiveEnumValuesAllowed(true)
                    .setOverwrittenOptionsAllowed(true)
                    .execute(args);
        }
        catch (JuggleError ex) {
            m.juggler.error(ex.getLocalizedMessage());
        }
    }
}
