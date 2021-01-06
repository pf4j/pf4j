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

    protected PluginZip(Builder builder) {
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

    public String pluginId() {
        return pluginId;
    }

    public String pluginClass() {
        return pluginClass;
    }

    public String pluginVersion() {
        return pluginVersion;
    }

    public Path unzippedPath() {
        Path path = path();
        String fileName = path.getFileName().toString();

        return path.getParent().resolve(fileName.substring(0, fileName.length() - 4)); // without ".zip" suffix
    }

    public static Properties createProperties(Map<String, String> map) {
        Properties properties = new Properties();
        properties.putAll(map);

        return properties;
    }

    public static class Builder {

        private final Path path;
        private final String pluginId;

        private String pluginClass;
        private String pluginVersion;
        private Map<String, String> properties = new LinkedHashMap<>();
        private Map<Path, byte[]> files = new LinkedHashMap<>();

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

        public PluginZip build() throws IOException {
            createPropertiesFile();

            return new PluginZip(this);
        }

        protected void createPropertiesFile() throws IOException {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(PropertiesPluginDescriptorFinder.PLUGIN_ID, pluginId);
            map.put(PropertiesPluginDescriptorFinder.PLUGIN_VERSION, pluginVersion);
            if (pluginClass != null) {
                map.put(PropertiesPluginDescriptorFinder.PLUGIN_CLASS, pluginClass);
            }
            if (properties != null) {
                map.putAll(properties);
            }

            try (ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(path.toFile()))) {
                ZipEntry propertiesFile = new ZipEntry(PropertiesPluginDescriptorFinder.DEFAULT_PROPERTIES_FILE_NAME);
                outputStream.putNextEntry(propertiesFile);
                createProperties(map).store(outputStream, "");
                outputStream.closeEntry();

                for (Map.Entry<Path, byte[]> fileEntry : files.entrySet()) {
                    ZipEntry file = new ZipEntry(fileEntry.getKey().toString());
                    outputStream.putNextEntry(file);
                    outputStream.write(fileEntry.getValue());
                    outputStream.closeEntry();
                }
            }
        }

    }

}
