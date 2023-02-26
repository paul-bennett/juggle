package com.angellane.juggle;

import com.angellane.juggle.comparator.member.*;

import java.lang.reflect.Member;
import java.util.Comparator;
import java.util.function.Function;

/**
 * The various different ways of sorting Members.
 * <p>
 * Enumerands are functions that return a Comparator<Member> suitably configured from the passed-in Main object.
 * (Main is useful because it ultimately contains the command-line args.)  Enumerand names are legal values for
 * Juggle's -s option.
 * <p>
 * To add a new sort criteria:
 *   1. Create a new class that implements java.util.Comparator<Member>
 *   2. Add a new enumerand to this enumeration:
 *       - name is the option to enable the criteria on the command-line
 *       - constructor arg is a Function<Main, Comparator<Member>>
 */
enum SortCriteria {
    ACCESS  (m -> new ByAccessibility()),
    TYPE    (m -> new ByMostSpecificType()),
    CLOSEST (m -> new ByClosestType(new TypeSignature(m.getParamTypes(), m.getReturnType(), m.getThrowTypes()))),
    PACKAGE (m -> new ByPackage(m.importedPackageNames)),
    NAME    (m -> new ByCanonicalName());

    private final Function<Main, Comparator<Member>> comparatorGenerator;

    SortCriteria(Function<Main, Comparator<Member>> comparatorGenerator) {
        this.comparatorGenerator = comparatorGenerator;
    }

    Comparator<Member> getComparator(Main m) {
        return comparatorGenerator.apply(m);
    }
}
