package com.angellane.juggle.comparator;

import com.angellane.juggle.CartesianProduct;
import com.angellane.juggle.PermutationGenerator;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Compares two class Members based on their package's position in a list.
 * Members that aren't in a package on the list are sorted last.
 */
public class ByClosestType implements Comparator<Member> {
    private final TypeComparator typeComparator = new TypeComparator();

    static class TypeSignature {
        public final List<Class<?>> paramTypes;
        public final Class<?> returnType;

        public TypeSignature(List<Class<?>> paramTypes, Class<?> returnType) {
            this.paramTypes = paramTypes;
            this.returnType = returnType;
        }

        public static List<TypeSignature> signaturesOf(Member m) {
            List<Class<?>> implicitParams = new ArrayList<>();

            // Handle the 'this' pointer for non-static members.
            // Note that Constructors appear to be non-static but they don't have a silent 'this'.
            if (!Modifier.isStatic(m.getModifiers()) && !(m instanceof Constructor<?>))
                implicitParams.add(m.getDeclaringClass());

            if (m instanceof Constructor<?>) {
                Constructor<?> c = (Constructor<?>)m;

                return List.of(new TypeSignature(
                        List.of(c.getParameterTypes()),
                        c.getDeclaringClass()
                ));
            }
            if (m instanceof Method) {
                Method e = (Method)m;

                return List.of(new TypeSignature(
                        Stream.of(implicitParams.stream(), Arrays.stream(e.getParameterTypes()))
                                .flatMap(Function.identity())
                                .collect(Collectors.toList()),
                        e.getReturnType()
                ));
            }
            else if (m instanceof Field) {
                Field f = (Field)m;

                return List.of(
                        new TypeSignature(implicitParams, f.getType()),                             // Getter
                        new TypeSignature(                                                          // Setter
                                Stream.concat(implicitParams.stream(), Stream.of(f.getType()))
                                        .collect(Collectors.toList()),
                                Void.TYPE
                        )
                );
            }
            else
                return List.of();
        }
    }

    @Override
    public int compare(Member m1, Member m2) {
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

        List<TypeSignature> m1Sigs = TypeSignature.signaturesOf(m1);
        List<TypeSignature> m2Sigs = TypeSignature.signaturesOf(m2);

        int winningScore = CartesianProduct.of(m1Sigs, m2Sigs).stream()
                .peek(ts -> { assert(ts.size() == 2); })                        // Sanity check: elements are pairs

                .filter(ts -> ts.get(0).paramTypes.size() == ts.get(1).paramTypes.size())   // Param list len must ==

                .flatMap(ts -> {                                                // Permute params of the 1st type sig
                    TypeSignature ts1 = ts.get(0), ts2 = ts.get(1);

                    return ts1.paramTypes.stream()
                                .collect(PermutationGenerator.collector())
                                .stream()
                                .map(params1 -> List.of(new TypeSignature(params1, ts1.returnType), ts2));
                })
                .mapToInt(ts -> computeScore(ts.get(0), ts.get(1)))             // Give the pair a score
                .reduce(0, (a,b) -> Math.abs(a) > Math.abs(b) ? a : b); // Find the max absolute score

        return Integer.compare(winningScore, 0);                                // return the sign
    }

    private int computeScore(TypeSignature ts1, TypeSignature ts2) {
        assert(ts1.paramTypes.size() == ts2.paramTypes.size());

        return IntStream.range(0, ts1.paramTypes.size())
                    .map(i -> typeComparator.compare(ts1.paramTypes.get(i), ts2.paramTypes.get(i)))
                    .sum()
                + typeComparator.compare(ts1.returnType, ts2.returnType);
    }}
