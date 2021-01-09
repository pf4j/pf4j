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
import org.pf4j.test.PluginJar;
import org.pf4j.test.PluginZip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultPluginManagerTest {

    private DefaultPluginManager pluginManager;
    private DefaultPluginDescriptor pluginDescriptor;
    private PluginWrapper pluginWrapper;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() throws IOException {
        pluginManager = new DefaultPluginManager(pluginsPath);

        pluginDescriptor = new DefaultPluginDescriptor();
        pluginDescriptor.setPluginId("myPlugin");
        pluginDescriptor.setPluginVersion("1.2.3");
        pluginDescriptor.setPluginDescription("My plugin");
        pluginDescriptor.setDependencies("bar, baz");
        pluginDescriptor.setProvider("Me");
        pluginDescriptor.setRequires("5.0.0");

        pluginWrapper = new PluginWrapper(pluginManager, pluginDescriptor, Files.createTempDirectory("test"), getClass().getClassLoader());
    }

    @AfterEach
    public void tearDown() {
        pluginManager = null;
        pluginDescriptor = null;
        pluginWrapper = null;
    }

    @Test
    public void validateOK() {
        pluginManager.validatePluginDescriptor(pluginDescriptor);
    }

    @Test
    public void validateFailsOnId() {
        pluginDescriptor.setPluginId("");
        assertThrows(PluginRuntimeException.class, () -> pluginManager.validatePluginDescriptor(pluginDescriptor));
    }

    @Test
    public void validateFailsOnVersion() {
        pluginDescriptor.setPluginVersion(null);
        assertThrows(PluginRuntimeException.class, () -> pluginManager.validatePluginDescriptor(pluginDescriptor));
    }

    @Test
    public void validateNoPluginClass() {
        pluginManager.validatePluginDescriptor(pluginDescriptor);
        assertEquals(Plugin.class.getName(), pluginDescriptor.getPluginClass());
    }

    @Test
    public void isPluginValid() {
        // By default accept all since system version not given
        assertTrue(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("1.0.0");
        assertFalse(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("5.0.0");
        assertTrue(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("6.0.0");
        assertTrue(pluginManager.isPluginValid(pluginWrapper));
    }

    @Test
    public void isPluginValidAllowExact() {
        pluginManager.setExactVersionAllowed(true);

        // By default accept all since system version not given
        assertTrue(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("1.0.0");
        assertFalse(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("5.0.0");
        assertTrue(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("6.0.0");
        assertFalse(pluginManager.isPluginValid(pluginWrapper));
    }

    @Test
    public void testDefaultExactVersionAllowed() {
        assertFalse(pluginManager.isExactVersionAllowed());
    }

    /**
     * Test that a disabled plugin doesn't start.
     * See https://github.com/pf4j/pf4j/issues/223.
     */
    @Test
    public void testPluginDisabledNoStart() throws IOException {
        new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        final PluginStatusProvider statusProvider = mock(PluginStatusProvider.class);
        when(statusProvider.isPluginDisabled("myPlugin")).thenReturn(true);

        PluginManager pluginManager = new DefaultPluginManager(pluginsPath) {

            protected PluginStatusProvider createPluginStatusProvider() {
                return statusProvider;
            }

        };

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals(0, pluginManager.getStartedPlugins().size());

        PluginWrapper plugin = pluginManager.getPlugin("myPlugin");
        assertSame(PluginState.DISABLED, plugin.getPluginState());
    }

    @Test
    public void deleteZipPlugin() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugin(pluginZip.path());
        pluginManager.startPlugin(pluginZip.pluginId());

        assertEquals(1, pluginManager.getPlugins().size());

        boolean deleted = pluginManager.deletePlugin(pluginZip.pluginId());
        assertTrue(deleted);

        assertFalse(pluginZip.file().exists());
    }

    @Test
    public void deleteJarPlugin() throws Exception {
        PluginJar pluginJar = new PluginJar.Builder(pluginsPath.resolve("my-plugin-1.2.3.jar"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugin(pluginJar.path());
        pluginManager.startPlugin(pluginJar.pluginId());

        assertEquals(1, pluginManager.getPlugins().size());

        boolean deleted = pluginManager.deletePlugin(pluginJar.pluginId());
        assertTrue(deleted);

        assertFalse(pluginJar.file().exists());
    }

}
