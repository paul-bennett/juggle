package com.angellane.juggle.processor;

import com.angellane.juggle.candidate.CandidateMember;
import com.angellane.juggle.query.MemberQuery;
import com.angellane.juggle.match.Match;

import java.util.function.Function;
import java.util.stream.Stream;

public interface Processor
    extends Function<Match<CandidateMember, MemberQuery>,
                     Stream<Match<CandidateMember, MemberQuery>>
        > {
    @Override
    default Stream<Match<CandidateMember, MemberQuery>>
    apply(Match<CandidateMember, MemberQuery> candidateMatch) {
        return processMatch(candidateMatch);
    }

    Stream<Match<CandidateMember, MemberQuery>> processMatch(Match<CandidateMember, MemberQuery> match);
}
