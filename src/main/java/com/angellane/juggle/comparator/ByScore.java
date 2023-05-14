package com.angellane.juggle.comparator;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.Query;

import java.util.Comparator;

public class ByScore implements Comparator<Match<Candidate, Query>> {
    @Override
    public int compare(Match<Candidate, Query> o1, Match<Candidate, Query> o2) {
        return Integer.compare(o1.score(), o2.score());
    }
}
