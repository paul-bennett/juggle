package com.angellane.juggle.processor;

import com.angellane.juggle.candidate.CandidateMember;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.MemberQuery;
import com.angellane.juggle.util.PermutationGenerator;

import java.util.stream.Stream;

public class PermuteParams implements Processor {
    @Override
    public Stream<Match<CandidateMember, MemberQuery>> processMatch(
            Match<CandidateMember, MemberQuery> match) {
        return (new PermutationGenerator<>(match.candidate().paramTypes())).stream()
                .distinct()
                .map(ps -> new Match<>(
                        new CandidateMember(match.candidate(), ps),
                        match.query(),
                        match.score()
                        )
                );
    }
}
