package com.angellane.juggle.processor;

import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.util.PermutationGenerator;

import java.util.function.Function;
import java.util.stream.Stream;

public class PermuteParams implements
        Function<MemberCandidate, Stream<MemberCandidate>> {
    @Override
    public Stream<MemberCandidate> apply(MemberCandidate candidate) {
        return (new PermutationGenerator<>(candidate.paramTypes())).stream()
                .distinct()
                .map(ps -> new MemberCandidate(candidate, ps)
                );
    }
}
