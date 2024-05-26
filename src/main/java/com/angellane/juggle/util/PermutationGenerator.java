/*
 *  Juggle -- a declarative search tool for Java
 *
 *  Copyright 2020,2024 Paul Bennett
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.angellane.juggle.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

public class PermutationGenerator<T> {
    private final List<T> contents;

    public PermutationGenerator(Collection<T> coll) {
        contents = new ArrayList<>(coll);
    }

    public static <T> Collector<T, ?, PermutationGenerator<T>> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), PermutationGenerator::new);
    }

    public Stream<List<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    protected Spliterator<List<T>> spliterator() {
        return new PermSpliterator<>(contents);
    }

    public static class PermSpliterator<T> implements Spliterator<List<T>> {

        // This is the thing we're going to permute over
        private final List<T> contents;

        // The indices of the next permutation to emit
        private int[] permutation;

        // How many permutations are left to go
        private long remaining;

        public PermSpliterator(List<T> contents) {
            this(contents, IntStream.range(0, contents.size()).toArray(), factorial(contents.size()));
        }

        private PermSpliterator(List<T> contents, int[] permutation, long remaining) {
            this.contents    = contents;
            this.permutation = permutation;
            this.remaining   = remaining;
        }

        private static long factorial(long n) {
            return LongStream.range(1, n + 1).reduce(1, (a, b) -> a * b);
        }

        /**
         * Computes the permutation that follows current lexicographically. If current is the lexicographical
         * last permutation, returns the lexicographically first one.  (I.E. repeatedly calling this function
         * will cycle through all permutations infinitely; you can start with any permutation.)
         * <p>
         * If input array's elements are distinct, this function will wrap after being called
         * factorial(current.size) times.  If the input elements aren't distinct, it will wrap sooner.
         *
         * @param current the permutation before the one that will be returned
         * @return the permutation following current
         */
        public static int[] permutationAfter(int[] current) {
            // Early exit for singleton and empty arrays: only one permutation exists
            if (current.length <= 1)
                return current;

            int[] a = Arrays.copyOf(current, current.length);

            // The comments headed "L1" through "L4" are text taken from Knuth's Art of Computer Programming,
            // Section 7.2.1.2, Algorithm L (Lexicographic permutation generation).

            // L1. [Visit.]
            //     Visit the permutation a[1] a[2] ... a[n].

            int n = a.length - 1; // Note: Knuth's indices are 1-based; ours are 0-based.

            // L2. [Find j.]
            //     Set j <- n – 1.
            //     If a[j] >= a[j+1], decrease j by 1 repeatedly until a[j] < a[j+1].
            //     Terminate the algorithm if j = 0.
            //     (At this point j is the smallest subscript such that we’ve already visited all permutations
            //     beginning with a1 . . . aj. So the lexicographically next permutation will make aj larger.)

            int j = n - 1;
            while (a[j] >= a[j + 1]) {
                --j;
                if (j < 0) {                                // Our "terminate" behaviour is to wrap to starting perm
                    reverseIntArray(a, 0, a.length - 1);
                    return a;
                }
            }

            // L3. [Increase a[j].]
            //     Set l <- n.
            //     If a[j] >= a[l], decrease l by 1 repeatedly until a[j] < a[l].
            //     Then interchange a[j] <-> a[l].
            //     (Since a[j+1] >= ... >= a[n], element a[l] is the smallest element greater than a[j]
            //     that can legitimately follow a[1] ... a[j–1] in a permutation.
            //     Before the interchange we had  a[j+1] >= ... >= a[l–1] >= a[l] > a[j] >= a[l+1] >= ... >= a[n];
            //     after the interchange, we have a[j+1] >= ... >= a[l–1] >= a[j] > a[l] >= a[l+1] >= ... >= a[n].)

            {
                int l = n;
                while (a[j] >= a[l])
                    --l;

                int temp = a[j];
                a[j] = a[l];
                a[l] = temp;
            }

            // L4. [Reverse a[j+1] ... a[n].]
            //     Set k <- j + 1 and l <- n.
            //     Then, while k < l, inter change a[k] <-> a[l] and set k <- k + 1, l <- l – 1.
            //     Return to L1.

            reverseIntArray(a, j + 1, n);

            return a;
        }

        /**
         * Reverses the contents of a span of `a`, between indexes nearIx and farIx inclusive.
         * To reverse the entire array, call reverseIntArray(a, 0, a.length-1).
         * <p>
         * @param a the array to reverse
         * @param nearIx the index of the first element of the span
         * @param farIx the index of the last element of the span
         */
        private static void reverseIntArray(int[] a, int nearIx, int farIx) {
            for ( ; nearIx < farIx; ++nearIx, --farIx) {
                int temp = a[nearIx];
                a[nearIx] = a[farIx];
                a[farIx] = temp;
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super List<T>> action) {
            if (remaining > 0) {
                int[] next = permutationAfter(permutation);
                action.accept(IntStream.of(permutation)
                        .mapToObj(contents::get)
                        .toList());
                permutation = next;
                --remaining;
                return true;
            }
            else
                return false;
        }

        /**
         * Note that this split still traverses all elements in the parent spliterator.
         *
         * @return a new Spliterator that will continue some of this Spliterator's work
         */
        @Override
        public Spliterator<List<T>> trySplit() {
            long numToDonate = remaining / 2;

            // The 'other' spliterator is responsible for the next 'numToDonate' perms
            Spliterator<List<T>> other = new PermSpliterator<>(contents, permutation, numToDonate);

            // We'll skip over 'numToDonate' permutations and deliver the rest
            for ( ; numToDonate-- > 0; --remaining)
                permutation = permutationAfter(permutation);

            return other;
        }

        @Override
        public long estimateSize() {
            return remaining;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE | NONNULL | ORDERED | SIZED | SUBSIZED;
        }
    }
}


