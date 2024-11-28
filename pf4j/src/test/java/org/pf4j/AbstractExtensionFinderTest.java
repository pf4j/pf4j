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

import kotlin.sequences.Sequence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.test.JavaFileObjectClassLoader;
import org.pf4j.test.JavaFileObjectUtils;
import org.pf4j.test.JavaSources;
import org.pf4j.test.TestExtension;
import org.pf4j.test.TestExtensionPoint;

import javax.tools.JavaFileObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Mario Franco
 */
class AbstractExtensionFinderTest {

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

}
