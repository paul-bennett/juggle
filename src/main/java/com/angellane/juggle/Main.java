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
    List<String> importedPackageNames = new ArrayList<>(List.of("java.lang"));

    @Option(name="-j", aliases="--jar", usage="JAR file to include in search", metaVar="jarFilePath")
    public void addJar(String jarName) { jarPaths.add(jarName); }
    List<String> jarPaths = new ArrayList<>();

    @Option(name="-m", aliases="--module", usage="Module to include in search", metaVar="moduleName")
    public void addModule(String moduleName) { moduleNames.add(moduleName); }
    List<String> moduleNames = new ArrayList<>();

    @Option(name="-p", aliases="--param", usage="Parameter type of searched function", metaVar="type,type,...")
    public void addParam(String paramTypeName) {
        if (paramTypeNames == null) paramTypeNames = new ArrayList<>();

        paramTypeNames.addAll(Arrays.stream(paramTypeName.split(","))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
        );
    }
    List<String> paramTypeNames = null;     // null = don't match params; empty list = zero params

    @Option(name="-r", aliases="--return", usage="Return type of searched function", metaVar="type")
    String returnTypeName;

    @Option(name="-a", aliases="--access", usage="Minimum accessibility of members to return",
            metaVar="private|protected|package|public")
    Accessibility minAccess = Accessibility.PUBLIC;

    @Option(name="-s", aliases="--sort", usage="Sort criteria")
    public void addSortCriterium(SortCriteria crit) {
        if (sortCriteria == null)
            sortCriteria = new ArrayList<>();
        sortCriteria.add(crit);
    }
    List<SortCriteria> sortCriteria = null;
    public List<SortCriteria> getSortCriteria() {
        // Return default criteria of none were set.
        return sortCriteria != null
                ? sortCriteria
                : List.of(SortCriteria.TYPE, SortCriteria.ACCESS, SortCriteria.PACKAGE, SortCriteria.NAME);
    }

    @Option(name="-h", aliases="--help", help=true)
    boolean helpRequested;


    // Application logic follows.

    public Juggler juggler;

    public List<Class<?>> getParamTypes() {
        return paramTypeNames == null ? null : paramTypeNames.stream()
                .map(juggler::classForTypename)
                .collect(Collectors.toList());
    }

    public Class<?> getReturnType() {
        return returnTypeName == null ? null : juggler.classForTypename(returnTypeName);
    }

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
            System.exit(-1);
        }

        return true;
    }

    public Comparator<Member> getComparator() {
        return MultiComparator.of(getSortCriteria().stream()
                .map(c -> c.getComparator(this))
                .collect(Collectors.toList()));
    }

    public void goJuggle() {
        juggler = new Juggler(jarPaths, moduleNames, importedPackageNames);

        MemberDecoder decoder = new MemberDecoder(importedPackageNames);

        Arrays.stream(juggler.findMembers(minAccess, getParamTypes(), getReturnType()))
                .sorted(getComparator())
                .forEach(m -> System.out.println(decoder.decode(m)));
    }

    public static void main(String[] args) {
        Main app = new Main();
        if (app.parseArgs(args))
            app.goJuggle();
    }
}
