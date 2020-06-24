package com.angellane.juggle;

import com.angellane.juggle.comparator.MultiComparator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.lang.reflect.Member;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    @Option(name="-i", aliases={"--import"}, usage="Imported package names", metaVar="packageName")
    public void addImport(String importName) { importPackageNames.add(importName); }
    List<String> importPackageNames = new ArrayList<>();

    @Option(name="-j", aliases="--jar", usage="JAR file to include in search", metaVar="jarFilePath")
    public void addJar(String jarName) { jarPaths.add(jarName); }
    List<String> jarPaths = new ArrayList<>();

    @Option(name="-m", aliases="--module", usage="Module to include in search", metaVar="moduleName")
    public void addModule(String moduleName) { moduleNames.add(moduleName); }
    List<String> moduleNames = new ArrayList<>();

    @Option(name="-p", aliases="--param", usage="Parameter type of searched function", metaVar="type,type,...")
    public void addParam(String paramTypeName) {
        if (paramTypes == null) paramTypes = new ArrayList<>();

        paramTypes.addAll(Arrays.stream(paramTypeName.split(","))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList())
        );
    }
    List<String> paramTypes = null;     // null = don't match params; empty list = zero params

    @Option(name="-r", aliases="--return", usage="Return type of searched function", metaVar="type")
    String returnType;

    @Option(name="-a", aliases="--access", usage="Minimum accessibility of members to return",
            metaVar="private|protected|package|public")
    Accessibility minAccess = Accessibility.PUBLIC;

    @Option(name="-s", aliases="--sort", usage="Sort criteria")
    public void addSortCriterium(SortCriteria crit) {
        sortCriteria.add(crit);
    }
    List<SortCriteria> sortCriteria = new ArrayList<>();

    @Option(name="-h", aliases="--help", help=true)
    boolean helpRequested;

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
        return MultiComparator.of(sortCriteria.stream()
                .map(c -> c.getComparator(this))
                .collect(Collectors.toList()));
    }

    public void goJuggle() {
        String[] imports = Stream.concat(Stream.of("java.lang"), importPackageNames.stream()).toArray(String[]::new);

        String[] paramTypes = this.paramTypes == null ? null : this.paramTypes.toArray(String[]::new);

        Juggler j = new Juggler(jarPaths, moduleNames);

        MemberDecoder decoder = new MemberDecoder(imports);
        Arrays.stream(j.findMembers(imports, minAccess, paramTypes, returnType))
                .sorted(getComparator())
                .forEach(m -> System.out.println(decoder.decode(m)));
    }

    public static void main(String[] args) {
        Main app = new Main();
        if (app.parseArgs(args))
            app.goJuggle();
    }
}
