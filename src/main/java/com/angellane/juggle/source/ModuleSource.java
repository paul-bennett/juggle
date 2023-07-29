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
package com.angellane.juggle.source;

import com.angellane.backport.jdk11.java.lang.ClassExtras;
import com.angellane.backport.jdk11.java.lang.module.Configuration;
import com.angellane.backport.jdk11.java.lang.module.ModuleDescriptor;
import com.angellane.backport.jdk11.java.lang.module.ModuleFinder;
import com.angellane.backport.jdk11.java.lang.ModuleLayer;
import com.angellane.backport.jdk11.java.lang.module.ModuleReader;
import com.angellane.backport.jdk11.java.lang.module.ResolvedModule;
import com.angellane.backport.jdk11.java.nio.PathExtras;
import com.angellane.backport.jdk11.java.util.OptionalExtras;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModuleSource extends Source {
    private static final String CLASS_SUFFIX = ".class";
    private static final String MODULE_INFO  = "module-info";

    private final List<String> modulePaths;
    private final String moduleName;

    public ModuleSource() {
        this.modulePaths = Collections.emptyList();
        this.moduleName = ClassExtras.getModule(Object.class).getName();
    }

    public ModuleSource(List<String> modulePaths, String name) {
        this.modulePaths = modulePaths;
        this.moduleName = name;
    }

    private final Set<ResolvedModule> mods = new HashSet<>();

    @Override
    public List<URL> configure() {
        List<String> modNames = Collections.singletonList(moduleName);

        Path[] paths = modulePaths.stream()
                .map(PathExtras::of)
                .toArray(Path[]::new);

        Configuration modConf = ModuleLayer.boot().configuration().resolve(
                ModuleFinder.ofSystem(),
                ModuleFinder.of(paths),
                modNames);

        return addTransitiveModules(modConf, moduleName).stream()
                .flatMap(u -> {
                    try {
                        return Stream.of(u.toURL());
                    } catch (MalformedURLException ignored) {
                        return Stream.of();
                    }
                })
                .collect(Collectors.toList());
    }

    private List<URI> addTransitiveModules(Configuration modConf, String moduleName) {
        List<URI> ret = new ArrayList<>();

        ResolvedModule mod = modConf.findModule(moduleName).orElse(null);

        assert mod != null;

        mods.add(mod);
        mod.reference().location().ifPresent(ret::add);

        // Find transitively required modules ("implied read")

        mod.reference().descriptor().requires().stream()
                .filter(rm -> rm.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE))
                .map(ModuleDescriptor.Requires::name)
                .forEach(name -> ret.addAll(
                        addTransitiveModules(modConf, name)
                        )
                );

        return ret;
    }

    @Override
    public Stream<Class<?>> classStream() {
        return mods.stream()
                .flatMap(mod -> {
                    Stream<Class<?>> ret = Stream.empty();
                    try (ModuleReader reader = mod.reference().open()) {
                        ret = reader.list()
                                .filter(s -> s.endsWith(CLASS_SUFFIX))
                                .map(s -> s.substring(0, s.length() - CLASS_SUFFIX.length()))
                                .filter(s -> !s.equals(MODULE_INFO))
                                .map(s -> s.replace('/', '.'))
                                .map(n -> getJuggler().loadClassByName(n))
                                .flatMap(OptionalExtras::stream)
                                // Ignore classes in packages that aren't exported
                                .filter(c -> ClassExtras.getModule(c).isExported(c.getPackage().getName()))
                        ;
                    }
                    catch (IOException ignored) {}
                    return ret;
                });
    }
}
