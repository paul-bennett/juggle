package com.angellane.juggle.processor;

import com.angellane.juggle.candidate.CandidateMember;
import com.angellane.juggle.util.PermutationGenerator;

import java.util.stream.Stream;

public class PermuteParams implements Processor {
    @Override
    public Stream<CandidateMember> processCandidate(CandidateMember m) {
        return (new PermutationGenerator<>(m.paramTypes())).stream()
                .distinct()
                .map(ps -> new CandidateMember(m, ps));
    }
}
