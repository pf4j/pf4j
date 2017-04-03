/*
 * Copyright 2015 Decebal Suiu
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
package ro.fortsoft.pf4j;

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

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        testFolder.newFolder("plugin-1");
        // Prove that we can delete a folder with a file inside
        Files.createFile(Paths.get(testFolder.getRoot().getAbsolutePath()).resolve("plugin-1").resolve("myfile"));
        testFolder.newFolder("plugin-2");
        testFolder.newFolder("plugin-3");
    }

    /**
     * Test of {@link DefaultPluginRepository#getPluginPaths()} method.
     */
    @Test
    public void testGetPluginArchives() {
        Path pluginsRoot = getPluginsRoot();

        PluginRepository instance = new DefaultPluginRepository(pluginsRoot, false);

        List<Path> result = instance.getPluginPaths();

        assertEquals(3, result.size());
        assertPathExists(result, pluginsRoot.resolve("plugin-1"));
        assertPathExists(result, pluginsRoot.resolve("plugin-2"));
        assertPathExists(result, pluginsRoot.resolve("plugin-3"));
    }

    /**
     * Test of {@link DefaultPluginRepository#deletePluginPath(Path)} method.
     */
    @Test
    public void testDeletePluginPath() {
        Path pluginsRoot = getPluginsRoot();

        PluginRepository instance = new DefaultPluginRepository(pluginsRoot, false);

        assertTrue(instance.deletePluginPath(pluginsRoot.resolve("plugin-1")));
        assertTrue(instance.deletePluginPath(pluginsRoot.resolve("plugin-3")));
        assertFalse(instance.deletePluginPath(pluginsRoot.resolve("plugin-4")));

        List<Path> result = instance.getPluginPaths();

        assertEquals(1, result.size());
        assertEquals(pluginsRoot.relativize(result.get(0)).toString(), "plugin-2");
    }

    private void assertPathExists(List<Path> paths, Path path) {
        assertTrue("The directory must contain the file " + path, paths.contains(path));
    }

    private Path getPluginsRoot() {
        return testFolder.getRoot().toPath();
    }

}
