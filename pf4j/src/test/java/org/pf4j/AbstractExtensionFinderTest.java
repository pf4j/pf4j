/*
 * Copyright 2015 Decebal Suiu
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pf4j.plugin.FailTestPlugin;
import org.pf4j.plugin.TestExtensionPoint;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Mario Franco
 */
public class AbstractExtensionFinderTest {

    private PluginManager pluginManager;

    @Before
    public void setUp() {
        PluginWrapper pluginStarted = mock(PluginWrapper.class);
        when(pluginStarted.getPluginClassLoader()).thenReturn(getClass().getClassLoader());
        when(pluginStarted.getPluginState()).thenReturn(PluginState.STARTED);

        PluginWrapper pluginStopped = mock(PluginWrapper.class);
        when(pluginStopped.getPluginClassLoader()).thenReturn(getClass().getClassLoader());
        when(pluginStopped.getPluginState()).thenReturn(PluginState.STOPPED);

        pluginManager = mock(PluginManager.class);
        when(pluginManager.getPlugin(eq("plugin1"))).thenReturn(pluginStarted);
        when(pluginManager.getPlugin(eq("plugin2"))).thenReturn(pluginStopped);
        when(pluginManager.getPluginClassLoader(eq("plugin1"))).thenReturn(getClass().getClassLoader());
        when(pluginManager.getExtensionFactory()).thenReturn(new DefaultExtensionFactory());
    }

    @After
    public void tearDown() {
        pluginManager = null;
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class)}.
     */
    @Test
    public void testFindFailType() {
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
    public void testFindFromClasspath() {
        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                return Collections.emptyMap();
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

        List<ExtensionWrapper<TestExtensionPoint>> list = instance.find(TestExtensionPoint.class);
        assertEquals(2, list.size());
    }

    /**
     * Test of {@link AbstractExtensionFinder#find(Class, String)}.
     */
    @Test
    public void testFindFromPlugin() {
        ExtensionFinder instance = new AbstractExtensionFinder(pluginManager) {

            @Override
            public Map<String, Set<String>> readPluginsStorages() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();

                Set<String> bucket = new HashSet<>();
                bucket.add("org.pf4j.plugin.TestExtension");
                bucket.add("org.pf4j.plugin.FailTestExtension");
                entries.put("plugin1", bucket);
                bucket = new HashSet<>();
                bucket.add("org.pf4j.plugin.TestExtension");
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
    public void testFindClassNames() {
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

}
