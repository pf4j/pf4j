/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j;

import com.google.testing.compile.JavaFileObjects;
import kotlin.sequences.Sequence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.test.JavaFileObjectClassLoader;
import org.pf4j.test.JavaFileObjectUtils;
import org.pf4j.test.JavaSources;
import org.pf4j.test.TestExtension;
import org.pf4j.test.TestExtensionPoint;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Mario Franco
 */
class AbstractExtensionFinderTest {

    private static final String FOO_GREETING_CLASS_NAME = "test.FooGreeting";
    private static final JavaFileObject FOO_GREETING = JavaFileObjects.forSourceLines("FooGreeting",
        "package test;",
        "import org.pf4j.Extension;",
        "",
        "@Extension(plugins = \"foo\")",
        "public class FooGreeting implements Greeting {",
        "    @Override",
        "    public String getGreeting() {",
        "        return \"Foo\";",
        "    }",
        "}");

    @TempDir
    Path tempDir;

    private PluginManager pluginManager;

    @BeforeEach
    public void setUp() {
        PluginWrapper pluginStarted = mock(PluginWrapper.class);
        when(pluginStarted.getPluginClassLoader()).thenReturn(getClass().getClassLoader());
        when(pluginStarted.getPluginState()).thenReturn(PluginState.STARTED);

        PluginWrapper pluginStopped = mock(PluginWrapper.class);
        when(pluginStopped.getPluginClassLoader()).thenReturn(getClass().getClassLoader());
        when(pluginStopped.getPluginState()).thenReturn(PluginState.STOPPED);

        pluginManager = mock(PluginManager.class);
        when(pluginManager.getPlugin("plugin1")).thenReturn(pluginStarted);
        when(pluginManager.getPlugin("plugin2")).thenReturn(pluginStopped);
        when(pluginManager.getPluginClassLoader("plugin1")).thenReturn(getClass().getClassLoader());
        when(pluginManager.getExtensionFactory()).thenReturn(new DefaultExtensionFactory());
    }

