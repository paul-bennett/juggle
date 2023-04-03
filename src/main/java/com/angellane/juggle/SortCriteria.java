package com.angellane.juggle;

import com.angellane.juggle.comparator.member.*;

import java.util.Comparator;
import java.util.Set;
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
    ACCESS  (j -> new ByAccessibility()),
    TYPE    (j -> new ByMostSpecificType()),
    CLOSEST (j -> new ByClosestType(
            new TypeSignature(j.getParamTypes(), j.getReturnType(),
                    Set.of(), Set.of()))),                  // This comparator doesn't inspect Throws or annotations
    PACKAGE (j -> new ByPackage(j.getImportedPackageNames())),
    NAME    (j -> new ByCanonicalName());

    private final Function<Juggler, Comparator<CandidateMember>> comparatorGenerator;

    SortCriteria(Function<Juggler, Comparator<CandidateMember>> comparatorGenerator) {
        this.comparatorGenerator = comparatorGenerator;
    }

    Comparator<CandidateMember> getComparator(Juggler j) {
        return comparatorGenerator.apply(j);
    }
}
