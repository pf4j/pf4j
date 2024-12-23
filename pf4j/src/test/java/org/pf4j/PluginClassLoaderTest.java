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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.processor.LegacyExtensionStorage;
import org.pf4j.test.JavaFileObjectUtils;
import org.pf4j.test.JavaSources;
import org.pf4j.test.PluginZip;
import org.pf4j.util.FileUtils;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sebastian Lövdahl
 */
class PluginClassLoaderTest {

    private TestPluginManager pluginManager;
    private TestPluginManager pluginManagerParentFirst;
    private DefaultPluginDescriptor pluginDependencyDescriptor;
    private DefaultPluginDescriptor pluginDescriptor;

    private PluginClassLoader parentLastPluginClassLoader;
    private PluginClassLoader parentFirstPluginClassLoader;

    private PluginClassLoader parentLastPluginDependencyClassLoader;
    private PluginClassLoader parentFirstPluginDependencyClassLoader;

    private PluginZip pluginDependencyZip;

    @TempDir
    Path pluginsPath;

    @BeforeAll
    static void setUpGlobal() throws IOException, URISyntaxException {
        Path parentClassPathBase = Paths.get(PluginClassLoaderTest.class.getClassLoader().getResource(".").toURI());

        Path metaInfPath = parentClassPathBase.resolve("META-INF");
        File metaInfFile = metaInfPath.toFile();
        if (metaInfFile.mkdir()) {
            // Only delete the directory if this test created it, guarding for any future usages of the directory.
            metaInfFile.deleteOnExit();
        }

        createFile(metaInfPath.resolve("file-only-in-parent"));
        createFile(metaInfPath.resolve("file-in-both-parent-and-dependency-and-plugin"));
        createFile(metaInfPath.resolve("file-in-both-parent-and-dependency"));
        createFile(metaInfPath.resolve("file-in-both-parent-and-plugin"));
        createFile(parentClassPathBase.resolve(LegacyExtensionStorage.EXTENSIONS_RESOURCE));
    }

