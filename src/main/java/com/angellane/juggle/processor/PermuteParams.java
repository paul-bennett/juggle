package com.angellane.juggle.processor;

import com.angellane.juggle.CandidateMember;
import com.angellane.juggle.PermutationGenerator;

import java.util.stream.Stream;

public class PermuteParams implements Processor {
    @Override
    public Stream<CandidateMember> processCandidate(CandidateMember m) {
        return (new PermutationGenerator<>(m.paramTypes())).stream()
                .distinct()
                .map(ps -> new CandidateMember(m, ps));
    }
}
