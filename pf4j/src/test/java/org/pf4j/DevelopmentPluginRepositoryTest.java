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

/**
 * @author Decebal Suiu
 */
public class DevelopmentPluginRepositoryTest {

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() throws IOException {
        // standard maven/gradle bin folder - these should be skipped in development mode because the cause errors
        Files.createDirectory(pluginsPath.resolve(DevelopmentPluginRepository.MAVEN_BUILD_DIR));
        Files.createDirectory(pluginsPath.resolve(DevelopmentPluginRepository.GRADLE_BUILD_DIR));
    }

    @Test
    public void testGetPluginArchivesInDevelopmentMode() {
        PluginRepository repository = new DevelopmentPluginRepository(pluginsPath);

        List<Path> pluginsPaths = repository.getPluginsPaths();

        // target and build should be ignored
        assertEquals(0, pluginsPaths.size());
        assertPathDoesNotExists(pluginsPaths, pluginsPath.resolve(DevelopmentPluginRepository.MAVEN_BUILD_DIR));
        assertPathDoesNotExists(pluginsPaths, pluginsPath.resolve(DevelopmentPluginRepository.GRADLE_BUILD_DIR));
    }

    private void assertPathDoesNotExists(List<Path> paths, Path path) {
        assertFalse(paths.contains(path), "The directory must not contain the file " + path);
    }

}
