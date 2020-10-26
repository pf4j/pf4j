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
import org.pf4j.plugin.PluginZip;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
public class DefaultPluginRepositoryTest {

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() throws IOException {
        Path plugin1Path = Files.createDirectory(pluginsPath.resolve("plugin-1"));

        // Prove that we can delete a folder with a file inside
        Properties plugin1Properties = new Properties();
        plugin1Properties.setProperty("id", "plugin-1");
        plugin1Properties.setProperty("version", "1.0");
        plugin1Properties.store(new FileOutputStream(plugin1Path.resolve("plugin.properties").toFile()), null);

        // Create a zip file for plugin-1 to test that it is deleted when plugin is deleted
        new PluginZip.Builder(pluginsPath.resolve("plugin-1.zip"), "plugin-1").pluginVersion("1.0").build();
        new PluginZip.Builder(pluginsPath.resolve("plugin-2.zip"), "plugin-2").pluginVersion("1.0").build();
        Files.createDirectory(pluginsPath.resolve("plugin-3"));
    }

    /**
     * Test of {@link DefaultPluginRepository#getPluginPaths()} method.
     */
    @Test
    public void testGetPluginArchives() {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath);

        List<Path> pluginPaths = repository.getPluginPaths();

        assertEquals(3, pluginPaths.size());
        assertPathExists(pluginPaths, pluginsPath.resolve("plugin-1"));
        assertPathExists(pluginPaths, pluginsPath.resolve("plugin-2"));
        assertPathExists(pluginPaths, pluginsPath.resolve("plugin-3"));
    }

    /**
     * Test of {@link DefaultPluginRepository#getPluginPaths()} method with subdirectory plugins.
     * <p>
     * This test uses a PropertiesPluginDescriptorFinder, so plugin-3 will not be recognized as plugins.
     * </p>
     * <p>
     * Structure of the pluginsRoot folder for this test:
     * </p>
     * <code><pre>
     * pluginsPath
     * ├── subdirectory
     * │    ├── subsub
     * │    │    └── plugin-5.zip
     * │    └── plugin-4.zip
     * ├── plugin-1
     * │     └── plugin.properties
     * ├── plugin-1.zip
     * ├── plugin-2.zip
     * └── plugin-3
     * </pre></code>
     */
    @Test
    public void testGetSubdirectoryPluginArchives() throws IOException {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath, new PropertiesPluginDescriptorFinder());

        Path subdirectory = Files.createDirectory(pluginsPath.resolve("subdirectory"));
        new PluginZip.Builder(subdirectory.resolve("plugin-4.zip"), "plugin-4").pluginVersion("1.0").build();
        Path subsub = Files.createDirectory(subdirectory.resolve("subsub"));
        new PluginZip.Builder(subsub.resolve("plugin-5.zip"), "plugin-5").pluginVersion("1.0").build();

        List<Path> pluginPaths = repository.getPluginPaths();

        assertEquals(4, pluginPaths.size());
        assertPathExists(pluginPaths, pluginsPath.resolve("plugin-1"));
        assertPathExists(pluginPaths, pluginsPath.resolve("plugin-2"));
        assertPathExists(pluginPaths, subdirectory.resolve("plugin-4"));
        assertPathExists(pluginPaths, subsub.resolve("plugin-5"));
    }

    /**
     * Test of {@link DefaultPluginRepository#deletePluginPath(Path)} method.
     */
    @Test
    public void testDeletePluginPath() {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath);

        assertTrue(Files.exists(pluginsPath.resolve("plugin-1.zip")));
        assertTrue(repository.deletePluginPath(pluginsPath.resolve("plugin-1")));
        assertFalse(Files.exists(pluginsPath.resolve("plugin-1.zip")));
        assertTrue(repository.deletePluginPath(pluginsPath.resolve("plugin-3")));
        assertFalse(repository.deletePluginPath(pluginsPath.resolve("plugin-4")));

        List<Path> pluginPaths = repository.getPluginPaths();

        assertEquals(1, pluginPaths.size());
        assertEquals(pluginsPath.relativize(pluginPaths.get(0)).toString(), "plugin-2");
    }

    private void assertPathExists(List<Path> paths, Path path) {
        assertTrue(paths.contains(path), "The directory must contain the file " + path);
    }

}
