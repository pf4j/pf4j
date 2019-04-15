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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Decebal Suiu
 */
public class JarPluginRepositoryTest {

    @TempDir
    Path pluginsPath;

    /**
     * Test of {@link JarPluginRepository#deletePluginPath(Path)} method.
     */
    @Test
    public void testDeletePluginPath() throws IOException {
        PluginRepository repository = new JarPluginRepository(pluginsPath);

        Path plugin1Path = Files.createDirectory(pluginsPath.resolve("plugin-1"));
        Path plugin1JarPath = Files.createFile(pluginsPath.resolve("plugin-1.jar"));

        assertFalse(repository.deletePluginPath(plugin1Path));

        List<Path> pluginsPaths = repository.getPluginsPaths();
        assertEquals(1, pluginsPaths.size());

        assertTrue(repository.deletePluginPath(plugin1JarPath));

        pluginsPaths = repository.getPluginsPaths();
        assertEquals(0, pluginsPaths.size());
    }

}
