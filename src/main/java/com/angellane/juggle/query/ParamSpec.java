package com.angellane.juggle.query;

import java.util.Set;
import java.util.regex.Pattern;

public sealed interface ParamSpec permits ZeroOrMoreParams, SingleParam {
    static ZeroOrMoreParams ellipsis() {
        return new ZeroOrMoreParams();
    }

    static SingleParam wildcard() {
        return new SingleParam(
                Pattern.compile(""), BoundedType.wildcardType());
    }

    static SingleParam unnamed(BoundedType bt) {
        return new SingleParam(Pattern.compile(""), bt);
    }

    static SingleParam untyped(Pattern pat) {
        return new SingleParam(pat, BoundedType.wildcardType());
    }

    static SingleParam param(String name, Class<?> type) {
        return new SingleParam(Pattern.compile("^" + name + "$"),
                new BoundedType(Set.of(type), type));
    }

}
