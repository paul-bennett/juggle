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
package com.angellane.juggle;

import com.angellane.backport.jdk11.java.lang.Module;
import com.angellane.backport.jdk11.java.lang.module.FindException;
import com.angellane.backport.jdk11.java.lang.StringExtras;
import com.angellane.backport.jdk11.java.util.OptionalExtras;
import com.angellane.backport.jdk17.java.lang.ClassExtras;
import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.comparator.MultiComparator;
import com.angellane.juggle.formatter.Formatter;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.match.TypeMatcher;
import com.angellane.juggle.query.*;
import com.angellane.juggle.sink.Sink;
import com.angellane.juggle.source.FileSource;
import com.angellane.juggle.source.ModuleSource;
import com.angellane.juggle.util.ResolvingURLClassLoader;
import com.angellane.juggle.source.Source;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Juggler {
    private final List<String> importedPackageNames = new ArrayList<>();

    public Juggler() {
        if (Module._modulesSupported())
            addSource(new ModuleSource());
        else
            addSource(new FileSource(System.getProperty("java.home") + "/lib/rt.jar"));

        importedPackageNames.add(Object.class.getPackage().getName());    // "java.lang" is always imported
    }


    // Modules ========================================================================================================

    private final List<String> modulePaths = new ArrayList<>();

    public void addModulePath(String path) {
        modulePaths.add(path);
    }

    public List<String> getModulePaths() {
        return modulePaths.size() == 0 ? Collections.singletonList(".") : modulePaths;
    }


    // Sources ========================================================================================================

    private final List<Source> sources = new ArrayList<>();
    public void addSource(Source source) { sources.add(source); source.setJuggler(this); }
    public List<Source> getSources() { return sources; }

    ResolvingURLClassLoader loader = new ResolvingURLClassLoader(new URL[] {});

    public void configureAllSources() {
        try {
            URL[] urls = getSources().stream()
                    .map(Source::configure)
                    .flatMap(List::stream)
                    .toArray(URL[]::new);

            this.loader = new ResolvingURLClassLoader(urls);
        }
        catch (FindException ex) {
            throw new JuggleError(ex.getLocalizedMessage());
        }
    }


    public Collection<Class<?>> getClassesToSearch() {
        return getSources().stream()
                .flatMap(Source::classStream)
                .collect(Collectors.toList());
    }

    public void addImportedPackageName(String name) {
        // java.lang is always present and must remain the last element
        importedPackageNames.add(importedPackageNames.size()-1, name);
    }
    public List<String> getImportedPackageNames()   { return importedPackageNames; }

    public Optional<Class<?>> loadClassByName(String className) {
        String[] nameComponents = className.split("\\.");

        // className might refer to an inner class.  We need to convert it
        // to a "binary name" (JLS 13.1) before passing on to the ClassLoader.
        // That involves finding the boundary between the outer and inner
        // class name components, and replacing it (a dot) and all component
        // separators to its right with dollar signs.
        //
        // The most likely case is that there is no inner class, so we'll
        // start from the right and work our way left.

        for (int numOuterComponents = nameComponents.length;
             numOuterComponents > 0;
             --numOuterComponents)
        {
            String[] outerComps = Arrays.copyOfRange(
                    nameComponents, 0, numOuterComponents
            );
            String[] innerComps = Arrays.copyOfRange(
                    nameComponents, numOuterComponents, nameComponents.length
            );

            String binaryName = String.join(".", outerComps) +
                    (numOuterComponents == nameComponents.length
                            ? "" : "$" +  String.join("$", innerComps));

            try {
                Class<?> cls = loader.loadClass(binaryName);
                loader.linkClass(cls);
                return Optional.of(cls);
            } catch (ClassNotFoundException ex) {
                // Carry on with next iteration
            } catch (NoClassDefFoundError e) {
                // This might be thrown if the class file references other classes that can't be loaded.
                // Maybe it depends on another JAR that hasn't been specified on the command-line with -cp.
                warn("related class " + className + ": " + e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static final Map<String, Class<?>> primitiveMap = Stream.of(
                        Void.TYPE, Boolean.TYPE, Character.TYPE,
                        Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE,
                        Float.TYPE, Double.TYPE
                    )
                    .collect(Collectors.toMap(Class::getTypeName, Function.identity()));

    public Class<?> classForTypename(String typename) {
        final String ARRAY_SUFFIX = "[]";

        // If this is an array, work out how many dimensions are involved, and strip []s from typename
        int arrayDimension;
        String baseTypename = typename;
        for (arrayDimension = 0; baseTypename.endsWith(ARRAY_SUFFIX); ++arrayDimension)
            baseTypename = StringExtras.stripTrailing(
                    baseTypename.substring(0, baseTypename.length() - ARRAY_SUFFIX.length())
            );

        // Start with the base type
        Juggler juggler = this;
        Class<?> ret = primitiveMap.computeIfAbsent(baseTypename,
                name -> {
                    // Actually now want to try typename plainly, then prefixed by each import in turn
                    // Default to Object if we can't find any match
                    Optional<Class<?>> opt =
                            Stream.of(Stream.of(""), getImportedPackageNames().stream().map(pkg -> pkg + "."))
                                    .flatMap(Function.identity())
                                    .map(prefix -> juggler.loadClassByName(prefix + name))
                                    .flatMap(OptionalExtras::stream)
                                    .findFirst();

                    if (opt.isPresent())
                        return opt.get();
                    else {
                        // If we get here, the class wasn't found, either naked or with any imported package prefix
                        throw new JuggleError("Couldn't find type: " + name);
                    }
                });

        // Now add the array dimension
        for ( ; arrayDimension > 0; --arrayDimension)
            ret = ClassExtras.arrayType(ret);

        return ret;
    }

    public Stream<TypeCandidate> candidateTypeStream() {
        return getClassesToSearch().stream()
                .map(TypeCandidate::candidateForType);
    }

    public Stream<MemberCandidate> candidateMemberStream() {
        return getClassesToSearch().stream()
                .flatMap(c -> Stream.of(
                                  Arrays.stream(c.getDeclaredFields())
                                        .map(MemberCandidate::membersFromField)
                                        .flatMap(List::stream)
                                , Arrays.stream(c.getDeclaredConstructors())
                                        .map(MemberCandidate::memberFromConstructor)
                                , Arrays.stream(c.getDeclaredMethods())
                                        .map(MemberCandidate::memberFromMethod)
                                )
                        .flatMap(Function.identity())
                );
    }

    // Processors =====================================================================================================

    private final
    List<Function<TypeCandidate, Stream<TypeCandidate>>>
            typeCandidateProcessors = new ArrayList<>();
    Deque<Function<MemberCandidate, Stream<MemberCandidate>>>
            memberCandidateProcessors = new LinkedList<>();
    private final
    List<Function<Match<TypeCandidate, TypeQuery>,
            Stream<Match<TypeCandidate, TypeQuery>>>>
            typeMatchProcessors = new ArrayList<>();
    private final
    List<Function<Match<MemberCandidate, MemberQuery>,
            Stream<Match<MemberCandidate, MemberQuery>>>>
            memberMatchProcessors = new ArrayList<>();

    public void prependMemberCandidateProcessor(
            Function<MemberCandidate, Stream<MemberCandidate>> processor
    ) {
        memberCandidateProcessors.addFirst(processor);
    }

    public void addTypeMatchProcessor(
            Function<
                    Match<TypeCandidate, TypeQuery>,
                    Stream<Match<TypeCandidate, TypeQuery>>
                    > processor) {
        typeMatchProcessors.add(processor);
    }
    public void addMemberMatchProcessor(
            Function<
                    Match<MemberCandidate, MemberQuery>,
                    Stream<Match<MemberCandidate, MemberQuery>>
                    > processor) {
        memberMatchProcessors.add(processor);
    }

    private
    <C extends Candidate, Q extends Query<C>, M extends Match<C,Q>>
    Function<M, Stream<M>> makeFilter(Predicate<C> pred) {
        return m -> pred.test(m.candidate())
                ? Stream.of(m)
                : Stream.empty();
    }
    public void addTypeFilter(Predicate<TypeCandidate> pred) {
        addTypeMatchProcessor(makeFilter(pred));
    }
    public void addMemberFilter(Predicate<MemberCandidate> pred) {
        addMemberMatchProcessor(makeFilter(pred));
    }


    // Sorting ========================================================================================================

    public List<SortCriteria> sortCriteria                  = new ArrayList<>();
    public void addSortCriteria(SortCriteria sort)          { sortCriteria.add(sort); }
    public List<SortCriteria> getSortCriteria() {
        // Return default criteria of none were set.
        return sortCriteria.size() != 0
                ? sortCriteria
                : Arrays.asList(
                        SortCriteria.SCORE,   SortCriteria.HIERARCHY,
                        SortCriteria.ACCESS,  SortCriteria.PACKAGE,
                        SortCriteria.NAME,    SortCriteria.TEXT
                );
    }

    Comparator<Match<TypeCandidate,TypeQuery>> getTypeComparator() {
        return MultiComparator.of(getSortCriteria().stream()
                .map(g -> g.getTypeComparator(this))
                .collect(Collectors.toList()));
    }
    Comparator<Match<MemberCandidate,MemberQuery>> getMemberComparator() {
        return MultiComparator.of(getSortCriteria().stream()
                .map(g -> g.getMemberComparator(this))
                .collect(Collectors.toList()));
    }


    // Sinks ==========================================================================================================

    public Sink sink;
    public void setSink(Sink sink) {
        this.sink = sink;
    }

    private Formatter formatter;
    public void setFormatter(Formatter f) {
        this.formatter = f;
    }

    public void info(String msg) {
        System.out.println(formatter.formatInfo(msg));
    }

    public void warn(String msg) {
        System.err.println(formatter.formatWarning(
                "*** Warning: " + msg));
    }
    public void error(String msg) {
        System.err.println(formatter.formatError(
                "*** Error: " + msg));
    }



    // Conversions ====================================================================================================

    enum Conversions { AUTO, NONE, ALL }
    private Conversions conversions = Conversions.AUTO;
    public void setConversions(Conversions conversions) {
        this.conversions = conversions;
    }

    public <C extends Candidate, Q extends Query<C>>
    TypeMatcher getTypeMatcher(Q query) {
        switch (conversions) {
            case ALL: return new TypeMatcher(true);
            case NONE: return new TypeMatcher(false);
            case AUTO: return new TypeMatcher(!query.hasBoundedWildcards());
        }
        return null;
    }


    // Main Event =====================================================================================================

    MemberQuery memberQuery = new MemberQuery();
    public void setMemberQuery(MemberQuery memberQuery) {
        this.memberQuery = memberQuery;
    }

    TypeQuery typeQuery;
    public void setTypeQuery(TypeQuery typeQuery) {
        this.typeQuery = typeQuery;
    }


    /**
     * Chains together a collection of processor functions, passing the output
     * elements of one to the input of the next.  Each processor function
     * takes a single parameter of type T, and returns a Stream of T.
     *
     * @param processors Processors to be chained together
     * @return The composite function
     * @param <T> Type of elements to be passed through the chain
     */
    private <T>
    Function<T, Stream<T>> chainProcessors(
            Collection<Function<T, Stream<T>>> processors
    ) {
        return processors.stream()
                .reduce(Stream::of, (a,b) -> (v -> a.apply(v).flatMap(b)));
    }

    protected
    <C extends Candidate, Q extends Query<C>, M extends Match<C,Q>>
    void runPipeline(Stream<C> source,
                     Q query,
                     Collection<Function<C, Stream<C>>> candidateProcessors,
                     Collection<Function<M, Stream<M>>> matchProcessors,
                     Comparator<M> comparator
    ) {
        Function<C, Stream<C>> candidateChain  = chainProcessors(candidateProcessors);
        Function<M, Stream<M>> matchChain      = chainProcessors(matchProcessors);

        source
                .flatMap(candidateChain)
                .<M>flatMap(c -> query.match(getTypeMatcher(query), c))
                .flatMap(matchChain)
                .distinct()
                .sorted(comparator)
                .map(Match::candidate)
                .forEach(sink);
    }

    public void doJuggle() {
        if (typeQuery != null)
            runPipeline(candidateTypeStream(), this.typeQuery,
                    typeCandidateProcessors, typeMatchProcessors,
                    getTypeComparator());
        else {
            if (this.memberQuery.params != null) {
               // Permuting parameters can be very slow, so here's a small
               // optimisation
               final long minParams = this.memberQuery.params.stream()
                       .filter(p -> p instanceof SingleParam).count();
               final long maxParams = this.memberQuery.params.stream()
                       .noneMatch(p -> p instanceof ZeroOrMoreParams)
                       ? minParams
                       : Long.MAX_VALUE;

               prependMemberCandidateProcessor(
                       c -> {
                           int paramCount = c.params().size();
                           return (paramCount >= minParams
                                   && paramCount <= maxParams)
                                   ? Stream.of(c)
                                   : Stream.of();
                       }
               );
            }

            runPipeline(candidateMemberStream(), this.memberQuery,
                    memberCandidateProcessors, memberMatchProcessors,
                    getMemberComparator());
        }
    }
}
