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
package com.angellane.juggle.match;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.query.Query;

import java.util.Objects;


/**
 * This record combines a candidate and a query, along with a score
 * indicating how close the two match.  The lower the score, the closer
 * the match.
 *
 * @param candidate
 * @param query
 * @param score How close does #candidate match #query? Lower is better.
 * @param <C> The type of the candidate
 * @param <Q> The type of the query
 */
public class Match<C extends Candidate, Q extends Query<C>> {
    private final C candidate;
    private final Q query;
    private final int score;

    public Match(
            C candidate,
            Q query,
            int score) {
        this.candidate = candidate;
        this.query = query;
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match<?, ?> match = (Match<?, ?>) o;
        return score == match.score && Objects.equals(candidate, match.candidate) && Objects.equals(query, match.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidate, query, score);
    }

    public C candidate()    { return candidate; }
    public Q query()        { return query; }
    public int score()      { return score; }
}

