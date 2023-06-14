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

import com.angellane.juggle.JuggleError;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.stream.Stream;

public class FileSource extends Source {
    private final static String CLASS_SUFFIX = ".class";
    private final static String MODULE_INFO = "module-info.class";

    private final Path path;

    public FileSource(String s) {
        this.path = Path.of(s);
    }

    @Override
    public List<URL> configure() {
        try {
            if (path.toFile().exists())
                return List.of(path.toUri().toURL());
            else
                throw new JuggleError("Couldn't locate %s".formatted(path));
        }
        catch (MalformedURLException ex) {
            throw new JuggleError(ex.getLocalizedMessage());
        }
    }

    @Override
    public Stream<Class<?>> classStream() {
        File f = path.toFile();
        List<String> entries;

        // Returns stream of class names within a JAR.  Note: these class names might not be valid Java identifiers,
        // especially in the case of inner classes or JAR files generated by something other than the Java compiler.
        try {
            if (f.isFile())
                try (java.util.jar.JarFile file = new java.util.jar.JarFile(f)) {
                    entries = file.stream()
                            .filter(Predicate.not(JarEntry::isDirectory))
                            .map(JarEntry::getName)
                            .toList();
                }
            else if (f.isDirectory())
                try (Stream<Path> stream = Files.walk(path)) {
                    entries = stream
                            .map(path::relativize)
                            .map(Path::toString)
                            .toList();
                }
            else
                throw new JuggleError("Not a file or directory: `%s'".formatted(f));
        }
        catch (IOException ex) {
            throw new JuggleError(ex.getLocalizedMessage());
        }

        return entries.stream()
                .filter(s -> s.endsWith(CLASS_SUFFIX))
                .filter(s -> !s.endsWith(MODULE_INFO))
                .map(s -> s.substring(0, s.length() - CLASS_SUFFIX.length()))
                .map(s -> s.replace('/', '.'))
                .map(n -> getJuggler().loadClassByName(n))
                .flatMap(Optional::stream);
    }
}