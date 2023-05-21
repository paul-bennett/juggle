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

