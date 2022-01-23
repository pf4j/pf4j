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
import org.pf4j.test.PluginJar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
public class ManifestPluginDescriptorFinderTest {

    private VersionManager versionManager;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() throws IOException {
        Path pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-1"));
        storeManifestToPath(getPlugin1Manifest(), pluginPath);

        pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-2"));
        storeManifestToPath(getPlugin2Manifest(), pluginPath);

        // empty plugin
        Files.createDirectories(pluginsPath.resolve("test-plugin-3"));

        // no plugin class
        pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-4"));
        storeManifestToPath(getPlugin4Manifest(), pluginPath);

        // no plugin version
        pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-5"));
        storeManifestToPath(getPlugin5Manifest(), pluginPath);

        // no plugin id
        pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-6"));
        storeManifestToPath(getPlugin6Manifest(), pluginPath);

        versionManager = new DefaultVersionManager();
    }

    /**
     * Test of {@link ManifestPluginDescriptorFinder#find(Path)} method.
     */
    @Test
    public void testFind() throws Exception {
        PluginDescriptorFinder descriptorFinder = new ManifestPluginDescriptorFinder();

        PluginDescriptor plugin1 = descriptorFinder.find(pluginsPath.resolve("test-plugin-1"));
        PluginDescriptor plugin2 = descriptorFinder.find(pluginsPath.resolve("test-plugin-2"));

        assertEquals("test-plugin-1", plugin1.getPluginId());
        assertEquals("Test Plugin 1", plugin1.getPluginDescription());
        assertEquals("org.pf4j.plugin.TestPlugin", plugin1.getPluginClass());
        assertEquals("0.0.1", plugin1.getVersion());
        assertEquals("Decebal Suiu", plugin1.getProvider());
        assertEquals(2, plugin1.getDependencies().size());
        assertEquals("test-plugin-2", plugin1.getDependencies().get(0).getPluginId());
        assertEquals("test-plugin-3", plugin1.getDependencies().get(1).getPluginId());
        assertEquals("~1.0", plugin1.getDependencies().get(1).getPluginVersionSupport());
        assertEquals("Apache-2.0", plugin1.getLicense());
        assertTrue(versionManager.checkVersionConstraint("1.0.0", plugin1.getRequires()));

        assertEquals("test-plugin-2", plugin2.getPluginId());
        assertEquals("", plugin2.getPluginDescription());
        assertEquals("org.pf4j.plugin.TestPlugin", plugin2.getPluginClass());
        assertEquals("0.0.1", plugin2.getVersion());
        assertEquals("Decebal Suiu", plugin2.getProvider());
        assertEquals(0, plugin2.getDependencies().size());
        assertTrue(versionManager.checkVersionConstraint("1.0.0", plugin2.getRequires()));
    }

    /**
     * Test of {@link ManifestPluginDescriptorFinder#find(Path)} method.
     */
    @Test
    public void testFindNotFound() {
        PluginDescriptorFinder descriptorFinder = new ManifestPluginDescriptorFinder();
        assertThrows(PluginRuntimeException.class, () -> descriptorFinder.find(pluginsPath.resolve("test-plugin-3")));
    }

    private Manifest getPlugin1Manifest() {
        Map<String, String> map = new LinkedHashMap<>(8);
        map.put(ManifestPluginDescriptorFinder.PLUGIN_ID, "test-plugin-1");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_CLASS, "org.pf4j.plugin.TestPlugin");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_VERSION, "0.0.1");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_DESCRIPTION, "Test Plugin 1");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "test-plugin-2,test-plugin-3@~1.0");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_REQUIRES, "*");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_LICENSE, "Apache-2.0");

        return PluginJar.createManifest(map);
    }

    private Manifest getPlugin2Manifest() {
        Map<String, String> map = new LinkedHashMap<>(5);
        map.put(ManifestPluginDescriptorFinder.PLUGIN_ID, "test-plugin-2");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_CLASS, "org.pf4j.plugin.TestPlugin");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_VERSION, "0.0.1");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "");

        return PluginJar.createManifest(map);
    }

    private Manifest getPlugin4Manifest() {
        Map<String, String> map = new LinkedHashMap<>(3);
        map.put(ManifestPluginDescriptorFinder.PLUGIN_ID, "test-plugin-1");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_VERSION, "0.0.1");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");

        return PluginJar.createManifest(map);
    }

    private Manifest getPlugin5Manifest() {
        Map<String, String> map = new LinkedHashMap<>(3);
        map.put(ManifestPluginDescriptorFinder.PLUGIN_ID, "test-plugin-2");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_CLASS, "org.pf4j.plugin.TestPlugin");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");

        return PluginJar.createManifest(map);
    }

    private Manifest getPlugin6Manifest() {
        Map<String, String> map = new LinkedHashMap<>(2);
        map.put(ManifestPluginDescriptorFinder.PLUGIN_CLASS, "org.pf4j.plugin.TestPlugin");
        map.put(ManifestPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");

        return PluginJar.createManifest(map);
    }

    private void storeManifestToPath(Manifest manifest, Path pluginPath) throws IOException {
        Path path = Files.createDirectory(pluginPath.resolve("META-INF"));
        try (OutputStream output = Files.newOutputStream(path.resolve("MANIFEST.MF"))) {
            manifest.write(output);
        }
    }

}
