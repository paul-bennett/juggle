package com.angellane.juggle.processor;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.CandidateMember;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a declaration query -- the result of parsing a pseudo-Java declaration that's
 * subsequently used as a template against which to match.
 */
public class DeclQuery {
    public record BoundedType(
            Set<Class<?>> upperBound,   // extends/implements
            Class<?> lowerBound         // super
    ) {
        public boolean matchesClass(Class<?> candidate) {
            // TODO: consider what to do about conversions here (esp boxing/unboxing)
            return (lowerBound == null || lowerBound.isAssignableFrom(candidate)) &&
                    (upperBound == null || upperBound.stream().allMatch(candidate::isAssignableFrom));
        }
    }

    public sealed interface ParamSpec permits Ellipsis, SingleParam {}
    public record Ellipsis() implements ParamSpec {}
    public record SingleParam(
            Matcher paramName,
            BoundedType paramType
    ) implements ParamSpec {}


    public Set<Class<? extends Annotation>> annotationTypes = null;
    public Accessibility accessibility = null;
    public int modifierMask = 0, modifiers = 0;
    public BoundedType returnType = null;

    public Pattern declarationName = null;

    public List<ParamSpec> params = null;
    public Set<BoundedType> exceptions = null;

    /**
     * @param x the Executable to test against this query
     * @return true if @x matches this query, or false otherwise
     */
    public boolean isMatchFor(Executable x) {
        return matchesMember(x)
                && matchesAnnotations(x.getDeclaringClass().getAnnotations(), x.getAnnotations())
                && matchesReturn(x)
                && matchesParams(x)
                && matchesExceptions(x);
    }

    public boolean isMatchFor(Field f) {
        return matchesMember(f)
                && matchesAnnotations(f.getDeclaringClass().getAnnotations(), f.getAnnotations())
                // TODO: consider "params" (i.e. setter)
                && matchesReturn(f)
                ;

    }

    public boolean matchesMember(Member m) {

        Stream<Predicate<Member>> matchers = Stream.of(
                this::matchesAccessibility,
                this::matchesModifiers,
                this::matchesName
        );
        return matchers.allMatch(p -> p.test(m));
    }

    private boolean matchesAnnotations(Annotation[] classAnnotations, Annotation[] memberAnnotations) {
        Set<Class<? extends Annotation>> targetAnnotations =
                Stream.of(classAnnotations, memberAnnotations)
                        .flatMap(Arrays::stream)
                        .map(Annotation::annotationType)
                        .collect(Collectors.toSet());

        return annotationTypes == null
                || targetAnnotations.containsAll(annotationTypes);
    }

    private boolean matchesAccessibility(Member m) {
        return accessibility == null
                || accessibility.isAtLeastAsAccessibleAsOther(Accessibility.fromModifiers(m.getModifiers()));
    }

    private boolean matchesModifiers(Member m) {
        final int ACCESS_MODIFIERS = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC;

        final int mask = this.modifierMask & ~ACCESS_MODIFIERS;     // Access modifiers handled separately

        return (m.getModifiers() & mask) == (this.modifiers & mask);
    }

    private boolean matchesName(Member m) {
        return this.declarationName == null
                || this.declarationName.matcher(m.getName()).matches();
    }

    private boolean matchesReturn(Member m) {
        // TODO: replace this with a type switch once those come out of preview
//        return switch (returnType) {
//            case null               -> true;
//            case Constructor<?> c   -> returnType.matchesClass(c.getDeclaringClass());
//            case Field f            -> returnType.matchesClass(f.getType());
//            case Method x           -> returnType.matchesClass(x.getReturnType());
//            default                 -> false;
//        }
        if (returnType == null)                     return true;
        else if (m instanceof Constructor<?> c)     return returnType.matchesClass(c.getDeclaringClass());
        else if (m instanceof Field f)              return returnType.matchesClass(f.getType());
        else if (m instanceof Method x)             return returnType.matchesClass(x.getReturnType());
        else                                        return false;
    }

    private boolean matchesParams(Member m) {
        return true;    // TODO: implement
    }

    private boolean matchesExceptions(Executable x) {
        Set<Class<?>> targetExceptions = Set.of(x.getExceptionTypes());     // Class<? extends Throwable>

        return exceptions == null ||
                exceptions.stream().allMatch(ex -> targetExceptions.stream().anyMatch(ex::matchesClass));
    }

    public boolean matchesCandidate(CandidateMember cm) {
        // TODO: consider whether we should be using CandidateMember fields instead of Member.
        Member m = cm.getMember();

        if (m instanceof Executable x)  return isMatchFor(x);
        else if (m instanceof Field f)  return isMatchFor(f);
        else                            return false;               // Should never get here; TODO: log?
    }

    public DeclQuery(final String declString) {
        if (!declString.isEmpty())
            System.err.println("QUERY STRING: `" + declString + "'");

        // TODO: parse declString and fill out fields of this
    }
}
