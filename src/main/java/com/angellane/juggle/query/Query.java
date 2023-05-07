package com.angellane.juggle.query;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.candidate.Candidate;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public sealed class Query permits TypeQuery, MemberQuery {
    protected Set<Class<?>> annotationTypes     = null;
    protected Accessibility accessibility       = null;
    protected int           modifierMask        = 0;
    protected int           modifiers           = 0;
    protected Pattern       declarationPattern  = null;


    private static final int OTHER_MODIFIERS_MASK =
            Candidate.OTHER_MODIFIERS_MASK;

    // FRAMEWORK ==============================================================

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (!(other instanceof Query q))
            return false;
        else
            return Objects.equals(annotationTypes,       q.annotationTypes)
                    && accessibility                  == q.accessibility
                    && modifierMask                   == q.modifierMask
                    && modifiers                      == q.modifiers
                    && patternsEqual(declarationPattern, q.declarationPattern)
                    ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationTypes
                , accessibility
                , modifierMask
                , modifiers
                , declarationPattern
        );
    }


    // UTILITIES --------------------------------------------------------------

    /*
     * java.util.regex.Pattern doesn't provide a meaningful equality
     * test, so we convert both sides to Strings and hope for the best
     */
    static boolean patternsEqual(Pattern a, Pattern b) {
        return (a != null && b != null)
                ? (Objects.equals(a.pattern(), b.pattern())
                    && (a.flags() == b.flags()))
                : (a == b);
    }


    // SETTERS ================================================================

    public void addAnnotationType(Class<?> annotationType) {
        if (annotationTypes == null)
            annotationTypes = new HashSet<>();
        annotationTypes.add(annotationType);
    }

    public void addModifier(int modifier, boolean val) {
        // First clear this modifier bit, then add it if necessary
        this.modifiers = (this.modifiers & ~modifier) | (val ? modifier : 0);
        this.modifierMask |= modifier;
    }

    public void addModifier(int modifier) { addModifier(modifier, true); }

    public void setAccessibility(Accessibility accessibility) {
        this.accessibility = accessibility;
    }

    public void setNameExact(final String name) {
        setNamePattern(Pattern.compile(name, Pattern.LITERAL));
    }

    public void setNamePattern(Pattern pattern) {
        this.declarationPattern = pattern;
    }


    // MATCHERS ===============================================================

    protected boolean matchesAnnotations(Set<Class<?>> annotationTypes) {
        return this.annotationTypes == null
                || annotationTypes.containsAll(this.annotationTypes);
    }

    protected boolean matchesAccessibility(Accessibility access) {
        return this.accessibility == null
                || access.isAtLeastAsAccessibleAsOther(this.accessibility);
    }

    protected boolean matchesModifiers(int otherMods) {
        final int mask = modifierMask & OTHER_MODIFIERS_MASK;
        return (mask & modifiers) == (mask & otherMods);
    }

    protected boolean matchesName(String name) {
        return this.declarationPattern == null
                || this.declarationPattern.matcher(name).find();
    }

}
