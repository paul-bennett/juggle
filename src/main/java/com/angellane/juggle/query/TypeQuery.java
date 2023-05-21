package com.angellane.juggle.query;

import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.match.Match;

import java.lang.reflect.RecordComponent;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class TypeQuery extends Query<TypeCandidate> {
    public TypeFlavour          flavour             = null;

    public BoundedType          supertype           = null;
    public Set<BoundedType>     superInterfaces     = null;
    public Set<BoundedType>     permittedSubtypes   = null;
    public List<ParamSpec>      recordComponents    = null;

    public TypeQuery() {}
    public TypeQuery(TypeFlavour flavour) { this.flavour = flavour; }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        else if (!(other instanceof TypeQuery q))
            return false;
        else
            return super.equals(q)
                    && Objects.equals(flavour,           q.flavour)
                    && Objects.equals(supertype,         q.supertype)
                    && Objects.equals(superInterfaces,   q.superInterfaces)
                    && Objects.equals(permittedSubtypes, q.permittedSubtypes)
                    && Objects.equals(recordComponents,  q.recordComponents)
                    ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode()
                , flavour
                , supertype
                , superInterfaces
                , permittedSubtypes
                , recordComponents
        );
    }

    @Override
    public String toString() {
        return "ClassQuery{"
                + "flavour="              + flavour
                + ", annotationTypes="    + annotationTypes
                + ", accessibility="      + accessibility
                + ", modifierMask="       + modifierMask
                + ", modifiers="          + modifiers
                + ", declarationPattern=" + declarationPattern
                + ", supertype="          + supertype
                + ", superInterfaces="    + superInterfaces
                + ", permittedSubtypes="  + permittedSubtypes
                + ", recordComponents="   + recordComponents
                + '}';
    }

    @Override
    public
    <Q extends Query<TypeCandidate>, M extends Match<TypeCandidate, Q>>
    Stream<M> match(TypeCandidate candidate) {
        if (isMatchForCandidate(candidate)) {
            // TODO: implement scoring
            @SuppressWarnings("unchecked")      // TODO: remove this nasty cast
            M m = (M)new Match<>(candidate, this, 0);
            return Stream.of(m);
        }
        else
            return Stream.empty();
    }

    public boolean isMatchForCandidate(TypeCandidate ct) {
        return matchesAnnotations(ct.annotationTypes())
                && matchesAccessibility(ct.accessibility())
                && matchesModifiers(ct.otherModifiers())
                && matchesName(ct.declarationName())
                && matchesFlavour(ct.flavour())
                && matchesSupertype(ct.superClass())
                && matchesSuperInterfaces(ct.superInterfaces())
                && matchesPermittedSubtypes(ct.permittedSubtypes())
                && matchesRecordComponents(ct.recordComponents())
                ;
    }

    public void setSupertype(BoundedType supertype) {
        this.supertype = supertype;
    }

    public void setSuperInterfaces(Set<BoundedType> superInterfaces) {
        this.superInterfaces = superInterfaces;
    }

    public void setPermittedSubtypes(Set<BoundedType> permittedSubtypes) {
        this.permittedSubtypes = permittedSubtypes;
    }

    public void setRecordComponents(List<ParamSpec> components) {
        this.recordComponents = components;
    }

    private boolean matchesFlavour(TypeFlavour f) {
        return flavour == null || flavour.equals(f);
    }

    private boolean matchesSupertype(Class<?> c) {
        return supertype == null ||
                supertype.matchesClass(c);
    }

    private boolean matchesSuperInterfaces(Set<Class<?>> cs) {
        return superInterfaces == null ||
                superInterfaces.stream()
                        .allMatch(bt -> cs.stream().anyMatch(bt::matchesClass));
    }

    private boolean matchesPermittedSubtypes(Set<Class<?>> cs) {
        return permittedSubtypes == null
                || permittedSubtypes.stream()
                .allMatch(bt -> cs.stream().anyMatch(bt::matchesClass));
    }

    private boolean matchesRecordComponents(List<RecordComponent> rcs) {
        if (recordComponents == null)
            return true;
        else if (recordComponents.size() != rcs.size())
            return false;
        else {
            Iterator<RecordComponent> actualIter = rcs.iterator();
            return recordComponents.stream().allMatch(ps -> {
                if (!(ps instanceof SingleParam p))
                    return false;
                else {
                    RecordComponent rc = actualIter.next();

                    boolean nameMatches = p.paramName().matcher(rc.getName()).find();
                    boolean typeMatches = p.paramType().matchesClass(rc.getType());
                    return
                            nameMatches && typeMatches;
                }
            });
        }
    }
}
