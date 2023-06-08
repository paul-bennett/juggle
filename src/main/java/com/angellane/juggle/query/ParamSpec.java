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

    static SingleParam wildcard(Set<Class<?>> annotations,
                                int modifiers, int modifiersMask) {
        return param(annotations, modifiers, modifiersMask, null, null);
    }

    static SingleParam wildcard() {
        return wildcard(null, 0, 0);
    }

    static SingleParam param(Set<Class<?>> annotations,
                             int modifiers, int modifiersMask,
                             BoundedType bt,
                             Pattern pat) {
        if (bt  == null) bt  = BoundedType.unboundedWildcardType();
        if (pat == null) pat = Pattern.compile("");

        return new SingleParam(annotations, modifiers, modifiersMask, bt, pat);
    }

    static SingleParam param(BoundedType bt, Pattern pat) {
        return param(null, 0, 0, bt, pat);
    }

    static SingleParam param(Pattern pat) {
        return param(null, 0, 0, null, pat);
    }

    static SingleParam param(BoundedType bt) {
        return param(null, 0, 0, bt, null);
    }

    static SingleParam param(Class<?> type) {
        return param(BoundedType.exactType(type));
    }

    static SingleParam param(Class<?> type, String name) {
        return param(BoundedType.exactType(type),
                Pattern.compile("^%s$".formatted(name)));
    }

}
