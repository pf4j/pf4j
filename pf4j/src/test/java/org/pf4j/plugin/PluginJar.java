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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 /**
 * Represents a plugin {@code jar} file.
 * The {@code MANIFEST.MF} file is created on the fly from the information supplied in {@link Builder}.
 *
 * @author Decebal Suiu
 */
public class PluginJar {

    private final Path path;
    private final String pluginId;
    private final String pluginVersion;

    protected PluginJar(Builder builder) {
        this.path = builder.path;
        this.pluginId = builder.pluginId;
        this.pluginVersion = builder.pluginVersion;
    }

    public Path path() {
        return path;
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

        private String pluginVersion;
        private Map<String, String> attributes;

        public Builder(Path path, String pluginId) {
            this.path = path;
            this.pluginId = pluginId;
        }

        public Builder pluginVersion(String pluginVersion) {
            this.pluginVersion = pluginVersion;

            return this;
        }

        /**
         * Add extra attributes to the {@code manifest} file.
         */
        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;

            return this;
        }

        public PluginJar build() throws IOException {
            createManifestFile();

            return new PluginJar(this);
        }

        protected void createManifestFile() throws IOException {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(ManifestPluginDescriptorFinder.PLUGIN_ID, pluginId);
            map.put(ManifestPluginDescriptorFinder.PLUGIN_VERSION, pluginVersion);
            map.put(ManifestPluginDescriptorFinder.PLUGIN_CLASS, "org.pf4j.plugin.TestPlugin");
            if (attributes != null) {
                map.putAll(attributes);
            }

            JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(path.toFile()), createManifest(map));
            outputStream.close();
        }

    }

}