    @AfterEach
    public void tearDown() {
        pluginManager = null;
        // Force garbage collection to clean up ClassLoaders from dynamic class loading tests
        // This helps prevent ClassLoader conflicts, especially on Java 11
        System.gc();
        // TODO: Test is still flaky, needs further investigation
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class)}.
     */
    @Test
    void testFindFailType() {
        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                return Collections.emptyMap();
            }

            @Override
            public Map<String, Set<String>> readClasspathStorages() {
                return Collections.emptyMap();
            }

        };
        List<ExtensionWrapper<TestExtension>> list = instance.find(TestExtension.class);
        assertEquals(0, list.size());
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class)}.
     */
    @Test
    void testFindFromClasspath() {
        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                return Collections.emptyMap();
            }

            @Override
            public Map<String, Set<String>> readClasspathStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();

                Set<String> bucket = new HashSet<>();
                bucket.add("org.pf4j.test.TestExtension");
                entries.put(null, bucket);

                return entries;
            }

        };

        List<ExtensionWrapper<TestExtensionPoint>> list = instance.find(TestExtensionPoint.class);
        assertEquals(1, list.size());
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class, String)}.
     */
    @Test
    void testFindFromPlugin() {
        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();

                Set<String> bucket = new HashSet<>();
                bucket.add("org.pf4j.test.TestExtension");
                entries.put("plugin1", bucket);
                bucket = new HashSet<>();
                bucket.add("org.pf4j.test.TestExtension");
                entries.put("plugin2", bucket);

                return entries;
            }

            @Override
            public Map<String, Set<String>> readClasspathStorages() {
                return Collections.emptyMap();
            }

        };

        List<ExtensionWrapper<TestExtensionPoint>> list = instance.find(TestExtensionPoint.class);
        assertEquals(1, list.size());

        list = instance.find(TestExtensionPoint.class, "plugin1");
        assertEquals(1, list.size());

        list = instance.find(TestExtensionPoint.class, "plugin2");
        // "0" because the status of "plugin2" is STOPPED => no extensions
        assertEquals(0, list.size());
    }

    /**
     * Test of {@link AbstractExtensionFinder#findClassNames(String)}.
     */
    @Test
    void testFindClassNames() {
        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();

                Set<String> bucket = new HashSet<>();
                bucket.add("org.pf4j.plugin.TestExtension");
                entries.put("plugin1", bucket);

                return entries;
            }

            @Override
            public Map<String, Set<String>> readClasspathStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();

                Set<String> bucket = new HashSet<>();
                bucket.add("org.pf4j.plugin.TestExtension");
                bucket.add("org.pf4j.plugin.FailTestExtension");
                entries.put(null, bucket);

                return entries;
            }

        };

        Set<String> result = instance.findClassNames(null);
        assertEquals(2, result.size());

        result = instance.findClassNames("plugin1");
        assertEquals(1, result.size());
    }

    /**
     * Test of {@link org.pf4j.AbstractExtensionFinder#find(java.lang.String)}.
     */
    @Test
    void testFindExtensionWrappersFromPluginId() {
        // complicate the test to show hot to deal with dynamic Java classes (generated at runtime from sources)
        PluginWrapper plugin3 = mock(PluginWrapper.class);
        JavaFileObject object = JavaSources.compile(DefaultExtensionFactoryTest.FailTestExtension);
        JavaFileObjectClassLoader classLoader = new JavaFileObjectClassLoader();
        classLoader.load(object);
        when(plugin3.getPluginClassLoader()).thenReturn(classLoader);
        when(plugin3.getPluginState()).thenReturn(PluginState.STARTED);
        when(pluginManager.getPluginClassLoader("plugin3")).thenReturn(classLoader);
        when(pluginManager.getPlugin("plugin3")).thenReturn(plugin3);

        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();

                Set<String> bucket = new HashSet<>();
                bucket.add("org.pf4j.test.TestExtension");
                entries.put("plugin1", bucket);
                bucket = new HashSet<>();
                bucket.add("org.pf4j.test.TestExtension");
                entries.put("plugin2", bucket);
                bucket = new HashSet<>();
                bucket.add(JavaFileObjectUtils.getClassName(object));
                entries.put("plugin3", bucket);

                return entries;
            }

            @Override
            public Map<String, Set<String>> readClasspathStorages() {
                return Collections.emptyMap();
            }

        };

        List<ExtensionWrapper> plugin1Result = instance.find("plugin1");
        assertEquals(1, plugin1Result.size());

        List<ExtensionWrapper> plugin2Result = instance.find("plugin2");
        assertEquals(0, plugin2Result.size());

        List<ExtensionWrapper> plugin3Result = instance.find("plugin3");
        assertEquals(1, plugin3Result.size());

        List<ExtensionWrapper> plugin4Result = instance.find(UUID.randomUUID().toString());
        assertEquals(0, plugin4Result.size());
    }

    @Test
    void findExtensionAnnotation() {
        List<JavaFileObject> generatedFiles = JavaSources.compileAll(JavaSources.GREETING, JavaSources.WHAZZUP_GREETING);
        assertEquals(2, generatedFiles.size());

        Map<String, Class<?>> loadedClasses = new JavaFileObjectClassLoader().load(generatedFiles);
        Class<?> clazz = loadedClasses.get(JavaSources.WHAZZUP_GREETING_CLASS_NAME);
        Extension extension = AbstractExtensionFinder.findExtensionAnnotation(clazz);
        Assertions.assertNotNull(extension);
    }

    @Test
    void findExtensionAnnotationThatMissing() {
        List<JavaFileObject> generatedFiles = JavaSources.compileAll(JavaSources.GREETING,
            ExtensionAnnotationProcessorTest.SpinnakerExtension_NoExtension,
            ExtensionAnnotationProcessorTest.WhazzupGreeting_SpinnakerExtension);
        assertEquals(3, generatedFiles.size());

        Map<String, Class<?>> loadedClasses = new JavaFileObjectClassLoader().load(generatedFiles);
        Class<?> clazz = loadedClasses.get(JavaSources.WHAZZUP_GREETING_CLASS_NAME);
        Extension extension = AbstractExtensionFinder.findExtensionAnnotation(clazz);
        Assertions.assertNull(extension);
    }

    // This is a regression test, as this caused an StackOverflowError with the previous implementation
    @Test
    public void runningOnNonExtensionKotlinClassDoesNotThrowException() {
        Extension result = AbstractExtensionFinder.findExtensionAnnotation(Sequence.class);

        Assertions.assertNull(result);
    }

    @Test
    void checkDifferentClassLoaders() {
        AbstractExtensionFinder extensionFinder = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                return Collections.emptyMap();
            }

            @Override
            public Map<String, Set<String>> readClasspathStorages() {
                return Collections.emptyMap();
            }

        };

        List<JavaFileObject> generatedFiles = JavaSources.compileAll(JavaSources.GREETING, JavaSources.WHAZZUP_GREETING);
        assertEquals(2, generatedFiles.size());
        Class<?> extensionPointClass = new JavaFileObjectClassLoader().load(generatedFiles).get(JavaSources.GREETING_CLASS_NAME);
        Class<?> extensionClass = new JavaFileObjectClassLoader().load(generatedFiles).get(JavaSources.WHAZZUP_GREETING_CLASS_NAME);

        assertTrue(extensionFinder.checkDifferentClassLoaders(extensionPointClass, extensionClass));
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class)} with an extension that requires
     * a plugin which is not started.
     */
    @Test
    void findSkipsExtensionWhoseRequiredPluginIsNotStarted() throws Exception {
        try (ExtensionClassLoader classLoader = createExtensionClassLoader()) {
            AbstractExtensionFinder instance = createFinderWithExtensionDependencies(classLoader);
            Class<?> greeting = classLoader.loadClass(JavaSources.GREETING_CLASS_NAME);

            // "foo" is not a plugin
            assertEquals(0, instance.find(greeting).size());

            // "foo" is a plugin, but it is not started
            PluginWrapper foo = mock(PluginWrapper.class);
            when(foo.getPluginState()).thenReturn(PluginState.STOPPED);
            when(pluginManager.getPlugin("foo")).thenReturn(foo);
            assertEquals(0, instance.find(greeting).size());

            // the annotation is read with asm, the extension class itself is never loaded
            assertFalse(classLoader.isLoaded(FOO_GREETING_CLASS_NAME));
        }
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class)} with an extension that requires
     * a plugin which is started.
     */
    @Test
    void findReturnsExtensionWhoseRequiredPluginIsStarted() throws Exception {
        try (ExtensionClassLoader classLoader = createExtensionClassLoader()) {
            AbstractExtensionFinder instance = createFinderWithExtensionDependencies(classLoader);
            Class<?> greeting = classLoader.loadClass(JavaSources.GREETING_CLASS_NAME);

            PluginWrapper foo = mock(PluginWrapper.class);
            when(foo.getPluginState()).thenReturn(PluginState.STARTED);
            when(pluginManager.getPlugin("foo")).thenReturn(foo);

            List<? extends ExtensionWrapper<?>> list = instance.find(greeting);
            assertEquals(1, list.size());
            assertEquals(FOO_GREETING_CLASS_NAME, list.get(0).getDescriptor().extensionClass.getName());
            assertTrue(classLoader.isLoaded(FOO_GREETING_CLASS_NAME));
        }
    }

    /**
     * Writes the class files of {@link JavaSources#GREETING} and {@link #FOO_GREETING} to a directory,
     * so that asm can read them as resources of the returned class loader.
     */
    private ExtensionClassLoader createExtensionClassLoader() throws IOException {
        for (JavaFileObject object : JavaSources.compileAll(JavaSources.GREETING, FOO_GREETING)) {
            Path classFile = tempDir.resolve(JavaFileObjectUtils.getClassName(object).replace('.', '/') + ".class");
            Files.createDirectories(classFile.getParent());
            Files.write(classFile, JavaFileObjectUtils.getAllBytes(object));
        }

        return new ExtensionClassLoader(tempDir.toUri().toURL(), getClass().getClassLoader());
    }

    /**
     * Creates a finder with {@link #FOO_GREETING} in the storage of "plugin3", a started plugin
     * with an optional dependency, whose start turns on the check for extension dependencies.
     */
    private AbstractExtensionFinder createFinderWithExtensionDependencies(ClassLoader classLoader) {
        PluginDescriptor descriptor = mock(PluginDescriptor.class);
        when(descriptor.getDependencies()).thenReturn(Collections.singletonList(new PluginDependency("foo?")));

        PluginWrapper plugin3 = mock(PluginWrapper.class);
        when(plugin3.getDescriptor()).thenReturn(descriptor);
        when(plugin3.getPluginClassLoader()).thenReturn(classLoader);
        when(plugin3.getPluginState()).thenReturn(PluginState.STARTED);
        when(pluginManager.getPlugin("plugin3")).thenReturn(plugin3);
        when(pluginManager.getPluginClassLoader("plugin3")).thenReturn(classLoader);

        AbstractExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();
                entries.put("plugin3", Collections.singleton(FOO_GREETING_CLASS_NAME));

                return entries;
            }

            @Override
            public Map<String, Set<String>> readClasspathStorages() {
                return Collections.emptyMap();
            }

        };

        assertFalse(instance.isCheckForExtensionDependencies());
        instance.pluginStateChanged(new PluginStateEvent(pluginManager, plugin3, PluginState.RESOLVED));
        assertTrue(instance.isCheckForExtensionDependencies());

        return instance;
    }

    private static class ExtensionClassLoader extends URLClassLoader {

        ExtensionClassLoader(URL url, ClassLoader parent) {
            super(new URL[] { url }, parent);
        }

        boolean isLoaded(String className) {
            return findLoadedClass(className) != null;
        }

    }

}
