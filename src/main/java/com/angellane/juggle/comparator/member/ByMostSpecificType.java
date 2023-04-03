package com.angellane.juggle.comparator.member;

import com.angellane.juggle.CandidateMember;
import com.angellane.juggle.CartesianProduct;
import com.angellane.juggle.PermutationGenerator;
import com.angellane.juggle.TypeSignature;
import com.angellane.juggle.comparator.TypeComparator;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Compares two class Members based on their type specificity.  More specific types before less specific ones.
 */
public class ByMostSpecificType implements Comparator<CandidateMember> {
    private final TypeComparator typeComparator = new TypeComparator();

    @Override
    public int compare(CandidateMember m1, CandidateMember m2) {
        // Which member is more specific (and therefore should be sorted first), m1 or m2?

        // Strategy is to convert the member into a list of type signatures.  (For Methods and Constructors such a
        // list will be a singleton, but for a Field there will be two signatures: one for a getter and one for a
        // setter.)
        //
        // Then, compare each permutation of parameters for each m1 signature with each m2 signature.  (We don't
        // permute m2's args because doing so would just add redundant work.)
        //
        // For each permuted m1 signature, derive a score that marks the signature by specificity.  The score is
        // the sum of specificity scores for each parameter: 0 means the parameters can't be assigned to each other
        // (i.e. as mismatch); -1 says that m1's parameter is more specific than m2's; +1 says m2's parameter is
        // more specific than m1's.
        //
        // After examining all permutations, find the score with the highest absolute value.  Return the sign of
        // the highest score.  So if we had scores of 0, -5, 4 and -2, we're return -1 because -5 has the largest
        // absolute value so it wins, and it's a negative value so the return value is -1.
        //
        // Annotations, and exceptions thrown by the candidate members are ignored in this process

        int winningScore = CartesianProduct.of(TypeSignature.of(m1.getMember()), TypeSignature.of(m2.getMember())).stream()
                .peek(ts -> { assert(ts.size() == 2); })                        // Sanity check: elements are pairs

                .filter(ts -> ts.get(0).paramTypes.size() == ts.get(1).paramTypes.size())   // Param list len must ==

                .flatMap(ts -> {                                                // Permute params of the 1st type sig
                    TypeSignature ts1 = ts.get(0), ts2 = ts.get(1);

                    return ts1.paramTypes.stream()
                                .collect(PermutationGenerator.collector())
                                .stream()
                                .map(params1 -> List.of(new TypeSignature(params1, ts1.returnType, Set.of(), Set.of()), ts2));
                })

                .mapToInt(this::computeScore)                                   // Give the pair a score

                .reduce(0, (a,b) -> Math.abs(a) > Math.abs(b) ? a : b); // Find the max absolute score

        return Integer.compare(winningScore, 0);                                // return the sign
    }

    private int computeScore(List<TypeSignature> ts) {
        assert(ts.size() == 2);

        TypeSignature ts1 = ts.get(0), ts2 = ts.get(1);

        assert(ts1.paramTypes.size() == ts2.paramTypes.size());

        return IntStream.range(0, ts1.paramTypes.size())
                    .map(i -> typeComparator.compare(ts1.paramTypes.get(i), ts2.paramTypes.get(i)))
                    .sum()
                + typeComparator.compare(ts1.returnType, ts2.returnType);
    }}
