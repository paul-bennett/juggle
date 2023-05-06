package com.angellane.juggle.query;

import java.util.Set;
import java.util.regex.Pattern;

public sealed interface ParamSpec permits ParamEllipsis, SingleParam {
    static ParamSpec ellipsis() {
        return new ParamEllipsis();
    }

    static ParamSpec wildcard() {
        return new SingleParam(
                Pattern.compile(""), BoundedType.wildcardType());
    }

    static ParamSpec unnamed(BoundedType bt) {
        return new SingleParam(Pattern.compile(""), bt);
    }

    static ParamSpec untyped(Pattern pat) {
        return new SingleParam(pat, BoundedType.wildcardType());
    }

    static ParamSpec param(String name, Class<?> type) {
        return new SingleParam(Pattern.compile("^" + name + "$"),
                new BoundedType(Set.of(type), type));
    }

}
