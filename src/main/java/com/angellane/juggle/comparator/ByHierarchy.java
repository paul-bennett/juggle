/*
 *  Juggle -- an API search tool for Java
 *
 *  Copyright 2020,2023 Paul Bennett
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
package com.angellane.juggle.comparator;

import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.TypeQuery;

import java.util.Comparator;

/**
 * Compares two Matches based on their Candidates' simple name.
 */
public class ByHierarchy
        implements Comparator<Match<TypeCandidate, TypeQuery>> {
    @Override
    public int compare(Match<TypeCandidate, TypeQuery> m1,
                       Match<TypeCandidate, TypeQuery> m2) {

        Class<?> c1 = m1.candidate().clazz();
        Class<?> c2 = m2.candidate().clazz();

        boolean c1Assignable = c1.isAssignableFrom(c2);
        boolean c2Assignable = c2.isAssignableFrom(c1);

        return c1Assignable == c2Assignable
                ? 0
                : c1Assignable ? -1 : +1;
    }
}
