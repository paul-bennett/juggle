package com.angellane.juggle.source;

import java.io.IOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReader;
import java.lang.module.ResolvedModule;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Module extends Source {
    private static final String CLASS_SUFFIX = ".class";
    private static final String MODULE_INFO  = "module-info";

    private final String moduleName;

    private Configuration modConf;

    public Module(String name) {
        moduleName = name;
    }

    @Override
    public Optional<URL> configure() {
        List<String> modNames = List.of(moduleName);

        this.modConf = ModuleLayer.boot().configuration().resolve(
                ModuleFinder.ofSystem(),
                ModuleFinder.of(Path.of(".")),
                modNames);

        return Optional.empty();
    }

    @Override
    public Stream<Class<?>> classStream() {
        Optional<ResolvedModule> maybeMod = this.modConf.findModule(moduleName);

        if (maybeMod.isEmpty())
            System.err.println("Warning: couldn't find module " + moduleName);
        else {
            try (ModuleReader reader = maybeMod.get().reference().open()) {
                return reader.list()
                        .filter(s -> s.endsWith(CLASS_SUFFIX))
                        .map(s -> s.substring(0, s.length() - CLASS_SUFFIX.length()))
                        .filter(s -> !s.equals(MODULE_INFO))
                        .map(s -> s.replace('/', '.'))
                        .map(n -> getJuggler().loadClassByName(n))
                        .flatMap(Optional::stream);
            }
            catch (IOException e) {
                System.err.println("Warning: error opening module " + moduleName);
            }
        }
        return Stream.empty();
    }
}
