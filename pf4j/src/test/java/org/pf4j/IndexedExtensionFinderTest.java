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
import org.pf4j.test.PluginJar;
import org.pf4j.test.TestExtension;
import org.pf4j.test.TestPlugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class IndexedExtensionFinderTest {

    @TempDir
    Path pluginsPath;

    @TempDir
    Path applicationPath;

    /**
     * The application has its own {@code extensions.idx} and the plugin is loaded with
     * {@link ClassLoadingStrategy#APD}, so the application is consulted first. The extensions of the
     * plugin still have to be read from the plugin, see #449.
     */
    @Test
    public void shouldFindPluginExtensionsWhenApplicationComesFirst() throws Exception {
        PluginJar pluginJar = new PluginJar.Builder(pluginsPath.resolve("test-plugin.jar"), "test-plugin")
                .pluginClass(TestPlugin.class.getName())
                .pluginVersion("1.2.3")
                .extension(TestExtension.class.getName())
                .build();

        ClassLoader applicationClassLoader = createApplicationClassLoader();

        PluginManager pluginManager = new JarPluginManager(pluginsPath) {

            @Override
            protected PluginLoader createPluginLoader() {
                return new JarPluginLoader(this) {

                    @Override
                    protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
                        return new PluginClassLoader(pluginManager, pluginDescriptor, applicationClassLoader,
                            ClassLoadingStrategy.APD);
                    }

                };
            }

        };

        pluginManager.loadPlugins();

        assertEquals(1, pluginManager.getPlugins().size());

        IndexedExtensionFinder extensionFinder = new IndexedExtensionFinder(pluginManager);
        Map<String, Set<String>> pluginsStorages = extensionFinder.readPluginsStorages();

        Set<String> pluginStorage = pluginsStorages.get(pluginJar.pluginId());
        assertNotNull(pluginStorage);
        assertThat(pluginStorage, contains(TestExtension.class.getName()));
    }

    /**
     * A plugin without an extensions index of its own declares no extensions. The extensions of a
     * dependency belong to that dependency, which is read on its own turn.
     */
    @Test
    public void shouldNotReadTheExtensionsOfADependency() throws Exception {
        new PluginJar.Builder(pluginsPath.resolve("plugin-a.jar"), "plugin-a")
                .pluginVersion("1.0.0")
                .extension(TestExtension.class.getName())
                .build();

        new PluginJar.Builder(pluginsPath.resolve("plugin-b.jar"), "plugin-b")
                .pluginVersion("1.0.0")
                .manifestAttribute(ManifestPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "plugin-a")
                .build();

        PluginManager pluginManager = new JarPluginManager(pluginsPath);
        pluginManager.loadPlugins();

        assertEquals(2, pluginManager.getPlugins().size());

        IndexedExtensionFinder extensionFinder = new IndexedExtensionFinder(pluginManager);
        Map<String, Set<String>> pluginsStorages = extensionFinder.readPluginsStorages();

        assertThat(pluginsStorages.get("plugin-a"), contains(TestExtension.class.getName()));
        assertEquals(Collections.emptySet(), pluginsStorages.get("plugin-b"));
    }

    /**
     * A plugin loaded with a class loader that is not a {@link URLClassLoader} declares the
     * extensions found under its path. The class loader also sees the storage of the application,
     * and a plugin that declares nothing still declares nothing.
     */
    @Test
    public void shouldFindTheExtensionsOfAPluginLoadedWithAnotherClassLoader() throws Exception {
        new PluginJar.Builder(pluginsPath.resolve("plugin-a.jar"), "plugin-a")
                .pluginVersion("1.0.0")
                .extension(TestExtension.class.getName())
                .build();

        new PluginJar.Builder(pluginsPath.resolve("plugin-b.jar"), "plugin-b")
                .pluginVersion("1.0.0")
                .build();

        ClassLoader applicationClassLoader = createApplicationClassLoader();

        PluginManager pluginManager = new JarPluginManager(pluginsPath) {

            @Override
            protected PluginLoader createPluginLoader() {
                return new JarPluginLoader(this) {

                    @Override
                    public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
                        try {
                            return new CustomClassLoader(pluginPath, applicationClassLoader);
                        } catch (MalformedURLException e) {
                            throw new PluginRuntimeException(e);
                        }
                    }

                };
            }

        };

        pluginManager.loadPlugins();

        assertEquals(2, pluginManager.getPlugins().size());

        IndexedExtensionFinder extensionFinder = new IndexedExtensionFinder(pluginManager);
        Map<String, Set<String>> pluginsStorages = extensionFinder.readPluginsStorages();

        assertThat(pluginsStorages.get("plugin-a"), contains(TestExtension.class.getName()));
        assertEquals(Collections.emptySet(), pluginsStorages.get("plugin-b"));
    }

    /**
     * A plugin loaded with a class loader that is not a {@link URLClassLoader} is searched
     * through its path, so the storage of the application is left out.
     */
    @Test
    public void shouldReadTheStorageOfAPluginLoadedWithAnotherClassLoader() throws Exception {
        // a space in the name, the path of a jar is encoded in the url of a resource it holds
        Path pluginPath = pluginsPath.resolve("test plugin.jar");
        URL pluginUrl = createPluginIndex(pluginPath);
        URL applicationUrl = createApplicationIndex().toUri().toURL();

        Enumeration<URL> urls = findStorageResources(pluginPath, applicationUrl, pluginUrl);

        assertEquals(Collections.singletonList(pluginUrl), Collections.list(urls));
    }

    /**
     * The path of a plugin and the resources of its class loader can reach the same file through a
     * link, so both are resolved before they are compared.
     */
    @Test
    public void shouldReadTheStorageOfAPluginReachedThroughALink() throws Exception {
        Path pluginPath = pluginsPath.resolve("test-plugin.jar");
        URL pluginUrl = createPluginIndex(pluginPath);
        Path linkPath = Files.createSymbolicLink(pluginsPath.resolve("linked-plugin.jar"), pluginPath);

        Enumeration<URL> urls = findStorageResources(linkPath, pluginUrl);

        assertEquals(Collections.singletonList(pluginUrl), Collections.list(urls));
    }

    /**
     * A resource that does not come from a file cannot be traced to the plugin. It is kept, losing
     * the extensions of a plugin is worse than reporting extensions it does not declare.
     */
    @Test
    public void shouldKeepAStorageResourceThatIsNotAFile() throws Exception {
        URL url = new URL("http://localhost/" + IndexedExtensionFinder.EXTENSIONS_RESOURCE);

        Enumeration<URL> urls = findStorageResources(pluginsPath.resolve("test-plugin.jar"), url);

        assertEquals(Collections.singletonList(url), Collections.list(urls));
    }

    /**
     * Reads the storage of a plugin loaded with a class loader that is not a {@link URLClassLoader}
     * and makes the given resources visible.
     */
    private Enumeration<URL> findStorageResources(Path pluginPath, URL... resources) throws IOException {
        List<URL> urls = Arrays.asList(resources);
        ClassLoader classLoader = new ClassLoader(null) {

            @Override
            public Enumeration<URL> getResources(String name) {
                return Collections.enumeration(urls);
            }

        };

        PluginDescriptor pluginDescriptor = new DefaultPluginDescriptor("test-plugin", null, null, "1.2.3", null, null, null);
        PluginWrapper plugin = new PluginWrapper(mock(PluginManager.class), pluginDescriptor, pluginPath, classLoader);

        IndexedExtensionFinder extensionFinder = new IndexedExtensionFinder(mock(PluginManager.class));

        return extensionFinder.findStorageResources(plugin, IndexedExtensionFinder.EXTENSIONS_RESOURCE);
    }

    /**
     * Creates a plugin jar that declares an extension of its own and returns the url of its index.
     */
    private URL createPluginIndex(Path pluginPath) throws IOException {
        PluginJar pluginJar = new PluginJar.Builder(pluginPath, "test-plugin")
                .pluginVersion("1.2.3")
                .extension(TestExtension.class.getName())
                .build();

        return new URL("jar:" + pluginJar.path().toUri().toURL() + "!/" + IndexedExtensionFinder.EXTENSIONS_RESOURCE);
    }

    /**
     * Creates the extensions index of an application that declares an extension of its own.
     */
    private Path createApplicationIndex() throws IOException {
        Path metaInfPath = Files.createDirectories(applicationPath.resolve("META-INF"));
        Path indexPath = metaInfPath.resolve("extensions.idx");
        try (PrintWriter writer = new PrintWriter(indexPath.toFile())) {
            writer.println("# Generated by PF4J");
            writer.println("org.pf4j.test.ApplicationExtension");
        }

        return indexPath;
    }

    /**
     * Creates a class loader for an application that declares an extension of its own.
     */
    private ClassLoader createApplicationClassLoader() throws IOException {
        createApplicationIndex();

        URL[] urls = { applicationPath.toUri().toURL() };

        return new URLClassLoader(urls, getClass().getClassLoader());
    }

    /**
     * A plugin class loader that is not a {@link URLClassLoader}, the type is up to the
     * {@link PluginLoader} that creates it.
     */
    private static class CustomClassLoader extends ClassLoader {

        private final URLClassLoader delegate;

        CustomClassLoader(Path pluginPath, ClassLoader parent) throws MalformedURLException {
            super(parent);

            URL[] urls = { pluginPath.toUri().toURL() };
            this.delegate = new URLClassLoader(urls, parent);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return delegate.getResources(name);
        }

    }

}
