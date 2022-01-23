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

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.testing.compile.Compilation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.test.FailTestPlugin;
import org.pf4j.test.TestExtensionPoint;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void findFailType() {
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
        List<ExtensionWrapper<FailTestPlugin>> list = instance.find(FailTestPlugin.class);
        assertEquals(0, list.size());
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class)}.
     */
    @Test
    void findFromClasspath() {
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
                bucket.add("org.pf4j.test.FailTestExtension");
                entries.put(null, bucket);

                return entries;
            }

        };

        List<ExtensionWrapper<TestExtensionPoint>> list = instance.find(TestExtensionPoint.class);
        assertEquals(2, list.size());
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class, String)}.
     */
    @Test
    void findFromPlugin() {
        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();

                Set<String> bucket = new HashSet<>();
                bucket.add("org.pf4j.test.TestExtension");
                bucket.add("org.pf4j.test.FailTestExtension");
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
        assertEquals(2, list.size());

        list = instance.find(TestExtensionPoint.class, "plugin1");
        assertEquals(2, list.size());

        list = instance.find(TestExtensionPoint.class, "plugin2");
        assertEquals(0, list.size());
    }

    /**
     * Test of {@link AbstractExtensionFinder#findClassNames(String)}.
     */
    @Test
    void findClassNames() {
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
    void findExtensionWrappersFromPluginId() {
        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();

                Set<String> bucket = new HashSet<>();
                bucket.add("org.pf4j.test.TestExtension");
                bucket.add("org.pf4j.test.FailTestExtension");
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

        List<ExtensionWrapper<?>> plugin1Result = instance.find("plugin1");
        assertEquals(2, plugin1Result.size());

        List<ExtensionWrapper> plugin2Result = instance.find("plugin2");
        assertEquals(0, plugin2Result.size());

        List<ExtensionWrapper> plugin3Result = instance.find(UUID.randomUUID().toString());
        assertEquals(0, plugin3Result.size());
    }

    @Test
    void findExtensionAnnotation() throws Exception {
        Compilation compilation = javac().compile(ExtensionAnnotationProcessorTest.Greeting,
            ExtensionAnnotationProcessorTest.WhazzupGreeting);
        assertThat(compilation).succeededWithoutWarnings();
        ImmutableList<JavaFileObject> generatedFiles = compilation.generatedFiles();
        assertEquals(2, generatedFiles.size());

        JavaFileObjectClassLoader classLoader = new JavaFileObjectClassLoader();
        Map<String, Class<?>> loadedClasses = classLoader.loadClasses(new ArrayList<>(generatedFiles));
        Class<?> clazz = loadedClasses.get("test.WhazzupGreeting");
        Extension extension = AbstractExtensionFinder.findExtensionAnnotation(clazz);
        assertNotNull(extension);
    }

    @Test
    void findExtensionAnnotationThatMissing() throws Exception {
        Compilation compilation = javac().compile(ExtensionAnnotationProcessorTest.Greeting,
            ExtensionAnnotationProcessorTest.SpinnakerExtension_NoExtension,
            ExtensionAnnotationProcessorTest.WhazzupGreeting_SpinnakerExtension);
        assertThat(compilation).succeededWithoutWarnings();
        ImmutableList<JavaFileObject> generatedFiles = compilation.generatedFiles();
        assertEquals(3, generatedFiles.size());

        JavaFileObjectClassLoader classLoader = new JavaFileObjectClassLoader();
        Map<String, Class<?>> loadedClasses = classLoader.loadClasses(new ArrayList<>(generatedFiles));
        Class<?> clazz = loadedClasses.get("test.WhazzupGreeting");
        Extension extension = AbstractExtensionFinder.findExtensionAnnotation(clazz);
        assertNull(extension);
    }

   static class JavaFileObjectClassLoader extends ClassLoader {

        public Map<String, Class<?>> loadClasses(List<JavaFileObject> classes) throws IOException {
            // Sort generated ".class" by lastModified field
            classes.sort(Comparator.comparingLong(JavaFileObject::getLastModified));

            // Load classes
            Map<String, Class<?>> loadedClasses = new HashMap<>(classes.size());
            for (JavaFileObject clazz : classes) {
                String className = getClassName(clazz);
                byte[] data = ByteStreams.toByteArray(clazz.openInputStream());
                Class<?> loadedClass = defineClass(className, data,0, data.length);
                loadedClasses.put(className, loadedClass);
            }

            return loadedClasses;
        }

        private static String getClassName(JavaFileObject object) {
            String name = object.getName();
            // Remove "/CLASS_OUT/" from head and ".class" from tail
            name = name.substring(14, name.length() - 6);
            name = name.replace('/', '.');

            return name;
        }

    }

}
