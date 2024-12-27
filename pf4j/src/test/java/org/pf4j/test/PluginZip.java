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

import org.pf4j.PropertiesPluginDescriptorFinder;
import org.pf4j.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Represents a plugin {@code zip} file.
 * The {@code plugin.properties} file is created on the fly from the information supplied in {@link Builder}.
 *
 * @author Decebal Suiu
 */
public class PluginZip {

    private final Path path;
    private final String pluginId;
    private final String pluginClass;
    private final String pluginVersion;
    private final String pluginDependencies;

    protected PluginZip(Builder builder) {
        this.path = builder.path;
        this.pluginId = builder.pluginId;
        this.pluginClass = builder.pluginClass;
        this.pluginVersion = builder.pluginVersion;
        this.pluginDependencies = builder.pluginDependencies;
    }

    /**
     * Returns the path of the {@code zip} file.
     */
    public Path path() {
        return path;
    }

    /**
     * Returns the {@code zip} file.
     */
    public File file() {
        return path.toFile();
    }

    /**
     * Returns the plugin id.
     */
    public String pluginId() {
        return pluginId;
    }

    /**
     * Returns the plugin class.
     */
    public String pluginClass() {
        return pluginClass;
    }

    /**
     * Returns the plugin version.
     */
    public String pluginVersion() {
        return pluginVersion;
    }

    /**
     * Returns the plugin dependencies.
     */
    public String pluginDependencies() { return pluginDependencies; }

    /**
     * Returns the path where the {@code zip} file will be unzipped.
     */
    public Path unzippedPath() {
        Path zipPath = path();
        String fileName = zipPath.getFileName().toString();

        return zipPath.getParent().resolve(fileName.substring(0, fileName.length() - 4)); // without ".zip" suffix
    }

    /**
     * Unzips the {@code zip} file.
     */
    public Path unzip() throws IOException {
        return FileUtils.expandIfZip(path());
    }

    /**
     * Builder for {@link PluginZip}.
     * The {@code plugin.properties} file is created on the fly from the information supplied in this builder.
     * The {@code plugin.properties} file is created in the root of the {@code zip} file.
     * The {@code zip} file can contain extra files.
     */
    public static class Builder {

        private final Path path;
        private final String pluginId;
        private final Map<String, String> properties = new LinkedHashMap<>();
        private final Map<Path, byte[]> files = new LinkedHashMap<>();

        private String pluginClass;
        private String pluginVersion;
        private String pluginDependencies;

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

        public Builder pluginDependencies(String pluginDependencies) {
            this.pluginDependencies = pluginDependencies;

            return this;
        }

        /**
         * Add extra properties to the {@code properties} file.
         * As possible attribute name please see {@link PropertiesPluginDescriptorFinder}.
         */
        public Builder properties(Map<String, String> properties) {
            this.properties.putAll(properties);

            return this;
        }

        /**
         * Add extra property to the {@code properties} file.
         * As possible property name please see {@link PropertiesPluginDescriptorFinder}.
         */
        public Builder property(String name, String value) {
            properties.put(name, value);

            return this;
        }

        /**
         * Adds a file to the archive.
         *
         * @param path the relative path of the file
         * @param content the content of the file
         */
        public Builder addFile(Path path, byte[] content) {
            files.put(path, content.clone());

            return this;
        }

        /**
         * Adds a file to the archive.
         *
         * @param path the relative path of the file
         * @param content the content of the file
         */
        public Builder addFile(Path path, String content) {
            files.put(path, content.getBytes());

            return this;
        }

        /**
         * Builds the {@link PluginZip} instance.
         */
        public PluginZip build() throws IOException {
            try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(path.toFile()))) {
                ZipEntry propertiesFile = new ZipEntry(PropertiesPluginDescriptorFinder.DEFAULT_PROPERTIES_FILE_NAME);
                outputStream.putNextEntry(propertiesFile);
                createProperties().store(outputStream, "");
                outputStream.closeEntry();

                for (Map.Entry<Path, byte[]> fileEntry : files.entrySet()) {
                    ZipEntry file = new ZipEntry(fileEntry.getKey().toString());
                    outputStream.putNextEntry(file);
                    outputStream.write(fileEntry.getValue());
                    outputStream.closeEntry();
                }
            }

            return new PluginZip(this);
        }

        private Properties createProperties() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(PropertiesPluginDescriptorFinder.PLUGIN_ID, pluginId);
            map.put(PropertiesPluginDescriptorFinder.PLUGIN_VERSION, pluginVersion);
            if (pluginDependencies != null) {
                map.put(PropertiesPluginDescriptorFinder.PLUGIN_DEPENDENCIES, pluginDependencies);
            }
            if (pluginClass != null) {
                map.put(PropertiesPluginDescriptorFinder.PLUGIN_CLASS, pluginClass);
            }

            map.putAll(properties);

            return PropertiesUtils.createProperties(map);
        }

    }

}
