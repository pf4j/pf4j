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

import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.processor.LegacyExtensionStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Represents a plugin {@code jar} file.
 * The {@code MANIFEST.MF} file is created on the fly from the information supplied in {@link Builder}.
 *
 * @author Decebal Suiu
 */
public class PluginJar {

    private final Path path;
    private final String pluginId;
    private final String pluginClass;
    private final String pluginVersion;

    protected PluginJar(Builder builder) {
        this.path = builder.path;
        this.pluginId = builder.pluginId;
        this.pluginClass = builder.pluginClass;
        this.pluginVersion = builder.pluginVersion;
    }

    /**
     * Returns the {@code jar} file path.
     */
    public Path path() {
        return path;
    }

    /**
     * Returns the {@code jar} file.
     */
    public File file() {
        return path.toFile();
    }

    /**
     * Returns the plugin class.
     */
    public String pluginClass() {
        return pluginClass;
    }

    /**
     * Returns the plugin id.
     */
    public String pluginId() {
        return pluginId;
    }

    /**
     * Returns the plugin version.
     */
    public String pluginVersion() {
        return pluginVersion;
    }

    public static class Builder {

        private final Path path;
        private final String pluginId;
        private final Map<String, String> manifestAttributes = new LinkedHashMap<>();
        private final Set<String> extensions = new LinkedHashSet<>();

        private String pluginClass;
        private String pluginVersion;
        private ClassDataProvider classDataProvider = new DefaultClassDataProvider();

        public Builder(Path path, String pluginId) {
            this.path = path;
            this.pluginId = pluginId;
        }

        public Builder pluginClass(String pluginClass) {
            this.pluginClass = pluginClass;

            return this;
        }

        public Builder pluginVersion(String pluginVersion) {
            this.pluginVersion = pluginVersion;

            return this;
        }

        /**
         * Add extra attributes to the {@code manifest} file.
         * As possible attribute name please see {@link ManifestPluginDescriptorFinder}.
         */
        public Builder manifestAttributes(Map<String, String> manifestAttributes) {
            this.manifestAttributes.putAll(manifestAttributes);

            return this;
        }

        /**
         * Add extra attribute to the {@code manifest} file.
         * As possible attribute name please see {@link ManifestPluginDescriptorFinder}.
         */
        public Builder manifestAttribute(String name, String value) {
            manifestAttributes.put(name, value);

            return this;
        }

        public Builder extension(String extensionClassName) {
            extensions.add(extensionClassName);

            return this;
        }

        public Builder classDataProvider(ClassDataProvider classDataProvider) {
             this.classDataProvider = classDataProvider;

             return this;
        }

        /**
         * Builds the {@link PluginJar} instance.
         */
        public PluginJar build() throws IOException {
            try (OutputStream outputStream = new FileOutputStream(path.toFile());
                JarOutputStream jarOutputStream = new JarOutputStream(outputStream, createManifest())) {
                if (!extensions.isEmpty()) {
                    // add extensions.idx
                    JarEntry jarEntry = new JarEntry(LegacyExtensionStorage.EXTENSIONS_RESOURCE);
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

        private Manifest createManifest() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(ManifestPluginDescriptorFinder.PLUGIN_ID, pluginId);
            map.put(ManifestPluginDescriptorFinder.PLUGIN_VERSION, pluginVersion);
            if (pluginClass != null) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_CLASS, pluginClass);
            }
            map.putAll(manifestAttributes);

            return ManifestUtils.createManifest(map);
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
