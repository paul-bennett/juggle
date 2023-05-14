package com.angellane.juggle.comparator;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.Query;

import java.util.Comparator;

public class ByAccessibility implements Comparator<Match<Candidate, Query>> {
    @Override
    public int compare(Match<Candidate, Query> o1, Match<Candidate, Query> o2) {
        return Math.negateExact(o1.candidate().accessibility()
                .compareTo(o2.candidate().accessibility()));
    }
}

