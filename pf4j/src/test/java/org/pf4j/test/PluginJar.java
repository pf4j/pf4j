/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Represents a plugin {@code jar} file.
 * The {@code MANIFEST.MF} file is created on the fly from the information supplied in {@link Builder}.
 *
 * @author Decebal Suiu
 */
public class PluginJar {

    private final Path path;
    private final PluginManifest manifest;

    protected PluginJar(Builder builder) {
        this.path = builder.path;
        this.manifest = builder.manifest;
    }

    public Path path() {
        return path;
    }

    public PluginManifest manifest() {
        return manifest;
    }

    public String pluginClass() {
        return manifest.pluginClass();
    }

    public String pluginId() {
        return manifest.pluginId();
    }

    public String pluginVersion() {
        return manifest.pluginVersion();
    }

    public static class Builder {

        private final Path path;
        private final PluginManifest manifest;

        private Set<String> extensions = new LinkedHashSet<>();
        private ClassDataProvider classDataProvider = new DefaultClassDataProvider();

        public Builder(Path path, PluginManifest manifest) {
            this.path = path;
            this.manifest = manifest;
        }

        public Builder extension(String extensionClassName) {
            extensions.add(extensionClassName);

            return this;
        }

        public Builder classDataProvider(ClassDataProvider classDataProvider) {
             this.classDataProvider = classDataProvider;

             return this;
        }

        public PluginJar build() throws IOException {
            try (OutputStream outputStream = Files.newOutputStream(path);
                 JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest.manifest())) {
                if (!extensions.isEmpty()) {
                    // add extensions.idx
                    JarEntry jarEntry = new JarEntry("META-INF/extensions.idx");
                    jarOutputStream.putNextEntry(jarEntry);
                    jarOutputStream.write(extensionsAsByteArray());
                    jarOutputStream.closeEntry();
                    // add extensions classes
                    for (String extension : extensions) {
                        String extensionPath = extension.replace('.', '/') + ".class";
                        JarEntry classEntry = new JarEntry(extensionPath);
                        jarOutputStream.putNextEntry(classEntry);
                        jarOutputStream.write(classDataProvider.getClassData(extension));
                        jarOutputStream.closeEntry();
                    }
                }
            }

            return new PluginJar(this);
        }

        private byte[] extensionsAsByteArray() throws IOException {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PrintWriter writer = new PrintWriter(outputStream);
                for (String extension : extensions) {
                    writer.println(extension);
                }
                writer.flush();

                return outputStream.toByteArray();
            }
        }

    }

}
