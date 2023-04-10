package com.angellane.juggle.formatter;

public interface Formatter {
    String formatKeyword(String s);
    String formatPackageName(String s);
    String formatClassName(String s);
    String formatMethodName(String s);
    String formatType(String s);
}
