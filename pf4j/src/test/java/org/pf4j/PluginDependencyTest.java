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
import org.pf4j.plugin.PluginZip;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mario Franco
 */
public class PluginDependencyTest {

    private DefaultPluginManager pluginManager;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() {
        pluginManager = new DefaultPluginManager(pluginsPath);
    }

    /**
     * Test of getPluginId method, of class PluginDependency.
     */
    @Test
    public void testPluginDependecy() {
        PluginDependency instance = new PluginDependency("test");
        PluginDependency instance2 = new PluginDependency("test");
        assertEquals(instance, instance2);
        assertEquals("test", instance.getPluginId());
        assertEquals("*", instance.getPluginVersionSupport());
        assertFalse(instance.isOptional());

        instance = new PluginDependency("test@");
        instance2 = new PluginDependency("test@");
        assertEquals(instance, instance2);
        assertEquals("test", instance.getPluginId());
        assertEquals("*", instance.getPluginVersionSupport());
        assertFalse(instance.isOptional());

        instance = new PluginDependency("test?");
        instance2 = new PluginDependency("test?");
        assertEquals(instance, instance2);
        assertEquals("test", instance.getPluginId());
        assertEquals("*", instance.getPluginVersionSupport());
        assertTrue(instance.isOptional());

        instance = new PluginDependency("test?@");
        instance2 = new PluginDependency("test?@");
        assertEquals(instance, instance2);
        assertEquals("test", instance.getPluginId());
        assertEquals("*", instance.getPluginVersionSupport());
        assertTrue(instance.isOptional());

        instance = new PluginDependency("test@1.0");
        instance2 = new PluginDependency("test@1.0");
        assertEquals(instance, instance2);
        assertEquals("test", instance.getPluginId());
        assertEquals("1.0", instance.getPluginVersionSupport());
        assertFalse(instance.isOptional());
        assertEquals("PluginDependency [pluginId=test, pluginVersionSupport=1.0, optional=false]", instance.toString());

        instance = new PluginDependency("test?@1.0");
        instance2 = new PluginDependency("test?@1.0");
        assertEquals(instance, instance2);
        assertEquals("test", instance.getPluginId());
        assertEquals("1.0", instance.getPluginVersionSupport());
        assertTrue(instance.isOptional());
        assertEquals("PluginDependency [pluginId=test, pluginVersionSupport=1.0, optional=true]", instance.toString());
    }

    @Test
    public void dependentStop() throws Exception {
        // B depends on A
        PluginZip pluginA = new PluginZip.Builder(pluginsPath.resolve("A-plugin-1.2.3.zip"), "plugin.a")
            .pluginVersion("1.2.3").build();

        PluginZip pluginB = new PluginZip.Builder(pluginsPath.resolve("B-plugin-1.2.3.zip"), "plugin.b")
            .pluginDependencies("plugin.a")
            .pluginVersion("1.2.3").build();

        pluginManager.loadPlugins();
        assertEquals(2, pluginManager.getPlugins().size());

        pluginManager.startPlugins();
        assertEquals(2, pluginManager.getStartedPlugins().size());

        // stop A, both A and B should be stopped
        pluginManager.stopPlugin("plugin.a");
        assertEquals(0, pluginManager.getStartedPlugins().size());

        // start all again
        pluginManager.startPlugins();
        assertEquals(2, pluginManager.getStartedPlugins().size());

        // dependent info should be kept. #394
        pluginManager.stopPlugin("plugin.a");
        assertEquals(0, pluginManager.getStartedPlugins().size());
    }

}
