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
package com.angellane.juggle.query;

import java.util.Set;
import java.util.regex.Pattern;

public sealed interface ParamSpec permits ZeroOrMoreParams, SingleParam {
    static ZeroOrMoreParams ellipsis() {
        return new ZeroOrMoreParams();
    }

    static SingleParam wildcard() {
        return new SingleParam(
                Pattern.compile(""), BoundedType.unboundedWildcardType());
    }

    static SingleParam unnamed(BoundedType bt) {
        return new SingleParam(Pattern.compile(""), bt);
    }

    static SingleParam untyped(Pattern pat) {
        return new SingleParam(pat, BoundedType.unboundedWildcardType());
    }

    static SingleParam param(String name, Class<?> type) {
        return new SingleParam(Pattern.compile("^" + name + "$"),
                new BoundedType(Set.of(type), type));
    }

}
