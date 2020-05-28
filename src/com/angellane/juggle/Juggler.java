package com.angellane.juggle;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Juggler {
    private JuggleClassLoader loader;

    private Collection<Class> classesToSearch;

    public Juggler(String[] jars, String[] mods) {
        if (jars == null) jars = new String[] {};
        if (mods == null) mods = new String[] {};

        // TODO: add modules here too?

        URL[] urls = Arrays.stream(jars)
                .flatMap((String path) -> {
                    try {
                        return Stream.of(Paths.get(path).toUri().toURL());
                    } catch (MalformedURLException ex) {
                        return Stream.empty();
                    }
                })
                .toArray(URL[]::new);

        loader = new JuggleClassLoader(urls);

        classesToSearch = Arrays.stream(jars)
          .flatMap((var jarName) -> classesInJar(jarName).stream())
          .flatMap((var className) -> {
              try {
                  return Stream.of(loader.loadClassWithoutResolving(className));
              }
              catch (ClassNotFoundException ex) {
                  System.err.println("Warning: class " + className + " not found");
                  return Stream.empty();
              }
          })
          .collect(Collectors.toList());
    }

    // Returns list of class names within a JAR.  Note: these class names might not be valid Java identifiers,
    // especially in the case of inner classes or JAR files generated by something other than the Java compiler.
    public List<String> classesInJar(String filename) {
        try (JarFile file = new JarFile(filename)) {
            var CLASS_SUFFIX = ".class";
            return file.stream()
                    .filter(Predicate.not(JarEntry::isDirectory))
                    .map(JarEntry::getName)
                    .filter((var s) -> s.endsWith(CLASS_SUFFIX))
                    .map((var s) -> s.substring(0, s.length() - CLASS_SUFFIX.length()))
                    .map((var s) -> s.replace('/', '.'))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Couldn't read JAR file: " + filename + "; ignoring.");
            return Collections.emptyList();
        }
    }

    public Class classForTypename(String typename) {
        final String ARRAY_SUFFIX = "[]";

        // If this is an array, work out how many dimensions are involved.
        int arrayDimension;
        for (arrayDimension = 0; typename.endsWith(ARRAY_SUFFIX); ++arrayDimension)
            typename = typename.substring(0, typename.length() - ARRAY_SUFFIX.length()).stripTrailing();

        // TODO: think about Generics

        // Start with the base type
        Class ret = switch (typename) {
            case "void"    -> Void.TYPE;
            case "boolean" -> Boolean.TYPE;
            case "char"    -> Character.TYPE;
            case "byte"    -> Byte.TYPE;
            case "short"   -> Short.TYPE;
            case "int"     -> Integer.TYPE;
            case "long"    -> Long.TYPE;
            case "float"   -> Float.TYPE;
            case "double"  -> Double.TYPE;
            default        -> {
                try {
                    yield loader.loadClassWithoutResolving(typename);
                } catch (ClassNotFoundException e) {
                    Class defaultType = Object.class;

                    System.err.println("Warning: couldn't find class: " + typename
                                        + "; using " + defaultType + " instead");
                    yield defaultType;
                }
            }
        };

        // Now add the array dimension
        for ( ; arrayDimension > 0; --arrayDimension)
            ret = ret.arrayType();

        return ret;
    }

    public Method[] findMethods(String[] paramTypenames, String returnTypename) {
        Class[] paramTypes = Arrays.stream(paramTypenames).map(this::classForTypename).toArray(Class[]::new);
        Class returnType = returnTypename == null ? Void.TYPE : classForTypename(returnTypename);

        return findMethods(List.of(paramTypes), returnType);
    }

    public Method[] findMethods(List<Class> queryParamTypes, Class queryReturnType) {
        // We search the types involved in the query as well as the JARs.  This gets us a better hit rate on some
        // JDK classes (which aren't listed in classesToSearch).  However, it won't get all methods.  Notable it'll
        // fail to find static methods whose signatures don't include the declaring class, e.g. Math.sin().

        Stream<Class> queryTypeStream = Stream.concat(queryParamTypes.stream(), Stream.of(queryReturnType));

        return Stream.concat(queryTypeStream, classesToSearch.stream())
                .distinct()
                .flatMap((var c) -> {
                    try {
                        return Arrays.stream(c.getDeclaredMethods());
                    } catch (NoClassDefFoundError e) {
                        // This might be thrown if the class file references other classes that can't be loaded.
                        // Maybe it depends on another JAR that hasn't been specified on the command-line with -j.
                        System.err.println("*** Ignoring class " + c + ": " + e);
                        return Stream.empty();
                    }
                })
                .filter((var m) -> doesMethodMatch(queryParamTypes, queryReturnType, m))
                .toArray(Method[]::new);
    }

    public boolean doesMethodMatch(List<Class> queryParamTypes, Class queryReturnType, Method m) {
        List<Class> methodParamTypes = new LinkedList<>();

        // TODO: what about constructors?

        // For instance methods, treat the declaring class as a parameter
        if (Modifier.STATIC != (m.getModifiers() & Modifier.STATIC))
            methodParamTypes.add(m.getDeclaringClass());

        methodParamTypes.addAll(Arrays.asList(m.getParameterTypes()));

        Class methodReturnType = m.getReturnType();

        // Now for the big questions: do the parameter types match? Does the return match?

        // TODO: auto-boxing/unboxing

        Iterator<Class> queryTypeIter = queryParamTypes.iterator();

        return queryParamTypes.size() == methodParamTypes.size()
                && methodParamTypes.stream().allMatch(
                        (Class mpt) -> isTypeCompatibleForInvocation(mpt, queryTypeIter.next())
                    )
                && isTypeCompatibleForAssignment(queryReturnType, methodReturnType)
                ;
    }

    // An instinctive notion of whether two types are compatible.
    // May or may not be correct.  Written from memory, not the JLS
    //
    // Are the types of writtenType and readType compatible, as if:
    //    WrittenType w; ReadType r; w = r;
    // or
    //    ReadType r() {}
    //    WrittenType w = r();
    boolean isTypeInstinctivelyCompatible(Class writtenType, Class readType) {
        // Three cases:
        // 1. Primitive widening conversions
        // 2. Boxing/unboxing conversions
        // 3. Reference conversions
        return Optional.ofNullable(wideningConversions.get(readType)).orElse(Set.of()).contains(writtenType)
                || writtenType.equals(boxingConversions.get(readType))
                || writtenType.isAssignableFrom(readType);
    }


    // These next few methods implement the conversions described in the Java Langauge Specification
    // (Java SE 14 edition) chapter 5 "Conversions and Contexts":
    //    https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html

    // Invocation Context: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.3
    // TODO: check against JLS
    boolean isTypeCompatibleForInvocation(Class parameterType, Class argumentType) {
        return isTypeInstinctivelyCompatible(parameterType, argumentType);
    }

    // Assignment Context: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.2
    boolean isTypeCompatibleForAssignment(Class variableType, Class returnType) {
        return isTypeInstinctivelyCompatible(variableType, returnType);
    }

    // The 19 Widening Primitive Conversions, documented in Java Language Specification (Java SE 14 edn) sect 5.1.2
    // https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.2
    Map<Class, Set<Class>> wideningConversions = Map.ofEntries(
            Map.entry(Byte.TYPE,      Set.of(Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Short.TYPE,     Set.of(            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Character.TYPE, Set.of(            Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Integer.TYPE,   Set.of(                          Long.TYPE, Float.TYPE, Double.TYPE)),
            Map.entry(Long.TYPE,      Set.of(                                     Float.TYPE, Double.TYPE)),
            Map.entry(Float.TYPE,     Set.of(                                                 Double.TYPE))
    );

    // The boxing/unboxing conversions
    Map<Class, Class> boxingConversions = Map.ofEntries(
            // Boxing: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.7
            Map.entry(Boolean.class,   Boolean.TYPE),
            Map.entry(Byte.class,      Byte.TYPE),
            Map.entry(Short.class,     Short.TYPE),
            Map.entry(Character.class, Character.TYPE),
            Map.entry(Integer.class,   Integer.TYPE),
            Map.entry(Long.class,      Long.TYPE),
            Map.entry(Float.class,     Float.TYPE),
            Map.entry(Double.class,    Double.TYPE),

            // Unboxing: https://docs.oracle.com/javase/specs/jls/se14/html/jls-5.html#jls-5.1.8
            Map.entry(Boolean.TYPE,   Boolean.class),
            Map.entry(Byte.TYPE,      Byte.class),
            Map.entry(Short.TYPE,     Short.class),
            Map.entry(Character.TYPE, Character.class),
            Map.entry(Integer.TYPE,   Integer.class),
            Map.entry(Long.TYPE,      Long.class),
            Map.entry(Float.TYPE,     Float.class),
            Map.entry(Double.TYPE,    Double.class)
    );

    private static class JuggleClassLoader extends URLClassLoader {
        public JuggleClassLoader(URL[] urls) {
            super(urls);
        }

        public Class loadClassWithoutResolving(String className) throws ClassNotFoundException {
            return super.loadClass(className, false);
        }
    }
}
