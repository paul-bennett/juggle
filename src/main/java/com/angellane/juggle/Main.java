package com.angellane.juggle;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.formatter.AnsiColourFormatter;
import com.angellane.juggle.formatter.Formatter;
import com.angellane.juggle.formatter.PlaintextFormatter;
import com.angellane.juggle.match.Accessibility;
import com.angellane.juggle.query.MemberQuery;
import com.angellane.juggle.processor.PermuteParams;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
    @Option(names={"-p", "--param"}, paramLabel="type,type,...", description="Parameter type of searched function")
    public void addParamTypes(String paramTypeName) {
        if (paramTypeNames == null) paramTypeNames = new ArrayList<>();

        paramTypeNames.addAll(Arrays.stream(paramTypeName.split(","))
                .filter(s -> !s.isEmpty())
                .toList()
        );
    }

    List<String> paramTypeNames = null;     // null = don't match params; empty list = zero params


    public List<Class<?>> getParamTypes() {
        if (paramTypeNames == null)
            return null;
        else {
            // Do this in two steps because to properly capture the type wildcard
            Stream<Class<?>> str = paramTypeNames.stream().map(juggler::classForTypename);
            return str.toList();
        }
    }


    public Class<?> getReturnType() {
        return returnTypeName == null ? null : juggler.classForTypename(returnTypeName);
    }

    @Option(names={"-r", "--return"}, paramLabel="type", description="Return type of searched function")
    String returnTypeName;


    @SuppressWarnings("unused")
    @Option(names={"-t", "--throws"}, paramLabel="type,type,...", description="Thrown types")
    public void addThrowTypes(String throwTypeName) {
        if (throwTypeNames == null) throwTypeNames = new HashSet<>();

        throwTypeNames.addAll(Arrays.stream(throwTypeName.split(","))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet())
        );
    }
    Set<String>  throwTypeNames = null;     // null = don't care; empty set = throws nothing

    public Set<Class<?>> getThrowTypes() {
        return throwTypeNames == null ? null : throwTypeNames.stream()
                .map(juggler::classForTypename)
                .collect(Collectors.toSet());
    }


    @SuppressWarnings("unused")
    @Option(names={"-@", "--annotation"}, paramLabel="type,type,...", description="Annotations")
    public void addAnnotationTypes(String annotationNames) {
        annotationTypeNames.addAll(Arrays.stream(annotationNames.split(","))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet())
        );
    }
    List<String>  annotationTypeNames = new ArrayList<>();

    public Set<Class<?>> getAnnotationTypes() {
        return annotationTypeNames.stream()
                .map(juggler::classForTypename)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unused")
    @Option(names={"-n", "--member-name"}, paramLabel="methodName", description="Filter by member name")
    public void addMemberNameFilter(String name) {
        juggler.prependFilter(m -> m.member().getName().toLowerCase().contains(name.toLowerCase()));
    }


    @Option(names={"-a", "--access"}, paramLabel="private|protected|package|public",
            description="Minimum accessibility of members to return")
    Accessibility minAccess = Accessibility.PUBLIC;

    @SuppressWarnings("unused")
    @Option(names={"-s", "--sort"}, description="Sort criteria")
    public void addSortCriteria(SortCriteria criteria) {
        juggler.addSortCriteria(criteria);
    }

    @SuppressWarnings("unused")
    @Option(names={"-x", "--permute"}, negatable=true, description="Also match permutations of parameters")
    public void addPermutationProcessor(boolean permute) {
        if (permute)
            juggler.prependProcessor(new PermuteParams());
    }

    @Option(names={"-f", "--format"}, description="Output format")
    public FormatterOption formatterOption = FormatterOption.PLAIN;

    public enum FormatterOption {
        PLAIN(new PlaintextFormatter()),
        COLOUR(new AnsiColourFormatter());

        private final Formatter f;
        FormatterOption(Formatter f) { this.f = f; }

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

        juggler.appendFilter(m -> !m.member().getDeclaringClass().isAnonymousClass());       // anon and local classes ...
        juggler.appendFilter(m -> !m.member().getDeclaringClass().isLocalClass());           // ... are unutterable anyway

        juggler.prependFilter(m -> m.annotationTypes().containsAll(getAnnotationTypes()));
        if (getThrowTypes() != null) juggler.prependFilter(m -> {
            Set<Class<?>> throwTypes = getThrowTypes();

            if (throwTypes.size() == 0)
                return m.throwTypes().size() == 0;

            // A candidate's throws clause matches if the types it might throw are listed
            // in the query's set of caught exceptions
            for (var caughtType : throwTypes) {
                if (m.throwTypes().stream().noneMatch(thrownType -> Candidate.isTypeCompatibleForAssignment(caughtType, thrownType)))
                    return false;
            }
            return true;
        });
        if (getReturnType() != null) juggler.prependFilter(m ->
                Candidate.isTypeCompatibleForAssignment(getReturnType(), m.returnType()));
        if (getParamTypes() != null) juggler.appendFilter(m -> {
                    List<? extends Class<?>> paramTypes = getParamTypes();
                    Iterator<? extends Class<?>> queryTypeIter = paramTypes.iterator();
                    return m.paramTypes().stream().allMatch(mpt ->
                            Candidate.isTypeCompatibleForInvocation(mpt, queryTypeIter.next()));
                });
        juggler.prependFilter(m -> Accessibility.fromModifiers(
                m.member().getModifiers()).isAtLeastAsAccessibleAsOther(minAccess));

        if (getParamTypes() != null)
            // Optimisation: filter out anything that hasn't got the right number of params
            // Useful because permutation of every candidate member's params takes forever.
            juggler.prependFilter(m -> m.paramTypes().size() == getParamTypes().size());

        // Parameter String

        String queryString = getQueryString();
        if (!queryString.isEmpty()) {
            QueryFactory factory = new QueryFactory(juggler);

            Query query = factory.createQuery(queryString);

            if (query instanceof MemberQuery mq)
                juggler.appendFilter(mq::isMatchForCandidate);
            else
                juggler.setTypeQuery((TypeQuery)query);

            if (showQuery)
                System.err.println("QUERY: " + query);

            // Also disable the accessibility flag set by the `-a` option
            // since that defaults to the restrictive PUBLIC value.
            //
            minAccess = Accessibility.PRIVATE;
        }


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
