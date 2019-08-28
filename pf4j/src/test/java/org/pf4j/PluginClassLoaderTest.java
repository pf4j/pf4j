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
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.plugin.PluginZip;
import org.pf4j.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastian Lövdahl
 */
public class PluginClassLoaderTest {

    private DefaultPluginManager pluginManager;
    private DefaultPluginDescriptor pluginDescriptor;

    private PluginClassLoader parentLastPluginClassLoader;
    private PluginClassLoader parentFirstPluginClassLoader;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    void setUp() throws IOException {
        pluginManager = new DefaultPluginManager(pluginsPath);

        pluginDescriptor = new DefaultPluginDescriptor();
        pluginDescriptor.setPluginId("myPlugin");
        pluginDescriptor.setPluginVersion("1.2.3");
        pluginDescriptor.setPluginDescription("My plugin");
        pluginDescriptor.setDependencies("bar, baz");
        pluginDescriptor.setProvider("Me");
        pluginDescriptor.setRequires("5.0.0");

        Path pluginPath = pluginsPath.resolve(pluginDescriptor.getPluginId() + "-" + pluginDescriptor.getVersion() + ".zip");
        PluginZip pluginZip = new PluginZip.Builder(pluginPath, pluginDescriptor.getPluginId())
            .pluginVersion(pluginDescriptor.getVersion())
            .addFile(Paths.get("classes/META-INF/plugin-file"), "plugin")
            .addFile(Paths.get("classes/META-INF/file-in-both-parent-and-plugin"), "plugin")
            .build();

        FileUtils.expandIfZip(pluginZip.path());

        PluginClasspath pluginClasspath = new DefaultPluginClasspath();

        parentLastPluginClassLoader = new PluginClassLoader(pluginManager, pluginDescriptor, PluginClassLoaderTest.class.getClassLoader());
        parentFirstPluginClassLoader = new PluginClassLoader(pluginManager, pluginDescriptor, PluginClassLoaderTest.class.getClassLoader(), true);

        for (String classesDirectory : pluginClasspath.getClassesDirectories()) {
            File classesDirectoryFile = pluginZip.unzippedPath().resolve(classesDirectory).toFile();
            parentLastPluginClassLoader.addFile(classesDirectoryFile);
            parentFirstPluginClassLoader.addFile(classesDirectoryFile);
        }

        for (String jarsDirectory : pluginClasspath.getJarsDirectories()) {
            Path jarsDirectoryPath = pluginZip.unzippedPath().resolve(jarsDirectory);
            List<File> jars = FileUtils.getJars(jarsDirectoryPath);
            for (File jar : jars) {
                parentLastPluginClassLoader.addFile(jar);
                parentFirstPluginClassLoader.addFile(jar);
            }
        }
    }

    @AfterEach
    void tearDown() {
        pluginManager = null;
        pluginDescriptor = null;
    }

    @Test
    void parentLastGetResourceNonExisting() {
        assertNull(parentLastPluginClassLoader.getResource("META-INF/non-existing-file"));
    }

    @Test
    void parentFirstGetResourceNonExisting() {
        assertNull(parentFirstPluginClassLoader.getResource("META-INF/non-existing-file"));
    }

    @Test
    void parentLastGetResourceExistsInParent() throws IOException, URISyntaxException {
        URL resource = parentLastPluginClassLoader.getResource("META-INF/file");
        assertFirstLine("parent", resource);
    }

    @Test
    void parentFirstGetResourceExistsInParent() throws IOException, URISyntaxException {
        URL resource = parentFirstPluginClassLoader.getResource("META-INF/file");
        assertFirstLine("parent", resource);
    }

    @Test
    void parentLastGetResourceExistsOnlyInPlugin() throws IOException, URISyntaxException {
        URL resource = parentLastPluginClassLoader.getResource("META-INF/plugin-file");
        assertFirstLine("plugin", resource);
    }

    @Test
    void parentFirstGetResourceExistsOnlyInPlugin() throws IOException, URISyntaxException {
        URL resource = parentFirstPluginClassLoader.getResource("META-INF/plugin-file");
        assertFirstLine("plugin", resource);
    }

    @Test
    void parentLastGetResourceExistsInBothParentAndPlugin() throws URISyntaxException, IOException {
        URL resource = parentLastPluginClassLoader.getResource("META-INF/file-in-both-parent-and-plugin");
        assertFirstLine("plugin", resource);
    }

    @Test
    void parentFirstGetResourceExistsInBothParentAndPlugin() throws URISyntaxException, IOException {
        URL resource = parentFirstPluginClassLoader.getResource("META-INF/file-in-both-parent-and-plugin");
        assertFirstLine("parent", resource);
    }

    @Test
    void parentLastGetResourcesNonExisting() throws IOException {
        assertFalse(parentLastPluginClassLoader.getResources("META-INF/non-existing-file").hasMoreElements());
    }

    @Test
    void parentFirstGetResourcesNonExisting() throws IOException {
        assertFalse(parentFirstPluginClassLoader.getResources("META-INF/non-existing-file").hasMoreElements());
    }

    @Test
    void parentLastGetResourcesExistsInParent() throws IOException, URISyntaxException {
        Enumeration<URL> resources = parentLastPluginClassLoader.getResources("META-INF/file");
        assertNumberOfResourcesAndFirstLineOfFirstElement(1, "parent", resources);
    }

    @Test
    void parentFirstGetResourcesExistsInParent() throws IOException, URISyntaxException {
        Enumeration<URL> resources = parentFirstPluginClassLoader.getResources("META-INF/file");
        assertNumberOfResourcesAndFirstLineOfFirstElement(1, "parent", resources);
    }

    @Test
    void parentLastGetResourcesExistsOnlyInPlugin() throws IOException, URISyntaxException {
        Enumeration<URL> resources = parentLastPluginClassLoader.getResources("META-INF/plugin-file");
        assertNumberOfResourcesAndFirstLineOfFirstElement(1, "plugin", resources);
    }

    @Test
    void parentFirstGetResourcesExistsOnlyInPlugin() throws IOException, URISyntaxException {
        Enumeration<URL> resources = parentFirstPluginClassLoader.getResources("META-INF/plugin-file");
        assertNumberOfResourcesAndFirstLineOfFirstElement(1, "plugin", resources);
    }

    @Test
    void parentLastGetResourcesExistsInBothParentAndPlugin() throws URISyntaxException, IOException {
        Enumeration<URL> resources = parentLastPluginClassLoader.getResources("META-INF/file-in-both-parent-and-plugin");
        assertNumberOfResourcesAndFirstLineOfFirstElement(2, "plugin", resources);
    }

    @Test
    void parentFirstGetResourcesExistsInBothParentAndPlugin() throws URISyntaxException, IOException {
        Enumeration<URL> resources = parentFirstPluginClassLoader.getResources("META-INF/file-in-both-parent-and-plugin");
        assertNumberOfResourcesAndFirstLineOfFirstElement(2, "parent", resources);
    }

    private static void assertFirstLine(String expected, URL resource) throws URISyntaxException, IOException {
        assertNotNull(resource);
        assertEquals(expected, Files.readAllLines(Paths.get(resource.toURI())).get(0));
    }

    private static void assertNumberOfResourcesAndFirstLineOfFirstElement(int expectedCount, String expectedFirstLine, Enumeration<URL> resources) throws URISyntaxException, IOException {
        List<URL> list = Collections.list(resources);
        assertEquals(expectedCount, list.size());

        URL firstResource = list.get(0);
        assertEquals(expectedFirstLine, Files.readAllLines(Paths.get(firstResource.toURI())).get(0));
    }
}
