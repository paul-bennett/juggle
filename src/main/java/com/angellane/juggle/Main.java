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

import java.lang.module.FindException;
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
            return new String[] {
                    Optional.ofNullable(
                            getClass().getPackage().getImplementationVersion()
                    )
                            .orElse("Unknown")
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
    @Option(names={"-s", "--sort"}, description="Sort criteria")
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

        try {
            juggler.configureAllSources();      // Essential that sources are configured before getting param/return types
        }
        catch (FindException ex) {
            System.err.println("*** " + ex.getLocalizedMessage());
            return;
        }

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
        new CommandLine(new Main())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setOverwrittenOptionsAllowed(true)
                .execute(args);
    }
}
