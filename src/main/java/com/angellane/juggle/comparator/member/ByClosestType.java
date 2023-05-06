package com.angellane.juggle.comparator.member;

import com.angellane.juggle.candidate.CandidateMember;
import com.angellane.juggle.TypeSignature;
import com.angellane.juggle.comparator.TypeComparator;

import java.lang.reflect.Member;
import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Compares two class Members based on how close their types match the query types.
 */
public class ByClosestType implements Comparator<CandidateMember> {
    private final TypeComparator typeComparator = new TypeComparator();
    private final TypeSignature query;

    public ByClosestType(TypeSignature query) {
        this.query = query;
    }

    @Override
    public int compare(CandidateMember m1, CandidateMember m2) {
        // Which member has a type signature that is a better match for the query?

        // Uses the same strategy as ByMostSpecificType, but instead of computing a score between m1 and m2 it
        // computes a score between m1 and this.query, and between m2 and this.query.

        int m1score = computeMemberScore(m1.member());
        int m2score = computeMemberScore(m2.member());

        return Integer.compare(Math.abs(m1score), Math.abs(m2score));
    }

    /**
     * Computes the best score for this member, when comparing with this.query.  (Absolute best == 0.)
     *
     * @param m The member to score
     * @return A score value. The sign of the returned value indicates whether the member is more (-ve) or less (+ve)
     * specific than the query.  The magnitude of the returned value is an indication of distance from the query
     * signature -- the higher the value, the further apart. If the member and query have identical types, returns
     * zero.
     */
    private int computeMemberScore(Member m) {
        if (query.paramTypes == null)
            // If there were no parameters in the query, all members score the same
            return 0;
        else
            return TypeSignature.of(m).stream()                                             // Two for fields (get/set)
                    .filter(sig -> sig.paramTypes.size() == query.paramTypes.size())        // Toss out if #params !=
                    .mapToInt(this::computeSignatureScore)                                  // Compute the score
                    .map(Math::abs).min().orElse(Integer.MAX_VALUE);                        // Find the best
    }

    /**
     * Computes a score for the specified type signature when compared against this.query.
     *
     * @param ts The type signature to score.
     * @return A score that represents how far apart ts is from this.query. A score of 0 indicates that they are
     * identical. The magnitude of the score is the distance between the two type signatures; the sign indicates
     * whether ts is more specific (-ve) or less specific (+ve) overall than this.query.
     */
    private int computeSignatureScore(TypeSignature ts) {
        int paramScore =
                IntStream.range(0, ts.paramTypes.size())
                    .map(i -> typeComparator.compare(ts.paramTypes.get(i), query.paramTypes.get(i)))
                    .sum();
        int returnScore = query.returnType == null
                ? 0
                : typeComparator.compare(ts.returnType, query.returnType);

        // Should add throws and annotations scores here

        return paramScore + returnScore;
    }}
