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

import java.io.IOException;
import java.lang.module.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Module extends Source {
    private static final String CLASS_SUFFIX = ".class";
    private static final String MODULE_INFO  = "module-info";

    private final String moduleName;

    public Module(String name) {
        moduleName = name;
    }

    private final Set<ResolvedModule> mods = new HashSet<>();

    @Override
    public Optional<URL> configure() {
        List<String> modNames = List.of(moduleName);

        Configuration modConf = ModuleLayer.boot().configuration().resolve(
                ModuleFinder.ofSystem(),
                ModuleFinder.of(Path.of(".")),
                modNames);

        addTransitiveModules(modConf, moduleName);

        return Optional.empty();
    }

    private void addTransitiveModules(Configuration modConf, String moduleName) {
        ResolvedModule mod = modConf.findModule(moduleName).orElse(null);

        assert mod != null;

        mods.add(mod);

        // Find transitively required modules ("implied read")

        mod.reference().descriptor().requires().stream()
                .filter(rm -> rm.modifiers().contains(ModuleDescriptor.Requires.Modifier.TRANSITIVE))
                .map(ModuleDescriptor.Requires::name)
                .forEach(name -> addTransitiveModules(modConf, name));
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
                                .flatMap(Optional::stream);
                    }
                    catch (IOException ignored) {}
                    return ret;
                });
    }
}
