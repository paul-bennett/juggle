package com.angellane.juggle;

import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final String OPT_ACC = "access";
        final String OPT_IMP = "import";
        final String OPT_JAR = "jar";
        final String OPT_MOD = "module";
        final String OPT_PRM = "param";
        final String OPT_RET = "return";

        Options opts = new Options();

        opts.addOption("a", OPT_ACC, true, "Minimum accessibility of members to return");
        opts.addOption("i", OPT_IMP, true, "Imported packages");
        opts.addOption("j", OPT_JAR, true, "JAR file to include in search");
        opts.addOption("m", OPT_MOD, true, "Module to include in search");
        opts.addOption("p", OPT_PRM, true, "Parameter type of searched function");
        opts.addOption("r", OPT_RET, true, "Return type of searched function");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opts, args);

            String[] imports =
                    Stream.concat(
                            Stream.of("java.lang"),
                            Stream.ofNullable(cmd.getOptionValues(OPT_IMP)).flatMap(Arrays::stream)
                    ).toArray(String[]::new);

            Accessibility minAccess = Accessibility.PUBLIC;
            try {
                minAccess = Accessibility.fromString(cmd.getOptionValue(OPT_ACC, minAccess.name().toLowerCase()));
            }
            catch (IllegalArgumentException ex) {
                System.err.println("Unknown accessibility level; defaulting to " + minAccess.name().toLowerCase());
            }

            String[] jarsToSearch = cmd.getOptionValues(OPT_JAR);
            String[] modsToSearch = cmd.getOptionValues(OPT_MOD);

            String[] paramTypes = cmd.getOptionValues(OPT_PRM);
            String   returnType = cmd.getOptionValue(OPT_RET);

            if (paramTypes != null) {
                paramTypes = String.join(",", paramTypes).split(",");
                if ((paramTypes.length == 1) && (paramTypes[0].length() == 0))
                    paramTypes = new String[] {};
            }

            Juggler j = new Juggler(
                    jarsToSearch == null ? List.of() : List.of(jarsToSearch),
                    modsToSearch == null ? List.of() : List.of(modsToSearch)
            );

            MemberDecoder decoder = new MemberDecoder(imports);
            for (var m : j.findMembers(imports, minAccess, paramTypes, returnType))
                System.out.println(decoder.decode(m));

        } catch (ParseException e) {
            System.err.println("Parsing failed" + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("juggle", opts);
            System.exit(-1);
        }
    }
}
