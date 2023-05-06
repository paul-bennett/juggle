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
        return MemberQuery.patternsEqual(paramName, that.paramName) && Objects.equals(paramType, that.paramType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramName, paramType);
    }
}
