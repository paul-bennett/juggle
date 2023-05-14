package com.angellane.juggle;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.comparator.ByAccessibility;
import com.angellane.juggle.comparator.ByCanonicalName;
import com.angellane.juggle.comparator.ByPackage;
import com.angellane.juggle.comparator.ByScore;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.Query;

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
    SCORE   (j -> new ByScore()),
    ACCESS  (j -> new ByAccessibility()),
    PACKAGE (j -> new ByPackage(j.getImportedPackageNames())),
    NAME    (j -> new ByCanonicalName());

    private final Function<Juggler, Comparator<Match<Candidate, Query>>> comparatorGenerator;

    SortCriteria(Function<Juggler, Comparator<Match<Candidate, Query>>> comparatorGenerator) {
        this.comparatorGenerator = comparatorGenerator;
    }

    Comparator<Match<Candidate, Query>> getComparator(Juggler j) {
        return comparatorGenerator.apply(j);
    }
}
