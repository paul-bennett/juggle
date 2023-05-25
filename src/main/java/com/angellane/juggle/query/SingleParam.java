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

import java.util.Objects;
import java.util.regex.Pattern;

public record SingleParam(
        Pattern paramName,
        BoundedType paramType
) implements ParamSpec {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleParam that = (SingleParam) o;
        return Query.patternsEqual(paramName, that.paramName)
                && Objects.equals(paramType, that.paramType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramType);
    }
}