    private static void createFile(Path pathToFile) throws IOException {
        File file = pathToFile.toFile();

        file.deleteOnExit();
        assertTrue(file.exists() || file.createNewFile(), "failed to create '" + pathToFile + "'");
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.write("parent");
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        pluginManager = new TestPluginManager(pluginsPath);
        pluginManagerParentFirst = new TestPluginManager(pluginsPath);

        pluginDependencyDescriptor = new DefaultPluginDescriptor()
            .setPluginId("myDependency")
            .setPluginVersion("1.2.3")
            .setPluginDescription("My plugin")
            .setDependencies("")
            .setProvider("Me")
            .setRequires("5.0.0");

        Map<String, JavaFileObject> generatedClasses = JavaSources.compileAll(JavaSources.GREETING, JavaSources.WHAZZUP_GREETING)
            .stream()
            .map(javaFileObject -> new AbstractMap.SimpleEntry<>(JavaFileObjectUtils.getClassName(javaFileObject), javaFileObject))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Path classesPath = Paths.get("classes");
        Path metaInfPath = classesPath.resolve("META-INF");

        Path greetingClassPath = classesPath.resolve(JavaSources.GREETING_CLASS_NAME.replace('.', '/') + ".class");
        Path whaszzupGreetingClassPath = classesPath.resolve(JavaSources.WHAZZUP_GREETING_CLASS_NAME.replace('.', '/') + ".class");

        Path pluginDependencyPath = pluginsPath.resolve(pluginDependencyDescriptor.getPluginId() + "-" + pluginDependencyDescriptor.getVersion() + ".zip");
        pluginDependencyZip = new PluginZip.Builder(pluginDependencyPath, pluginDependencyDescriptor.getPluginId())
                .pluginVersion(pluginDependencyDescriptor.getVersion())
                .addFile(metaInfPath.resolve("dependency-file"), "dependency")
                .addFile(metaInfPath.resolve("file-in-both-parent-and-dependency-and-plugin"), "dependency")
                .addFile(metaInfPath.resolve("file-in-both-parent-and-dependency"), "dependency")
                .addFile(classesPath.resolve(LegacyExtensionStorage.EXTENSIONS_RESOURCE), "dependency")
                .addFile(greetingClassPath, JavaFileObjectUtils.getAllBytes(generatedClasses.get(JavaSources.GREETING_CLASS_NAME)))
                .addFile(whaszzupGreetingClassPath, JavaFileObjectUtils.getAllBytes(generatedClasses.get(JavaSources.WHAZZUP_GREETING_CLASS_NAME)))
                .build();

        FileUtils.expandIfZip(pluginDependencyZip.path());

        PluginClasspath pluginDependencyClasspath = new DefaultPluginClasspath();

        parentLastPluginDependencyClassLoader = new PluginClassLoader(pluginManager, pluginDependencyDescriptor, PluginClassLoaderTest.class.getClassLoader());
        parentFirstPluginDependencyClassLoader = new PluginClassLoader(pluginManagerParentFirst, pluginDependencyDescriptor, PluginClassLoaderTest.class.getClassLoader(), true);

        pluginManager.addClassLoader(pluginDependencyDescriptor.getPluginId(), parentLastPluginDependencyClassLoader);
        pluginManagerParentFirst.addClassLoader(pluginDependencyDescriptor.getPluginId(), parentFirstPluginDependencyClassLoader);

        for (String classesDirectory : pluginDependencyClasspath.getClassesDirectories()) {
            File classesDirectoryFile = pluginDependencyZip.unzippedPath().resolve(classesDirectory).toFile();
            parentLastPluginDependencyClassLoader.addFile(classesDirectoryFile);
            parentFirstPluginDependencyClassLoader.addFile(classesDirectoryFile);
        }

        for (String jarsDirectory : pluginDependencyClasspath.getJarsDirectories()) {
            Path jarsDirectoryPath = pluginDependencyZip.unzippedPath().resolve(jarsDirectory);
            List<File> jars = FileUtils.getJars(jarsDirectoryPath);
            for (File jar : jars) {
                parentLastPluginDependencyClassLoader.addFile(jar);
                parentFirstPluginDependencyClassLoader.addFile(jar);
            }
        }

        pluginDescriptor = new DefaultPluginDescriptor();
        pluginDescriptor.setPluginId("myPlugin");
        pluginDescriptor.setPluginVersion("1.2.3");
        pluginDescriptor.setPluginDescription("My plugin");
        pluginDescriptor.setDependencies("myDependency");
        pluginDescriptor.setProvider("Me");
        pluginDescriptor.setRequires("5.0.0");

        Path pluginPath = pluginsPath.resolve(pluginDescriptor.getPluginId() + "-" + pluginDescriptor.getVersion() + ".zip");
        PluginZip pluginZip = new PluginZip.Builder(pluginPath, pluginDescriptor.getPluginId())
                .pluginVersion(pluginDescriptor.getVersion())
                .addFile(metaInfPath.resolve("plugin-file"), "plugin")
                .addFile(metaInfPath.resolve("file-in-both-parent-and-dependency-and-plugin"), "plugin")
                .addFile(metaInfPath.resolve("file-in-both-parent-and-plugin"), "plugin")
                .addFile(classesPath.resolve(LegacyExtensionStorage.EXTENSIONS_RESOURCE), "plugin")
                .build();

        FileUtils.expandIfZip(pluginZip.path());

        PluginClasspath pluginClasspath = new DefaultPluginClasspath();

        parentLastPluginClassLoader = new PluginClassLoader(pluginManager, pluginDescriptor, PluginClassLoaderTest.class.getClassLoader());
        parentFirstPluginClassLoader = new PluginClassLoader(pluginManager, pluginDescriptor, PluginClassLoaderTest.class.getClassLoader(), true);

        pluginManager.addClassLoader(pluginDescriptor.getPluginId(), parentLastPluginClassLoader);
        pluginManagerParentFirst.addClassLoader(pluginDescriptor.getPluginId(), parentFirstPluginClassLoader);

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
        pluginDependencyDescriptor = null;
        pluginDescriptor = null;
        parentLastPluginClassLoader =  null;
        parentFirstPluginClassLoader = null;
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
        URL resource = parentLastPluginClassLoader.getResource("META-INF/file-only-in-parent");
        assertFirstLine("parent", resource);
    }

