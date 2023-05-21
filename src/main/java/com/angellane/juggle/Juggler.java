package com.angellane.juggle;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.candidate.MemberCandidate;
import com.angellane.juggle.candidate.TypeCandidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.MemberQuery;
import com.angellane.juggle.query.Query;
import com.angellane.juggle.query.TypeQuery;
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
    Deque<Function<Match<TypeCandidate, TypeQuery>,
            Stream<Match<TypeCandidate, TypeQuery>>>>
            typeProcessors = new LinkedList<>();
    private final
    Deque<Function<Match<MemberCandidate, MemberQuery>,
            Stream<Match<MemberCandidate, MemberQuery>>>>
            memberProcessors = new LinkedList<>();

    public void appendTypeProcessor(
            Function<
                    Match<TypeCandidate, TypeQuery>,
                    Stream<Match<TypeCandidate, TypeQuery>>
                    > processor) {
        typeProcessors.addLast(processor);
    }
    public void prependTypeProcessor(
            Function<
                    Match<TypeCandidate, TypeQuery>,
                    Stream<Match<TypeCandidate, TypeQuery>>
                    > processor) {
        typeProcessors.addFirst(processor);
    }

    public void appendMemberProcessor(
            Function<
                    Match<MemberCandidate, MemberQuery>,
                    Stream<Match<MemberCandidate, MemberQuery>>
                    > processor) {
        memberProcessors.addLast(processor);
    }
    public void prependMemberProcessor(
            Function<
                    Match<MemberCandidate, MemberQuery>,
                    Stream<Match<MemberCandidate, MemberQuery>>
                    > processor) {
        memberProcessors.addFirst(processor);
    }

    private
    <C extends Candidate, Q extends Query<C>, M extends Match<C,Q>>
    Function<M, Stream<M>> makeFilter(Predicate<C> pred) {
        return m -> pred.test(m.candidate())
                ? Stream.of(m)
                : Stream.empty();
    }

    public void appendTypeFilter(Predicate<TypeCandidate> pred) {
        appendTypeProcessor(makeFilter(pred));
    }
    public void prependTypeFilter(Predicate<TypeCandidate> pred) {
        prependTypeProcessor(makeFilter(pred));
    }

    public void appendMemberFilter(Predicate<MemberCandidate> pred) {
        appendMemberProcessor(makeFilter(pred));
    }
    public void prependMemberFilter(Predicate<MemberCandidate> pred) {
        prependMemberProcessor(makeFilter(pred));
    }


    // Sorting ========================================================================================================

    public List<SortCriteria> sortCriteria                  = new ArrayList<>();
    public void addSortCriteria(SortCriteria sort)          { sortCriteria.add(sort); }
    public List<SortCriteria> getSortCriteria() {
        // Return default criteria of none were set.
        return sortCriteria.size() != 0
                ? sortCriteria
                : List.of(SortCriteria.SCORE, SortCriteria.ACCESS, SortCriteria.PACKAGE, SortCriteria.NAME);
    }

    <C extends Candidate, Q extends Query<C>, M extends Match<C,Q>>
    Comparator<M> getComparator() {
        // TODO: implement
        return null;
    }

//    public <C extends Candidate, Q extends Query<Candidate>, M extends Match<C,Q>>
//    Comparator<M> getComparator() {
//        return null;
////        return MultiComparator.of(getSortCriteria().stream()
////                        .map(g -> g.getComparator(this))
////                        .toList());
//    }


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

    protected
    <C extends Candidate, Q extends Query<C>, M extends Match<C,Q>>
    void runPipeline(Stream<C> source,
                     Q query,
                     Collection<Function<M, Stream<M>>> processors
    ) {
        // chain the output of one processor function to the next
        Function<M, Stream<M>> processorChain = processors.stream()
                .reduce(Stream::of,
                        (a, b) -> (m -> a.apply(m).flatMap(b)));

        source
                .<M>flatMap(query::match)
                .flatMap(processorChain)
                .distinct()
                .sorted(getComparator())
                .map(Match::candidate)
                .forEach(sink);
    }

    public void doJuggle() {
        if (typeQuery != null)
            runPipeline(candidateTypeStream(), this.typeQuery, typeProcessors);
        else
            runPipeline(candidateMemberStream(), this.memberQuery, memberProcessors);
    }
}
