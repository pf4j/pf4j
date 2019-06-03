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
package org.pf4j.plugin;

import org.pf4j.ManifestPluginDescriptorFinder;

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
import java.util.jar.Attributes;
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

    public Path path() {
        return path;
    }

    public File file() {
        return path.toFile();
    }

    public String pluginClass() {
        return pluginClass;
    }

    public String pluginId() {
        return pluginId;
    }

    public String pluginVersion() {
        return pluginVersion;
    }

    public static Manifest createManifest(Map<String, String> map) {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            attributes.put(new Attributes.Name(entry.getKey()), entry.getValue());
        }

        return manifest;
    }

    public static class Builder {

        private final Path path;
        private final String pluginId;

        private String pluginClass;
        private String pluginVersion;
        private Map<String, String> manifestAttributes = new LinkedHashMap<>();
        private Set<String> extensions = new LinkedHashSet<>();
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

        public PluginJar build() throws IOException {
            Manifest manifest = createManifest();
            try (OutputStream outputStream = new FileOutputStream(path.toFile())) {
                JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest);
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
                jarOutputStream.close();
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
            if (manifestAttributes != null) {
                map.putAll(manifestAttributes);
            }

            return PluginJar.createManifest(map);
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
