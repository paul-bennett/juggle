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

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.comparator.MultiComparator;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.*;
import com.angellane.juggle.sink.Sink;
import com.angellane.juggle.source.Module;
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
        addSource(new Module(Object.class.getModule().getName()));  // "java.base" is always inspected
        addImportedPackageName(Object.class.getPackageName());      // "java.lang" is always imported
    }


    // Sources ========================================================================================================

    private final List<Source> sources = new ArrayList<>();
    public void addSource(Source source) { sources.add(source); source.setJuggler(this); }
    public List<Source> getSources() { return sources; }

    ResolvingURLClassLoader loader = new ResolvingURLClassLoader(new URL[] {});

    public void configureAllSources() {
        URL[] urls = getSources().stream()
                .map(Source::configure)
                .flatMap(Optional::stream)
                .toArray(URL[]::new);

        this.loader = new ResolvingURLClassLoader(urls);
    }


    public Collection<Class<?>> getClassesToSearch() {
        return getSources().stream()
                .flatMap(Source::classStream)
                .toList();
    }

    public void addImportedPackageName(String name) { importedPackageNames.add(name); }
    public List<String> getImportedPackageNames()   { return importedPackageNames; }



    public Optional<Class<?>> loadClassByName(String className) {
        try {
            Class<?> cls = loader.loadClass(className);
            loader.linkClass(cls);
            return Optional.of(cls);
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        } catch (NoClassDefFoundError e) {
            // This might be thrown if the class file references other classes that can't be loaded.
            // Maybe it depends on another JAR that hasn't been specified on the command-line with -j.
            System.err.println("*** Ignoring class " + className + ": " + e);
            return Optional.empty();
        }
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
            baseTypename = baseTypename.substring(0, baseTypename.length() - ARRAY_SUFFIX.length()).stripTrailing();

        // Start with the base type
        Juggler juggler = this;
        Class<?> ret = primitiveMap.computeIfAbsent(baseTypename,
                name -> {
                    // Actually now want to try typename plainly, then prefixed by each import in turn
                    // Default to Object if we can't find any match
                    Optional<Class<?>> opt =
                            Stream.of(Stream.of(""), importedPackageNames.stream().map(pkg -> pkg + "."))
                                    .flatMap(Function.identity())
                                    .map(prefix -> juggler.loadClassByName(prefix + name))
                                    .flatMap(Optional::stream)
                                    .findFirst();

                    if (opt.isPresent())
                        return opt.get();
                    else {
                        Class<?> def = Object.class;
                        // If we get here, the class wasn't found, either naked or with any imported package prefix
                        System.err.println("*** Couldn't find type: " + name + "; using " + def + " instead");
                        return def;
                    }
                });

        // Now add the array dimension
        for ( ; arrayDimension > 0; --arrayDimension)
            ret = ret.arrayType();

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
                : List.of(
                        SortCriteria.SCORE,   SortCriteria.ACCESS,
                        SortCriteria.PACKAGE, SortCriteria.NAME,
                        SortCriteria.TEXT
                );
    }

    Comparator<Match<TypeCandidate,TypeQuery>> getTypeComparator() {
        return MultiComparator.of(getSortCriteria().stream()
                .map(g -> g.getTypeComparator(this))
                .toList());
    }
    Comparator<Match<MemberCandidate,MemberQuery>> getMemberComparator() {
        return MultiComparator.of(getSortCriteria().stream()
                .map(g -> g.getMemberComparator(this))
                .toList());
    }


    // Sinks ==========================================================================================================

    public Sink sink;
    public void setSink(Sink sink) {
        this.sink = sink;
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
        var candidateChain  = chainProcessors(candidateProcessors);
        var matchChain      = chainProcessors(matchProcessors);

        source
                .flatMap(candidateChain)
                .<M>flatMap(query::match)
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
                           int paramCount = c.paramTypes().size();
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
