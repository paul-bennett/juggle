package com.angellane.juggle.match;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.query.Query;


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
public record Match<C extends Candidate, Q extends Query<C>>(
        C candidate,
        Q query,
        int score) {
}

