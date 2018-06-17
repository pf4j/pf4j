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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
public class DefaultPluginRepositoryTest {

    private Path pluginsPath;

    @Rule
    public TemporaryFolder pluginsFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        pluginsPath = pluginsFolder.getRoot().toPath();

        pluginsFolder.newFolder("plugin-1");
        // Prove that we can delete a folder with a file inside
        Files.createFile(Paths.get(pluginsFolder.getRoot().getAbsolutePath()).resolve("plugin-1").resolve("myfile"));
        // Create a zip file for plugin-1 to test that it is deleted when plugin is deleted
        Files.createFile(Paths.get(pluginsFolder.getRoot().getAbsolutePath()).resolve("plugin-1.zip"));
        pluginsFolder.newFolder("plugin-2");
        pluginsFolder.newFolder("plugin-3");
        // standard maven/gradle bin folder - these should be skipped in development mode because the cause errors
        pluginsFolder.newFolder("target");
        pluginsFolder.newFolder("build");
    }

    /**
     * Test of {@link DefaultPluginRepository#getPluginPaths()} method.
     */
    @Test
    public void testGetPluginArchives() {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath, false);

        List<Path> pluginPaths = repository.getPluginPaths();

        assertEquals(5, pluginPaths.size());
        assertPathExists(pluginPaths, pluginsPath.resolve("plugin-1"));
        assertPathExists(pluginPaths, pluginsPath.resolve("plugin-2"));
        assertPathExists(pluginPaths, pluginsPath.resolve("plugin-3"));
        // when not in development mode we will honor these folders
        assertPathExists(pluginPaths, pluginsPath.resolve("target"));
        assertPathExists(pluginPaths, pluginsPath.resolve("build"));
    }

    @Test
    public void testGetPluginArchivesInDevelopmentMode() {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath, true);

        List<Path> pluginPaths = repository.getPluginPaths();

        // target and build should be ignored
        assertEquals(3, pluginPaths.size());
        assertPathDoesNotExists(pluginPaths, pluginsPath.resolve("target"));
        assertPathDoesNotExists(pluginPaths, pluginsPath.resolve("build"));
    }

    /**
     * Test of {@link DefaultPluginRepository#deletePluginPath(Path)} method.
     */
    @Test
    public void testDeletePluginPath() {
        PluginRepository repository = new DefaultPluginRepository(pluginsPath, false);

        assertTrue(Files.exists(pluginsPath.resolve("plugin-1.zip")));
        assertTrue(repository.deletePluginPath(pluginsPath.resolve("plugin-1")));
        assertFalse(Files.exists(pluginsPath.resolve("plugin-1.zip")));
        assertTrue(repository.deletePluginPath(pluginsPath.resolve("plugin-3")));
        assertFalse(repository.deletePluginPath(pluginsPath.resolve("plugin-4")));
        assertTrue(repository.deletePluginPath(pluginsPath.resolve("target")));
        assertTrue(repository.deletePluginPath(pluginsPath.resolve("build")));

        List<Path> pluginPaths = repository.getPluginPaths();

        assertEquals(1, pluginPaths.size());
        assertEquals(pluginsPath.relativize(pluginPaths.get(0)).toString(), "plugin-2");
    }

    private void assertPathExists(List<Path> paths, Path path) {
        assertTrue("The directory must contain the file " + path, paths.contains(path));
    }

    private void assertPathDoesNotExists(List<Path> paths, Path path) {
        assertFalse("The directory must not contain the file " + path, paths.contains(path));
    }

}
