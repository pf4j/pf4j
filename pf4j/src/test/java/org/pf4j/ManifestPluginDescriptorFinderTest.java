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
import org.pf4j.test.PluginManifest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
class ManifestPluginDescriptorFinderTest {

    private VersionManager versionManager;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    void setUp() throws IOException {
        Path pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-1"));
        storeManifestToPath(createManifestPlugin1(), pluginPath);

        pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-2"));
        storeManifestToPath(createManifestPlugin2(), pluginPath);

        // empty plugin
        Files.createDirectories(pluginsPath.resolve("test-plugin-3"));

        // no plugin class
        pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-4"));
        storeManifestToPath(createManifestPlugin4(), pluginPath);

        // no plugin version
        pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-5"));
        storeManifestToPath(createManifestPlugin5(), pluginPath);

        // no plugin id
        pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-6"));
        storeManifestToPath(createManifestPlugin6(), pluginPath);

        versionManager = new DefaultVersionManager();
    }

    /**
     * Test of {@link ManifestPluginDescriptorFinder#find(Path)} method.
     */
    @Test
    void find() {
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
    void findNotFound() {
        PluginDescriptorFinder descriptorFinder = new ManifestPluginDescriptorFinder();
        Path pluginPath = pluginsPath.resolve("test-plugin-3");
        assertThrows(PluginRuntimeException.class, () -> descriptorFinder.find(pluginPath));
    }

    private Manifest createManifestPlugin1() {
        return new PluginManifest.Builder("test-plugin-1")
            .pluginClass("org.pf4j.plugin.TestPlugin")
            .pluginVersion("0.0.1")
            .pluginDescription("Test Plugin 1")
            .pluginProvider("Decebal Suiu")
            .pluginDependencies(Arrays.asList("test-plugin-2", "test-plugin-3@~1.0"))
            .pluginRequires("*")
            .pluginLicense("Apache-2.0")
            .build()
            .manifest();
    }

    private Manifest createManifestPlugin2() {
        return new PluginManifest.Builder("test-plugin-2")
            .pluginClass("org.pf4j.plugin.TestPlugin")
            .pluginVersion("0.0.1")
            .pluginProvider("Decebal Suiu")
            .build()
            .manifest();
    }

    private Manifest createManifestPlugin4() {
        return new PluginManifest.Builder("test-plugin-4")
            .pluginVersion("0.0.1")
            .pluginProvider("Decebal Suiu")
            .build()
            .manifest();
    }

    private Manifest createManifestPlugin5() {
        return new PluginManifest.Builder("test-plugin-5")
            .pluginClass("org.pf4j.plugin.TestPlugin")
            .pluginProvider("Decebal Suiu")
            .build()
            .manifest();
    }

    private Manifest createManifestPlugin6() {
        return new PluginManifest.Builder("test-plugin-6")
            .pluginClass("org.pf4j.plugin.TestPlugin")
            .pluginProvider("Decebal Suiu")
            .build()
            .manifest();
    }

    private void storeManifestToPath(Manifest manifest, Path pluginPath) throws IOException {
        Path path = Files.createDirectory(pluginPath.resolve("META-INF"));
        try (OutputStream output = Files.newOutputStream(path.resolve("MANIFEST.MF"))) {
            manifest.write(output);
        }
    }

}
