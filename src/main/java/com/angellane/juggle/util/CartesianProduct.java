/*
 *  Juggle -- an API search tool for Java
 *
 *  Copyright 2020,2023 Paul Bennett
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

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CartesianProduct<T> {

    private final List<T>[] lists;
    private int numRemaining ;

    @SafeVarargs
    public static <T> CartesianProduct<T> of(List<T>... lists) {
        return new CartesianProduct<>(lists);
    }

    public CartesianProduct(List<T>[] lists) {
        this.lists = lists;
        this.numRemaining = Arrays.stream(lists)
                .mapToInt(List::size)
                .reduce(1, (a, b) -> a * b);
    }

    public Stream<List<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    protected Spliterator<List<T>> spliterator() {
        numRemaining = Arrays.stream(lists)
                .mapToInt(List::size)
                .reduce((a,b) -> a*b)
                .orElse(0);     // case when there are no lists
        return new CartesianProductSpliterator<>(lists, 0, numRemaining);
    }


    public static class CartesianProductSpliterator<T> implements Spliterator<List<T>> {
        final List<T>[] lists;

        /** divisors[i] is the number entries in the CartesianProduct stream between changes to lists[i]. */
        final int[] divisors;

        /** the index of the next entry to emit */
        int pos;

        /** the total number of entries this spliterator is yet to emit. */
        int numRemaining;

        CartesianProductSpliterator(List<T>[] lists, int pos, int numRemaining) {
            this.lists        = lists;
            this.pos          = pos;
            this.numRemaining = numRemaining;

            this.divisors = new int[lists.length];
            for (int i=lists.length-1, d=1; i>=0; --i)
                divisors[i] = (d *= lists[i].size());
        }

        @Override
        public boolean tryAdvance(Consumer<? super List<T>> action) {
            if (numRemaining == 0)
                return false;
            else {
                // Consider the product of arrays with 1,2,3 and 4 members.
                //  => [0000, 0001, 0002, 0003, 0010, 0011, 0012, 0013, 0020, 0021, 0022, 0023, 0100, 0101, 0102, ...]
                //
                // index[3] =  num %        4
                // index[2] = (num %     (3*4)) /      4
                // index[1] = (num %   (2*3*4)) /   (3*4)
                // index[0] = (num % (1*2*3*4)) / (2*3*4)
                //
                // Example: when num=14, should get 0102.
                //   14 %        4            = 2
                //   14 %     (3*4) /      4  = 0
                //   14 %   (2*3*4) /   (3*4) = 1
                //   14 % (1*2*3*4) / (2*3*4) = 0

                // divisors[i] is the product of lists[i].size() .. lists[lists.length-1].size()

                // In general terms then, for product number n the component from argument a
                //      indexForList_i = num % divisors[i] / (divisors / lists[i].size())

                List<T> result = IntStream.range(0, lists.length)
                        .mapToObj(i -> lists[i].get(pos % divisors[i] / (divisors[i]/lists[i].size())))
                        .toList();

                action.accept(result);

                pos++;
                numRemaining--;

                return true;
            }
        }

        @Override
        public Spliterator<List<T>> trySplit() {
            int numToDonate = numRemaining / 2;

            var other = new CartesianProductSpliterator<>(lists, pos, numToDonate);

            pos += numToDonate;
            numRemaining -= numToDonate;

            return other;
        }

        @Override
        public long estimateSize() {
            return numRemaining;
        }

        @Override
        public int characteristics() {
            return IMMUTABLE | NONNULL | ORDERED | SIZED | SUBSIZED;
        }
    }
}


