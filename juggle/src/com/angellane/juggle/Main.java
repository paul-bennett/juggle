package com.angellane.juggle;

import org.apache.commons.cli.*;

import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) {
        final String OPT_JAR = "jar";
        final String OPT_MOD = "module";
        final String OPT_PRM = "param";
        final String OPT_RET = "return";

        Options opts = new Options();

        opts.addOption("j", OPT_JAR, true, "JAR file to include in search");
        opts.addOption("m", OPT_MOD, true, "Module to include in search");
        opts.addOption("p", OPT_PRM, true, "Parameter type of searched function");
        opts.addOption("r", OPT_RET, true, "Return type of searched function");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(opts, args);

            String[] jarsToSearch = cmd.getOptionValues("jar");
            String[] modsToSearch = cmd.getOptionValues("module");

            String[] paramTypes = cmd.getOptionValues("param");
            String   returnType = cmd.getOptionValue("return");

            Juggler j = new Juggler(jarsToSearch, modsToSearch);

            for (Method m : j.findMethods(paramTypes, returnType)) {
                System.out.println(m.toString());
            }
        } catch (ParseException e) {
            System.err.println("Parsing failed" + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("juggle", opts);
            System.exit(-1);
        }
    }
}
