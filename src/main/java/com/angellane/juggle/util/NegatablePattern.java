package com.angellane.juggle.util;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public record NegatablePattern(Pattern pattern, boolean positiveMatch) {

    public NegatablePattern(String text, boolean positiveMatch) {
        this(Pattern.compile(text), positiveMatch);
    }

    public static NegatablePattern compile(String pattern, int flags,
                                           boolean positiveMatch) {
        return new NegatablePattern(Pattern.compile(pattern, flags),
                positiveMatch);
    }

    public static NegatablePattern compile(String pattern, int flags) {
        return new NegatablePattern(Pattern.compile(pattern, flags), true);
    }

    public static NegatablePattern compile(String pattern) {
        return new NegatablePattern(Pattern.compile(pattern), true);
    }

    public static NegatablePattern alwaysMatch() {
        return new NegatablePattern(Pattern.compile(""), true);
    }

    public Predicate<String> asPredicate() {
        Predicate<String> positivePredicate = pattern().asPredicate();
        return positiveMatch() ? positivePredicate : positivePredicate.negate();
    }

    public boolean test(String text) {
        return asPredicate().test(text);
    }

    public boolean testAll(String... texts) {
        // When testing multiple strings, we behave differently depending on
        // whether we're using a positive or a negative matcher.  For positive
        // matchers we succeed if any of the texts match, but in the negative
        // case we'll return true only if none of the texts match the original
        // pattern, i.e. if all of them fail the test.

        Stream<String> textStream = Stream.of(texts).filter(Objects::nonNull);
        return positiveMatch()
                ? textStream.anyMatch(this::test)
                : textStream.allMatch(this::test);
    }


    // Because Pattern's `equals` and `hashCode` methods only consider identity
    // and not value equivalence, we derive our own `equals` and `hashCode`
    // methods here that manipulate the pattern's string version instead.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NegatablePattern that = (NegatablePattern) o;
        return positiveMatch == that.positiveMatch
                && pattern.flags() == that.pattern.flags()
                && Objects.equals(pattern.toString(), that.pattern.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern.toString(), pattern.flags(), positiveMatch);
    }

}
