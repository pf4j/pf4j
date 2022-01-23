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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.test.PluginZip;
import org.pf4j.test.TestPlugin;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertiesPluginDescriptorFinderTest {

    private VersionManager versionManager;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() throws IOException {
        Path pluginPath = Files.createDirectory(pluginsPath.resolve("test-plugin-1"));
        storePropertiesToPath(getPlugin1Properties(), pluginPath);

        pluginPath = Files.createDirectory(pluginsPath.resolve("test-plugin-2"));
        storePropertiesToPath(getPlugin2Properties(), pluginPath);

        // empty plugin
        Files.createDirectories(pluginsPath.resolve("test-plugin-3"));

        // no plugin class
        pluginPath = Files.createDirectory(pluginsPath.resolve("test-plugin-4"));
        storePropertiesToPath(getPlugin4Properties(), pluginPath);

        // no plugin version
        pluginPath = Files.createDirectory(pluginsPath.resolve("test-plugin-5"));
        storePropertiesToPath(getPlugin5Properties(), pluginPath);

        // no plugin id
        pluginPath = Files.createDirectory(pluginsPath.resolve("test-plugin-6"));
        storePropertiesToPath(getPlugin6Properties(), pluginPath);

        versionManager = new DefaultVersionManager();
    }

    @Test
    public void testFind() throws Exception {
        PluginDescriptorFinder descriptorFinder = new PropertiesPluginDescriptorFinder();

        PluginDescriptor plugin1 = descriptorFinder.find(pluginsPath.resolve("test-plugin-1"));
        PluginDescriptor plugin2 = descriptorFinder.find(pluginsPath.resolve("test-plugin-2"));

        assertEquals("test-plugin-1", plugin1.getPluginId());
        assertEquals("Test Plugin 1", plugin1.getPluginDescription());
        assertEquals(TestPlugin.class.getName(), plugin1.getPluginClass());
        assertEquals("0.0.1", plugin1.getVersion());
        assertEquals("Decebal Suiu", plugin1.getProvider());
        assertEquals(2, plugin1.getDependencies().size());
        assertEquals("test-plugin-2", plugin1.getDependencies().get(0).getPluginId());
        assertEquals("test-plugin-3", plugin1.getDependencies().get(1).getPluginId());
        assertEquals("~1.0", plugin1.getDependencies().get(1).getPluginVersionSupport());
        assertEquals("Apache-2.0", plugin1.getLicense());
        assertEquals(">=1", plugin1.getRequires());
        assertTrue(versionManager.checkVersionConstraint("1.0.0", plugin1.getRequires()));
        assertFalse(versionManager.checkVersionConstraint("0.1.0", plugin1.getRequires()));

        assertEquals("test-plugin-2", plugin2.getPluginId());
        assertEquals("", plugin2.getPluginDescription());
        assertEquals(TestPlugin.class.getName(), plugin2.getPluginClass());
        assertEquals("0.0.1", plugin2.getVersion());
        assertEquals("Decebal Suiu", plugin2.getProvider());
        assertEquals(0, plugin2.getDependencies().size());
        assertEquals("*", plugin2.getRequires()); // Default is *
        assertTrue(versionManager.checkVersionConstraint("1.0.0", plugin2.getRequires()));
    }

    @Test
    public void testNotFound() {
        PluginDescriptorFinder descriptorFinder = new PropertiesPluginDescriptorFinder();
        assertThrows(PluginRuntimeException.class, () -> descriptorFinder.find(pluginsPath.resolve("test-plugin-3")));
    }

    private Properties getPlugin1Properties() {
        Map<String, String> map = new LinkedHashMap<>(8);
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_ID, "test-plugin-1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_CLASS, TestPlugin.class.getName());
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_VERSION, "0.0.1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_DESCRIPTION, "Test Plugin 1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "test-plugin-2,test-plugin-3@~1.0");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_REQUIRES, ">=1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_LICENSE, "Apache-2.0");

        return PluginZip.createProperties(map);
    }

    private Properties getPlugin2Properties() {
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_ID, "test-plugin-2");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_CLASS, TestPlugin.class.getName());
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_VERSION, "0.0.1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "");

        return PluginZip.createProperties(map);
    }

    private Properties getPlugin4Properties() {
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_ID, "test-plugin-2");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_VERSION, "0.0.1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_REQUIRES, "*");

        return PluginZip.createProperties(map);
    }

    private Properties getPlugin5Properties() {
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_ID, "test-plugin-2");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_CLASS, TestPlugin.class.getName());
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_REQUIRES, "*");

        return PluginZip.createProperties(map);
    }

    private Properties getPlugin6Properties() {
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_CLASS, TestPlugin.class.getName());
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_VERSION, "0.0.1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_REQUIRES, "*");

        return PluginZip.createProperties(map);
    }

    private void storePropertiesToPath(Properties properties, Path pluginPath) throws IOException {
        Path path = pluginPath.resolve(PropertiesPluginDescriptorFinder.DEFAULT_PROPERTIES_FILE_NAME);
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            properties.store(writer, "");
        }
    }

}
