package com.angellane.juggle;

import com.angellane.juggle.processor.PermuteParams;
import com.angellane.juggle.sink.TextOutput;
import com.angellane.juggle.source.JarFile;
import com.angellane.juggle.source.Module;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.ExplicitBooleanOptionHandler;

import java.lang.module.FindException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public Juggler juggler = new Juggler();

    // Command-line options

    @Option(name="-i", aliases={"--import"}, usage="Imported package names", metaVar="packageName")
    public void addImport(String importName) { juggler.addImportedPackageName(importName); }


    @Option(name="-j", aliases="--jar", usage="JAR file to include in search", metaVar="jarFilePath")
    public void addJar(String jarName) { juggler.addSource(new JarFile(jarName)); }


    @Option(name="-m", aliases="--module", usage="Modules to search", metaVar="moduleName")
    public void addModule(String arg) {
        Arrays.stream(arg.split(","))
                .filter(s -> !s.isEmpty())
                .forEach(m -> juggler.addSource(new Module(m)));
    }


    @Option(name="-p", aliases="--param", usage="Parameter type of searched function", metaVar="type,type,...")
    public void addParamTypes(String paramTypeName) {
        if (paramTypeNames == null) paramTypeNames = new ArrayList<>();

        paramTypeNames.addAll(Arrays.stream(paramTypeName.split(","))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
        );
    }

    List<String> paramTypeNames = null;     // null = don't match params; empty list = zero params


    public List<Class<?>> getParamTypes() {
        return paramTypeNames == null ? null : paramTypeNames.stream()
                .map(juggler::classForTypename)
                .collect(Collectors.toList());
    }


    public Class<?> getReturnType() {
        return returnTypeName == null ? null : juggler.classForTypename(returnTypeName);
    }

    @Option(name="-r", aliases="--return", usage="Return type of searched function", metaVar="type")
    String returnTypeName;


    @Option(name="-t", aliases="--throws", usage="Thrown types", metaVar="type,type,...")
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


    @Option(name="-@", aliases="--annotation", usage="Annotations", metaVar="type,type,...")
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

    @Option(name="-n", aliases="--name", usage="Filter by member name", metaVar="methodName")
    public void addNameFilter(String name) {
        juggler.prependFilter(m -> m.getMember().getName().toLowerCase().contains(name.toLowerCase()));
    }


    @Option(name="-a", aliases="--access", usage="Minimum accessibility of members to return",
            metaVar="private|protected|package|public")
    Accessibility minAccess = Accessibility.PUBLIC;

    @Option(name="-s", aliases="--sort", usage="Sort criteria")
    public void addSortCriteria(SortCriteria criteria) {
        juggler.addSortCriteria(criteria);
    }

    @Option(name="-x", aliases="--permute", usage="Also match permutations of parameters",
            handler=ExplicitBooleanOptionHandler.class, metaVar="[TRUE | FALSE]")
    public void addPermutationProcessor(boolean permute) {
        if (permute)
            juggler.prependProcessor(new PermuteParams());
    }

    @Option(name="-h", aliases="--help", help=true)
    boolean helpRequested;


    // Application logic follows.

    public boolean parseArgs(String[] args) {
        final CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);

            if (helpRequested) {
                parser.printUsage(System.out);
                return false;
            }

        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printSingleLineUsage(System.out);
            System.out.println();
            return false;
        }

        return true;
    }

    public void goJuggle() {
        // Sources

        try {
            juggler.configureAllSources();      // Essential that sources are configured before getting param/return types
        }
        catch (FindException ex) {
            System.err.println("*** " + ex.getLocalizedMessage());
            return;
        }

        // Processors

        juggler.appendFilter(m -> !m.getMember().getDeclaringClass().isAnonymousClass());       // anon and local classes ...
        juggler.appendFilter(m -> !m.getMember().getDeclaringClass().isLocalClass());           // ... are unutterable anyway

        juggler.prependFilter(m -> m.matchesAnnotations(getAnnotationTypes()));
        if (getThrowTypes() != null) juggler.prependFilter(m -> m.matchesThrows(getThrowTypes()));
        if (getReturnType() != null) juggler.prependFilter(m -> m.matchesReturn(getReturnType()));
        if (getParamTypes() != null) juggler.appendFilter(m -> m.matchesParams(getParamTypes()));

        juggler.prependFilter(m -> Accessibility.fromModifiers(
                m.getMember().getModifiers()).isAtLastAsAccessibleAsOther(minAccess));

        if (getParamTypes() != null)
            // Optimisation: filter out anything that hasn't got the right number of params
            // Useful because permutation of every candidate member's params takes forever.
            juggler.prependFilter(m -> m.getParamTypes().size() == getParamTypes().size());

        // These assist the CLOSEST sort.
        juggler.setParamTypes(getParamTypes());
        juggler.setReturnType(getReturnType());

        // Sinks

        juggler.setSink(new TextOutput(juggler.getImportedPackageNames(), System.out));

        // Go!

        juggler.goJuggle();
    }

    public static void main(String[] args) {
        Main app = new Main();
        if (app.parseArgs(args))
            app.goJuggle();
    }
}
