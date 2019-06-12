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
public class DefaultPluginRepositoryTest {

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() throws IOException {
        Path plugin1Path = Files.createDirectory(pluginsPath.resolve("plugin-1"));
        // Prove that we can delete a folder with a file inside
        Files.createFile(plugin1Path.resolve("myfile"));
        // Create a zip file for plugin-1 to test that it is deleted when plugin is deleted
        Files.createFile(pluginsPath.resolve("plugin-1.zip"));
        Files.createDirectory(pluginsPath.resolve("plugin-2"));
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
