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
package com.angellane.juggle.processor;

import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.util.PermutationGenerator;

import java.util.function.Function;
import java.util.stream.Stream;

public class PermuteParams implements
        Function<MemberCandidate, Stream<MemberCandidate>> {
    @Override
    public Stream<MemberCandidate> apply(MemberCandidate candidate) {
        return (new PermutationGenerator<>(candidate.params())).stream()
                .distinct()
                .map(ps -> new MemberCandidate(candidate, ps)
                );
    }
}
