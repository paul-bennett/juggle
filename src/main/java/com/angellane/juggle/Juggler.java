package com.angellane.juggle;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ResolvedModule;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.List.of;

public class Juggler {
    private ClassLoader loader;
    private ModuleLayer layer;

    private Collection<Class<?>> classesToSearch;

    public Juggler(List<String> jars, List<String> mods) {
        URL[] urls = jars.stream()
                .flatMap(path -> {
                    try {
                        return Stream.of(Paths.get(path).toUri().toURL());
                    } catch (MalformedURLException ex) {
                        return Stream.empty();
                    }
                })
                .toArray(URL[]::new);

        loader = new URLClassLoader(urls);

        var boot = ModuleLayer.boot();
        ModuleFinder finder = ModuleFinder.ofSystem();
        Configuration conf = boot.configuration().resolve(
                ModuleFinder.ofSystem(),
                ModuleFinder.of(Path.of(".")),
                mods);

        layer = boot.defineModulesWithOneLoader(conf, loader);

        var moduleClassesStream = mods.stream()
                .flatMap(s -> classesForModule(conf, s).stream());

        var baseClassesStream = classesForModule(conf, "java.base").stream();

        var jarClassesStream = jars.stream()
          .flatMap(jarName -> classesInJar(jarName).stream())
          .flatMap(className -> {
              try {
                  return Stream.of(loader.loadClass(className));
              }
              catch (ClassNotFoundException ex) {
                  System.err.println("Warning: class " + className + " not found");
                  return Stream.empty();
              }
          });

        classesToSearch =
                Stream.concat(baseClassesStream,
                        Stream.concat(moduleClassesStream,
                                jarClassesStream))
          .collect(Collectors.toList());
    }

    private static final String CLASS_SUFFIX = ".class";
    private static final String MODULE_INFO  = "module-info";

    // Returns list of class names within a JAR.  Note: these class names might not be valid Java identifiers,
    // especially in the case of inner classes or JAR files generated by something other than the Java compiler.
    public List<String> classesInJar(String filename) {
        try (JarFile file = new JarFile(filename)) {
            return file.stream()
                    .filter(Predicate.not(JarEntry::isDirectory))
                    .map(JarEntry::getName)
                    .filter(s -> s.endsWith(CLASS_SUFFIX))
                    .map(s -> s.substring(0, s.length() - CLASS_SUFFIX.length()))
                    .map(s -> s.replace('/', '.'))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Couldn't read JAR file: " + filename + "; ignoring.");
            return Collections.emptyList();
        }
    }

    public List<Class<?>> classesForModule(Configuration conf, String moduleName) {
        Optional<ResolvedModule> maybeMod = conf.findModule(moduleName);

        if (maybeMod.isEmpty())
            System.err.println("Warning: couldn't find module " + moduleName);
        else {
            ResolvedModule mod = maybeMod.get();

            try {
                return mod.reference().open().list()
                        .filter(s -> s.endsWith(CLASS_SUFFIX))
                        .map(s -> s.substring(0, s.length() - CLASS_SUFFIX.length()))
                        .filter(s -> !s.equals(MODULE_INFO))
                        .map(s -> s.replace('/', '.'))
                        .map(s -> {
                            try { return loader.loadClass(s); }
                            catch (ClassNotFoundException x) {
                                System.err.println("Couldn't load: " + s);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            catch (IOException e) {
                System.err.println("Warning: error opening module " + moduleName);
            }
        }
        return List.of();
    }

    public Class<?> classForTypename(String[] imports, String typename) {
        final String ARRAY_SUFFIX = "[]";

        // If this is an array, work out how many dimensions are involved.
        int arrayDimension;
        for (arrayDimension = 0; typename.endsWith(ARRAY_SUFFIX); ++arrayDimension)
            typename = typename.substring(0, typename.length() - ARRAY_SUFFIX.length()).stripTrailing();

        // TODO: think about Generics

        // Start with the base type
        Class<?> ret = null;
        switch (typename) {
            case "void":        ret = Void.TYPE;        break;
            case "boolean":     ret = Boolean.TYPE;     break;
            case "char":        ret = Character.TYPE;   break;
            case "byte":        ret = Byte.TYPE;        break;
            case "short":       ret = Short.TYPE;       break;
            case "int":         ret = Integer.TYPE;     break;
            case "long":        ret = Long.TYPE;        break;
            case "float":       ret = Float.TYPE;       break;
            case "double":      ret = Double.TYPE;      break;
            default:
                // Actually now want to try typename plainly, then prefixed by each import in turn
                // Default to Object if we can't find any match
                for (var prefix : Stream.concat(Stream.of(""),
                        Arrays.stream(imports).map(i -> i + ".")).collect(Collectors.toList()))
                    try {
                        ret = loader.loadClass(prefix + typename);
                        break;
                    } catch (ClassNotFoundException e) { /* try next iter */ }

                if (ret == null) {
                    // If we get here, the class wasn't found, either naked or with any imported package prefix
                    Class<?> defaultType = Object.class;

                    System.err.println("Warning: couldn't find class: " + typename
                            + "; using " + defaultType + " instead");
                    ret = defaultType;
                }
        }

        // Now add the array dimension
        for ( ; arrayDimension > 0; --arrayDimension)
            ret = ret.arrayType();

        return ret;
    }

    public Member[] findMembers(String[] imports, String[] paramTypenames, String returnTypename) {
        Class<?>[] paramTypes = Arrays.stream(paramTypenames == null ? new String[0] : paramTypenames)
                .map(typename -> classForTypename(imports, typename))
                .toArray(Class<?>[]::new);
        Class<?> returnType = returnTypename == null ? Void.TYPE : classForTypename(imports, returnTypename);

        return findMembers(of(paramTypes), returnType);
    }

    public Member[] findMembers(List<Class<?>> queryParamTypes, Class<?> queryReturnType) {
        // Fields
        Stream<CandidateMember> fieldStream = classesToSearch.stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredFields())
                        .map(CandidateMember::membersFromField)
                        .flatMap(List::stream));

        // Constructors are like static methods returning an item of their declaring class
        Stream<CandidateMember> ctorStream = classesToSearch.stream()
                .flatMap(cls -> Arrays.stream(cls.getDeclaredConstructors())
                        .map(CandidateMember::memberFromConstructor));

        // Methods
        Stream<CandidateMember> methodStream = classesToSearch.stream()
                .flatMap(c -> {
                    try {
                        return Arrays.stream(c.getDeclaredMethods())
                                .map(CandidateMember::memberFromMethod);
                    } catch (NoClassDefFoundError e) {
                        // TODO: consider whether this is still necessary.  If it is, how can it be factored out?
                        // This might be thrown if the class file references other classes that can't be loaded.
                        // Maybe it depends on another JAR that hasn't been specified on the command-line with -j.
                        System.err.println("*** Ignoring class " + c + ": " + e);
                        return Stream.empty();
                    }
                });

        return Stream.concat(fieldStream, Stream.concat(ctorStream, methodStream))  // TODO: more elegant concat
                .filter(m -> m.matches(queryParamTypes, queryReturnType))
                .map(CandidateMember::getMember)
                .distinct()
                // TODO: access filtering here
                .toArray(Member[]::new);
    }
}
