/*
 *  Juggle -- a declarative search tool for Java
 *
 *  Copyright 2020,2024 Paul Bennett
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.angellane.juggle;

import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.comparator.*;
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

public enum SortCriteria {
    SCORE   (j -> new ByScore<>(),              j -> new ByScore<>()),
    HIERARCHY (j -> new ByHierarchy(),          j -> (cm1,cm2) -> 0),
    ACCESS  (j -> new ByAccessibility<>(),      j -> new ByAccessibility<>()),
    PACKAGE (j -> new ByPackage<>(j.getImportedPackageNames()),
             j -> new ByPackage<>(j.getImportedPackageNames())),
    TEXT    (j -> new ByString<>(),             j -> new ByString<>()),
    NAME    (j -> new BySimpleName<>(),         j -> new BySimpleName<>());

    private final Function<
            Juggler,
            Comparator<Match<TypeCandidate, Query<TypeCandidate>>>
            > typeComparatorGenerator;
    private final Function<
            Juggler,
            Comparator<Match<MemberCandidate, Query<MemberCandidate>>>
            > memberComparatorGenerator;

    SortCriteria(
            Function<Juggler, Comparator<Match<TypeCandidate, Query<TypeCandidate>>>>
                    typeComparatorGenerator,
            Function<Juggler, Comparator<Match<MemberCandidate, Query<MemberCandidate>>>>
                    memberComparatorGenerator
    ) {
        this.typeComparatorGenerator   = typeComparatorGenerator;
        this.memberComparatorGenerator = memberComparatorGenerator;
    }

    Comparator<Match<TypeCandidate,Query<TypeCandidate>>>
    getTypeComparator(Juggler j) {
        return typeComparatorGenerator.apply(j);
    }
    Comparator<Match<MemberCandidate,Query<MemberCandidate>>>
    getMemberComparator(Juggler j) {
        return memberComparatorGenerator.apply(j);
    }
}
