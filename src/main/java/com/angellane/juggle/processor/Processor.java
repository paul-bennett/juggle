package com.angellane.juggle.processor;

import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.query.MemberQuery;
import com.angellane.juggle.match.Match;

import java.util.function.Function;
import java.util.stream.Stream;

public interface Processor
    extends Function<Match<MemberCandidate, MemberQuery>,
                     Stream<Match<MemberCandidate, MemberQuery>>
        > {
    @Override
    default Stream<Match<MemberCandidate, MemberQuery>>
    apply(Match<MemberCandidate, MemberQuery> candidateMatch) {
        return processMatch(candidateMatch);
    }

    Stream<Match<MemberCandidate, MemberQuery>> processMatch(Match<MemberCandidate, MemberQuery> match);
}
