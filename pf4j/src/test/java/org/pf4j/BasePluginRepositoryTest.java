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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasePluginRepositoryTest {

    @TempDir
    Path pluginsRoot1;

    @TempDir
    Path pluginsRoot2;

    private FileFilter jarFilter;

    @BeforeEach
    void setUp() {
        jarFilter = file -> file.getName().endsWith(".jar");
    }

    @Test
    void testConstructorWithVarargs() {
        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1, pluginsRoot2);

        assertEquals(2, repository.pluginsRoots.size());
        assertTrue(repository.pluginsRoots.contains(pluginsRoot1));
        assertTrue(repository.pluginsRoots.contains(pluginsRoot2));
    }

    @Test
    void testConstructorWithList() {
        List<Path> roots = Arrays.asList(pluginsRoot1, pluginsRoot2);
        BasePluginRepository repository = new BasePluginRepository(roots);

        assertEquals(2, repository.pluginsRoots.size());
    }

    @Test
    void testConstructorWithFilter() {
        BasePluginRepository repository = new BasePluginRepository(Arrays.asList(pluginsRoot1), jarFilter);

        assertEquals(jarFilter, repository.filter);
    }

    @Test
    void testGetPluginPathsWithMultipleRoots() throws IOException {
        Files.createFile(pluginsRoot1.resolve("multiroot1.jar"));
        Files.createFile(pluginsRoot1.resolve("multiroot2.jar"));
        Files.createFile(pluginsRoot2.resolve("multiroot3.jar"));

        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1, pluginsRoot2);
        repository.setFilter(jarFilter);

        List<Path> paths = repository.getPluginPaths();

        // Verify our test files exist in the results
        assertTrue(paths.stream().anyMatch(p -> p.getFileName().toString().equals("multiroot1.jar")));
        assertTrue(paths.stream().anyMatch(p -> p.getFileName().toString().equals("multiroot2.jar")));
        assertTrue(paths.stream().anyMatch(p -> p.getFileName().toString().equals("multiroot3.jar")));

        // Verify files from both roots are included
        long root1Count = paths.stream().filter(p -> p.startsWith(pluginsRoot1)).count();
        long root2Count = paths.stream().filter(p -> p.startsWith(pluginsRoot2)).count();
        assertTrue(root1Count >= 2, "Should have at least 2 files from pluginsRoot1");
        assertTrue(root2Count >= 1, "Should have at least 1 file from pluginsRoot2");
    }

    @Test
    void testGetPluginPathsWithFilter() throws IOException {
        Files.createFile(pluginsRoot1.resolve("plugin1.jar"));
        Files.createFile(pluginsRoot1.resolve("readme.txt"));
        Files.createFile(pluginsRoot1.resolve("plugin2.jar"));

        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1);
        repository.setFilter(jarFilter);

        List<Path> paths = repository.getPluginPaths();

        assertEquals(2, paths.size());
        assertTrue(paths.stream().allMatch(p -> p.toString().endsWith(".jar")));
    }

    @Test
    void testGetPluginPathsWithEmptyDirectory() {
        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1);
        repository.setFilter(jarFilter);

        List<Path> paths = repository.getPluginPaths();

        assertTrue(paths.isEmpty());
    }

    @Test
    void testGetPluginPathsWithNonExistentDirectory() {
        Path nonExistent = pluginsRoot1.resolve("non-existent");
        BasePluginRepository repository = new BasePluginRepository(nonExistent);
        repository.setFilter(jarFilter);

        List<Path> paths = repository.getPluginPaths();

        assertTrue(paths.isEmpty());
    }

    @Test
    void testDeletePluginPathSuccess() throws IOException {
        Path pluginPath = Files.createFile(pluginsRoot1.resolve("plugin.jar"));

        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1);
        repository.setFilter(jarFilter);

        assertTrue(Files.exists(pluginPath));
        assertTrue(repository.deletePluginPath(pluginPath));
        assertFalse(Files.exists(pluginPath));
    }

    @Test
    void testDeletePluginPathNotFound() {
        Path nonExistent = pluginsRoot1.resolve("non-existent.jar");

        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1);
        repository.setFilter(jarFilter);

        assertFalse(repository.deletePluginPath(nonExistent));
    }

    @Test
    void testDeletePluginPathRejectedByFilter() throws IOException {
        Path textFile = Files.createFile(pluginsRoot1.resolve("readme.txt"));

        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1);
        repository.setFilter(jarFilter);

        assertFalse(repository.deletePluginPath(textFile));
        assertTrue(Files.exists(textFile));
    }

    @Test
    void testSetFilter() throws IOException {
        Files.createFile(pluginsRoot1.resolve("plugin.jar"));
        Files.createFile(pluginsRoot1.resolve("readme.txt"));

        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1);

        // No filter - should get all files
        repository.setFilter(file -> true);
        assertEquals(2, repository.getPluginPaths().size());

        // JAR filter - should get only .jar files
        repository.setFilter(jarFilter);
        assertEquals(1, repository.getPluginPaths().size());
    }

    @Test
    void testSetComparator() throws IOException {
        Files.createFile(pluginsRoot1.resolve("z-plugin.jar"));
        Files.createFile(pluginsRoot1.resolve("a-plugin.jar"));
        Files.createFile(pluginsRoot1.resolve("m-plugin.jar"));

        BasePluginRepository repository = new BasePluginRepository(pluginsRoot1);
        repository.setFilter(jarFilter);

        // Set alphabetical comparator (ascending by name)
        repository.setComparator(Comparator.comparing(File::getName));

        List<Path> paths = repository.getPluginPaths();

        assertEquals(3, paths.size());
        assertEquals("a-plugin.jar", paths.get(0).getFileName().toString());
        assertEquals("m-plugin.jar", paths.get(1).getFileName().toString());
        assertEquals("z-plugin.jar", paths.get(2).getFileName().toString());

        // Set reverse alphabetical comparator
        repository.setComparator(Comparator.comparing(File::getName).reversed());

        paths = repository.getPluginPaths();

        assertEquals("z-plugin.jar", paths.get(0).getFileName().toString());
        assertEquals("m-plugin.jar", paths.get(1).getFileName().toString());
        assertEquals("a-plugin.jar", paths.get(2).getFileName().toString());
    }

}