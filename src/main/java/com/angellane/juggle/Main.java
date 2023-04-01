package com.angellane.juggle;

import com.angellane.juggle.comparator.MultiComparator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.lang.reflect.Member;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    // Command-line options

    @Option(name="-i", aliases={"--import"}, usage="Imported package names", metaVar="packageName")
    public void addImport(String importName) { importedPackageNames.add(importName); }
    final List<String> importedPackageNames = new ArrayList<>(List.of("java.lang"));


    @Option(name="-j", aliases="--jar", usage="JAR file to include in search", metaVar="jarFilePath")
    public void addJar(String jarName) { jarPaths.add(jarName); }
    final List<String> jarPaths = new ArrayList<>();


    @Option(name="-m", aliases="--module", usage="Modules to search", metaVar="moduleName")
    public void addModule(String arg) {
        moduleNames.addAll(Arrays.stream(arg.split(","))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList()));
    }

    final List<String> moduleNames = new ArrayList<>();


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


    @Option(name="-a", aliases="--access", usage="Minimum accessibility of members to return",
            metaVar="private|protected|package|public")
    Accessibility minAccess = Accessibility.PUBLIC;

    @Option(name="-s", aliases="--sort", usage="Sort criteria")
    public void addSortCriteria(SortCriteria criteria) {
        if (sortCriteria == null)
            sortCriteria = new ArrayList<>();
        sortCriteria.add(criteria);
    }
    List<SortCriteria> sortCriteria = null;
    public List<SortCriteria> getSortCriteria() {
        // Return default criteria of none were set.
        return sortCriteria != null
                ? sortCriteria
                : List.of(SortCriteria.CLOSEST, SortCriteria.ACCESS, SortCriteria.PACKAGE, SortCriteria.NAME);
    }

    @Option(name="-h", aliases="--help", help=true)
    boolean helpRequested;


    // Application logic follows.

    public Juggler juggler;

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

    public Comparator<Member> getComparator() {
        return MultiComparator.of(getSortCriteria().stream()
                .map(g -> g.getComparator(this))
                .collect(Collectors.toList()));
    }

    public void goJuggle() {
        juggler = new Juggler(jarPaths, moduleNames, importedPackageNames);

        MemberDecoder decoder = new MemberDecoder(importedPackageNames);

        Arrays.stream(juggler.findMembers(minAccess,
                        new TypeSignature(getParamTypes(), getReturnType(), getThrowTypes(), getAnnotationTypes())))
                .sorted(getComparator())
                .forEach(m -> System.out.println(decoder.decode(m)));
    }

    public static void main(String[] args) {
        Main app = new Main();
        if (app.parseArgs(args))
            app.goJuggle();
    }
}
