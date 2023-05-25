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
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.function.IntUnaryOperator.identity;

public class Decomposer {
    /**
     * Decomposes a number into parts, such that the sum of the parts is
     * equal to the original number.  As each decomposition is computed,
     * it is passed to the Consumer.
     *
     * @param num the number to decompose
     * @param parts how many parts to decompose into
     * @param consumer the recipient of each decomposition
     */
    public static void decomposeIntoParts(int num, int parts,
                                          Consumer<int[]> consumer) {
        // Essentially we're creating a group of #parts nested loops.
        // This array holds the current index of each loop.
        int[] indices = IntStream.iterate(num, identity())
                .limit(parts - 1)
                .toArray();

        while (Arrays.stream(indices).anyMatch(ix -> ix != 0)) {
            // VISIT
            consumer.accept(indexesToCounts(num, indices));

            // STEP

            // Does the rightmost divider (lowest ix) stand alone?
            if (indices.length == 1 || indices[0] != indices[1])
                // yes: bump it along one notch
                indices[0]--;
            else {
                // no: nudge the leftmost divider from the group left one notch
                // and move all the rest of the dividers in that group to the
                // far right.
                int oldNotch = indices[0];
                int i;

                for (i = 0; i < indices.length; i++)
                    if (indices[i] == oldNotch)
                        indices[i] = num;     // Reset group's dividers
                    else
                        break;              // Stop when divider not in group

                indices[i - 1] = oldNotch - 1;     // Nudge previous div (last in grp)
            }

        }
        consumer.accept(indexesToCounts(num, indices));
    }

    private static int[] indexesToCounts(int num, int[] indexes) {
        if (indexes.length == 0) return new int[]{num};

        int[] counts = new int[indexes.length + 1];

        counts[counts.length - 1] = num - indexes[0];
        for (int i = 1; i < indexes.length; i++)
            counts[counts.length - 1 - i] = indexes[i - 1] - indexes[i];
        counts[0] = indexes[indexes.length - 1];

        return counts;
    }
}