    @Test
    void parentFirstGetResourceExistsInParent() throws IOException, URISyntaxException {
        URL resource = parentFirstPluginClassLoader.getResource("META-INF/file-only-in-parent");
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
    void parentLastGetResourceExistsOnlyInDependnecy() throws IOException, URISyntaxException {
        URL resource = parentLastPluginClassLoader.getResource("META-INF/dependency-file");
        assertFirstLine("dependency", resource);
    }

    @Test
    void parentFirstGetResourceExistsOnlyInDependency() throws IOException, URISyntaxException {
        URL resource = parentFirstPluginClassLoader.getResource("META-INF/dependency-file");
        assertFirstLine("dependency", resource);
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
    void parentLastGetResourceExistsInParentAndDependencyAndPlugin() throws URISyntaxException, IOException {
        URL resource = parentLastPluginClassLoader.getResource("META-INF/file-in-both-parent-and-dependency-and-plugin");
        assertFirstLine("plugin", resource);
    }

    @Test
    void parentFirstGetResourceExistsInParentAndDependencyAndPlugin() throws URISyntaxException, IOException {
        URL resource = parentFirstPluginClassLoader.getResource("META-INF/file-in-both-parent-and-dependency-and-plugin");
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
        Enumeration<URL> resources = parentLastPluginClassLoader.getResources("META-INF/file-only-in-parent");
        assertNumberOfResourcesAndFirstLineOfFirstElement(1, "parent", resources);
    }

    @Test
    void parentFirstGetResourcesExistsInParent() throws IOException, URISyntaxException {
        Enumeration<URL> resources = parentFirstPluginClassLoader.getResources("META-INF/file-only-in-parent");
        assertNumberOfResourcesAndFirstLineOfFirstElement(1, "parent", resources);
    }

    @Test
    void parentLastGetResourcesExistsOnlyInDependency() throws IOException, URISyntaxException {
        Enumeration<URL> resources = parentLastPluginClassLoader.getResources("META-INF/dependency-file");
        assertNumberOfResourcesAndFirstLineOfFirstElement(1, "dependency", resources);
    }

    @Test
    void parentFirstGetResourcesExistsOnlyInDependency() throws IOException, URISyntaxException {
        Enumeration<URL> resources = parentFirstPluginClassLoader.getResources("META-INF/dependency-file");
        assertNumberOfResourcesAndFirstLineOfFirstElement(1, "dependency", resources);
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

    @Test
    void parentLastGetResourcesExistsInParentAndDependencyAndPlugin() throws URISyntaxException, IOException {
        Enumeration<URL> resources = parentLastPluginClassLoader.getResources("META-INF/file-in-both-parent-and-dependency-and-plugin");
        assertNumberOfResourcesAndFirstLineOfFirstElement(3, "plugin", resources);
    }

    @Test
    void parentFirstGetResourcesExistsInParentAndDependencyAndPlugin() throws URISyntaxException, IOException {
        Enumeration<URL> resources = parentFirstPluginClassLoader.getResources("META-INF/file-in-both-parent-and-dependency-and-plugin");
        assertNumberOfResourcesAndFirstLineOfFirstElement(3, "parent", resources);
    }

    @Test
    void parentFirstGetExtensionsIndexExistsInParentAndDependencyAndPlugin() throws URISyntaxException, IOException {
        URL resource = parentLastPluginClassLoader.getResource(LegacyExtensionFinder.EXTENSIONS_RESOURCE);
        assertFirstLine("plugin", resource);
    }

    @Test
    void parentLastGetExtensionsIndexExistsInParentAndDependencyAndPlugin() throws URISyntaxException, IOException {
        URL resource = parentLastPluginClassLoader.getResource(LegacyExtensionFinder.EXTENSIONS_RESOURCE);
        assertFirstLine("plugin", resource);
    }

    @Test
    void isClosed() throws IOException {
        parentLastPluginClassLoader.close();
        assertTrue(parentLastPluginClassLoader.isClosed());
    }

    @Test
    void collectClassLoader() throws IOException, ClassNotFoundException, InterruptedException {
        // Create a new classloader
        PluginClassLoader classLoader = new PluginClassLoader(pluginManager, pluginDependencyDescriptor, PluginClassLoaderTest.class.getClassLoader());
        PluginClasspath pluginDependencyClasspath = new DefaultPluginClasspath();
        for (String classesDirectory : pluginDependencyClasspath.getClassesDirectories()) {
            File classesDirectoryFile = pluginDependencyZip.unzippedPath().resolve(classesDirectory).toFile();
            classLoader.addFile(classesDirectoryFile);
        }

        WeakReference<PluginClassLoader> weakRef = new WeakReference<>(classLoader);

        // Use the classloader
        classLoader.loadClass(JavaSources.GREETING_CLASS_NAME);

        // Clear strong reference
        classLoader.close();
        classLoader = null;

        // Try to force GC
        System.gc();
        System.runFinalization();
        Thread.sleep(100); // Give GC a chance to run

        // Check if ClassLoader was collected
        assertNull(weakRef.get(), "ClassLoader was not garbage collected");
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

    static class TestPluginManager extends DefaultPluginManager {

        public TestPluginManager(Path pluginsPath) {
            super(pluginsPath);
        }

        void addClassLoader(String pluginId, PluginClassLoader classLoader) {
            getPluginClassLoaders().put(pluginId, classLoader);
        }

    }

}
