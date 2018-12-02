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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.rules.TemporaryFolder;
import org.pf4j.plugin.PluginZip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultPluginManagerTest {

    private static final String CONFIG_DIR_PROPERTY_NAME = "pf4j.pluginsConfigDir";

    private DefaultPluginManager pluginManager;
    private DefaultPluginDescriptor pluginDescriptor;
    private PluginWrapper pluginWrapper;
    private Path pluginsPath;

    @Rule
    public TemporaryFolder pluginsFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        pluginsPath = pluginsFolder.getRoot().toPath();
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

    @After
    public void tearDown() {
        pluginManager = null;
        pluginDescriptor = null;
        pluginWrapper = null;
    }

    @Test
    public void validateOK() throws PluginException {
        pluginManager.validatePluginDescriptor(pluginDescriptor);
    }

    @Test(expected = PluginException.class)
    public void validateFailsOnId() throws PluginException {
        pluginDescriptor.setPluginId("");
        pluginManager.validatePluginDescriptor(pluginDescriptor);
    }

    @Test(expected = PluginException.class)
    public void validateFailsOnVersion() throws PluginException {
        pluginDescriptor.setPluginVersion(null);
        pluginManager.validatePluginDescriptor(pluginDescriptor);
    }

    @Test
    public void validateNoPluginClass() throws PluginException {
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
        new PluginZip.Builder(pluginsFolder.newFile("my-plugin-1.2.3.zip"), "myPlugin")
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

    public static class TestSystemPropertiesProvided {

        private static final String CONFIG_DIR_PATH = "/test/value";
        @Rule
        public final ProvideSystemProperty pluginConfigPropertySet = new ProvideSystemProperty(CONFIG_DIR_PROPERTY_NAME, CONFIG_DIR_PATH);

        private DefaultPluginManager pluginManager = new DefaultPluginManager();

        @Test
        public void shouldReturnPluginConfigDirectoryIfProvided() {
            Path path = pluginManager.getPluginsConfigRoot();

            assertEquals(CONFIG_DIR_PATH, path.toString());
        }
    }

    public static class TestSystemPropertiesNotProvided {

        @Rule
        public final ClearSystemProperties pluginConfigPropertyCleared = new ClearSystemProperties(CONFIG_DIR_PROPERTY_NAME);

        private DefaultPluginManager pluginManager = new DefaultPluginManager();

        @Test
        public void shouldReturnPluginDirectoryIfConfigDirectoryNotProvided() {
            Path path = pluginManager.getPluginsConfigRoot();

            assertEquals("plugins", path.toString());
        }
    }

}
