package com.angellane.juggle.query;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.candidate.CandidateMember;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public sealed class Query permits ClassQuery, MemberQuery {

    public Accessibility accessibility = null;
    public int modifierMask = 0;
    public int modifiers = 0;
    public Pattern declarationPattern = null;
    public Set<Class<?>> annotationTypes = null;

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

    boolean matchesName(CandidateMember cm) {
        return this.declarationPattern == null
                || this.declarationPattern.matcher(cm.member().getName()).find();
    }

    public void addAnnotationType(Class<?> annotationType) {
        if (annotationTypes == null)
            annotationTypes = new HashSet<>();
        annotationTypes.add(annotationType);
    }

    boolean matchesAnnotations(CandidateMember cm) {
        return this.annotationTypes == null
                || cm.annotationTypes().containsAll(this.annotationTypes);
    }
}
