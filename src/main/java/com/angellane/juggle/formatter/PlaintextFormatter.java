package com.angellane.juggle.formatter;

public class PlaintextFormatter implements Formatter {
    @Override public String formatKeyword       (String s) { return s; }
    @Override public String formatPackageName   (String s) { return s; }
    @Override public String formatClassName     (String s) { return s; }
    @Override public String formatMethodName    (String s) { return s; }
    @Override public String formatType          (String s) { return s; }
}
