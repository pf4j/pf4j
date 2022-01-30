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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.test.PluginProperties;
import org.pf4j.test.PluginZip;
import org.pf4j.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
class DefaultPluginRepositoryTest {

    Path pluginsPath1;
    Path pluginsPath2;

    @BeforeEach
    void setUp() throws IOException {
        pluginsPath1 = Files.createTempDirectory("junit-pf4j-");
        pluginsPath2 = Files.createTempDirectory("junit-pf4j-");
        Path plugin1Path = Files.createDirectory(pluginsPath1.resolve("plugin-1"));
        // Prove that we can delete a folder with a file inside
        Files.createFile(plugin1Path.resolve("myfile"));
        // Create a zip file for plugin-1 to test that it is deleted when plugin is deleted
        PluginProperties pluginProperties = new PluginProperties.Builder("plugin-1")
            .pluginVersion("1.0")
            .build();
        new PluginZip.Builder(pluginsPath1.resolve("plugin-1.zip"), pluginProperties)
            .build();
        Files.createDirectory(pluginsPath2.resolve("plugin-2"));
        Files.createDirectory(pluginsPath2.resolve("plugin-3"));
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.delete(pluginsPath1);
        FileUtils.delete(pluginsPath2);
    }

    /**
     * Test of {@link DefaultPluginRepository#getPluginPaths()} method.
     */
    @Test
    void getPluginArchivesFromSinglePath() {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath2);

        List<Path> pluginPaths = repository.getPluginPaths();

        assertEquals(2, pluginPaths.size());
        assertPathExists(pluginPaths, pluginsPath2.resolve("plugin-2"));
        assertPathExists(pluginPaths, pluginsPath2.resolve("plugin-3"));
    }

    /**
     * Test of {@link DefaultPluginRepository#getPluginPaths()} method.
     */
    @Test
    void getPluginArchives() {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath1, pluginsPath2);

        List<Path> pluginPaths = repository.getPluginPaths();

        assertEquals(3, pluginPaths.size());
        assertPathExists(pluginPaths, pluginsPath1.resolve("plugin-1"));
        assertPathExists(pluginPaths, pluginsPath2.resolve("plugin-2"));
        assertPathExists(pluginPaths, pluginsPath2.resolve("plugin-3"));
    }

    /**
     * Test of {@link DefaultPluginRepository#deletePluginPath(Path)} method.
     */
    @Test
    void deletePluginPath() {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath1, pluginsPath2);

        assertTrue(Files.exists(pluginsPath1.resolve("plugin-1.zip")));
        assertTrue(repository.deletePluginPath(pluginsPath1.resolve("plugin-1")));
        assertFalse(Files.exists(pluginsPath1.resolve("plugin-1.zip")));
        assertTrue(repository.deletePluginPath(pluginsPath2.resolve("plugin-3")));
        assertFalse(repository.deletePluginPath(pluginsPath2.resolve("plugin-4")));

        List<Path> pluginPaths = repository.getPluginPaths();

        assertEquals(1, pluginPaths.size());
        assertEquals("plugin-2", pluginsPath2.relativize(pluginPaths.get(0)).toString());
    }

    private void assertPathExists(List<Path> paths, Path path) {
        assertTrue(paths.contains(path), "The directory must contain the file " + path);
    }

}
