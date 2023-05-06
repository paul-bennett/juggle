package com.angellane.juggle.processor;

import com.angellane.juggle.candidate.CandidateMember;

import java.util.function.Function;
import java.util.stream.Stream;

public interface Processor extends Function<CandidateMember, Stream<CandidateMember>> {
    @Override
    default Stream<CandidateMember> apply(CandidateMember candidateMember) {
        return processCandidate(candidateMember);
    }

    Stream<CandidateMember> processCandidate(CandidateMember candidate);
}
