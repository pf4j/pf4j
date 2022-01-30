/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.test.PluginJar;
import org.pf4j.test.PluginManifest;
import org.pf4j.test.PluginProperties;
import org.pf4j.test.TestPlugin;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Decebal Suiu
 */
class CompoundPluginDescriptorFinderTest {

    @TempDir
    Path pluginsPath;

    @Test
    void add() {
        CompoundPluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder();
        assertEquals(0, descriptorFinder.size());

        descriptorFinder.add(new PropertiesPluginDescriptorFinder());
        assertEquals(1, descriptorFinder.size());
    }

    @Test
    void find() throws Exception {
        Path pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-1"));
        storePropertiesToPath(createPropertiesPlugin1(), pluginPath);

        PluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder()
            .add(new PropertiesPluginDescriptorFinder());

        PluginDescriptor pluginDescriptor = descriptorFinder.find(pluginPath);
        assertNotNull(pluginDescriptor);
        assertEquals("test-plugin-1", pluginDescriptor.getPluginId());
        assertEquals("0.0.1", pluginDescriptor.getVersion());
    }

    @Test
    void findInJar() throws Exception {
        PluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder()
            .add(new ManifestPluginDescriptorFinder());

        PluginManifest pluginManifest = new PluginManifest.Builder("myPlugin")
            .pluginClass(TestPlugin.class.getName())
            .pluginVersion("1.2.3")
            .build();
        PluginJar pluginJar = new PluginJar.Builder(pluginsPath.resolve("my-plugin-1.2.3.jar"), pluginManifest)
            .build();

        PluginDescriptor pluginDescriptor = descriptorFinder.find(pluginJar.path());
        assertNotNull(pluginDescriptor);
        assertEquals("myPlugin", pluginJar.pluginId());
        assertEquals(TestPlugin.class.getName(), pluginJar.pluginClass());
        assertEquals("1.2.3", pluginJar.pluginVersion());
    }

    @Test
    void notFound() {
        PluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder();
        Path pluginPath = pluginsPath.resolve("test-plugin-3");
        assertThrows(PluginRuntimeException.class, () -> descriptorFinder.find(pluginPath));
    }

    @Test
    void spaceCharacterInFileName() throws Exception {
        PluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder()
            .add(new ManifestPluginDescriptorFinder());

        PluginManifest pluginManifest = new PluginManifest.Builder("myPlugin")
            .pluginVersion("1.2.3")
            .build();
        PluginJar pluginJar = new PluginJar.Builder(pluginsPath.resolve("my plugin-1.2.3.jar"), pluginManifest)
            .build();

        PluginDescriptor pluginDescriptor = descriptorFinder.find(pluginJar.path());
        assertNotNull(pluginDescriptor);
    }

    private Properties createPropertiesPlugin1() {
        return new PluginProperties.Builder("test-plugin-1")
            .pluginClass(TestPlugin.class.getName())
            .pluginVersion("0.0.1")
            .pluginProvider("Decebal Suiu")
            .pluginDependencies(Arrays.asList("test-plugin-2", "test-plugin-3@~1.0"))
            .pluginRequires(">=1")
            .pluginLicense("Apache-2.0")
            .build()
            .properties();
    }

    private void storePropertiesToPath(Properties properties, Path pluginPath) throws IOException {
        Path path = pluginPath.resolve(PropertiesPluginDescriptorFinder.DEFAULT_PROPERTIES_FILE_NAME);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            properties.store(writer, "");
        }
    }

}
