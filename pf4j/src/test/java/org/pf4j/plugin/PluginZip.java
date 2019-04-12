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

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
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
    private final String pluginVersion;

    protected PluginZip(Builder builder) {
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

    public Path unzippedPath() {
        Path path = path();
        String fileName = path.getFileName().toString();

        return path.getParent().resolve(fileName.substring(0, fileName.length() - 4)); // without ".zip" suffix
    }

    public static class Builder {

        private final Path path;
        private final String pluginId;

        private String pluginVersion;
        private Map<String, String> properties;

        public Builder(Path path, String pluginId) {
            this.path = path;
            this.pluginId = pluginId;
        }

        public Builder pluginVersion(String pluginVersion) {
            this.pluginVersion = pluginVersion;

            return this;
        }

        /**
         * Add extra properties to the {@code properties} file.
         */
        public Builder properties(Map<String, String> properties) {
            this.properties = properties;

            return this;
        }

        public PluginZip build() throws IOException {
            createPropertiesFile();

            return new PluginZip(this);
        }

        protected void createPropertiesFile() throws IOException {
            Properties props = new Properties();
            props.setProperty("plugin.id", pluginId);
            props.setProperty("plugin.version", pluginVersion);
            props.setProperty("plugin.class", "org.pf4j.plugin.TestPlugin");
            if (properties != null) {
                props.putAll(properties);
            }

            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(path.toFile()));
            ZipEntry propertiesFile = new ZipEntry("plugin.properties");
            outputStream.putNextEntry(propertiesFile);
            props.store(outputStream, "");
            outputStream.closeEntry();
            outputStream.close();
        }

    }

}
