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

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.Query;

import java.util.Comparator;

/**
 * Compares two Matches based on their Candidates' accessibility.
 * More accessible candidates are sorted first (i.e. public before private).
 */
public class ByAccessibility<
        C extends Candidate, Q extends Query<C>, M extends Match<C,Q>
        >
        implements Comparator<M> {
    @Override
    public int compare(M m1, M m2) {
        return Math.negateExact(m1.candidate().accessibility()
                .compareTo(m2.candidate().accessibility()));
    }
}

