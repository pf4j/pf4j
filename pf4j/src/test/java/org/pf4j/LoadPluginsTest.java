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
import org.pf4j.test.PluginZip;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class LoadPluginsTest {

    private DefaultPluginManager pluginManager;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() {
        pluginManager = new DefaultPluginManager(pluginsPath);
    }

    @Test
    public void load() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        assertTrue(Files.exists(pluginZip.path()));
        assertEquals(0, pluginManager.getPlugins().size());
        pluginManager.loadPlugins();

        assertTrue(Files.exists(pluginZip.path()));
        assertTrue(Files.exists(pluginZip.unzippedPath()));
        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals(pluginZip.pluginId(), pluginManager.idForPath(pluginZip.unzippedPath()));
    }

    @Test
    public void loadNonExisting() {
        assertThrows(IllegalArgumentException.class, () -> pluginManager.loadPlugin(Paths.get("nonexisting")));
    }

    @Test
    public void loadTwiceFails() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        assertNotNull(pluginManager.loadPluginFromPath(pluginZip.path()));

        assertThrows(PluginAlreadyLoadedException.class, () -> pluginManager.loadPluginFromPath(pluginZip.path()));
    }

    @Test
    public void loadPluginWithSameIdDifferentPathFails() throws Exception {
        String pluginId = "myPlugin";
        String pluginVersion = "1.2.3";
        Path plugin1Path = pluginsPath.resolve("my-plugin-1.2.3.zip");
        PluginZip plugin1 = new PluginZip.Builder(plugin1Path, pluginId)
            .pluginVersion(pluginVersion)
            .build();

        Path plugin2Path = pluginsPath.resolve("my-plugin-1.2.3-renamed.zip");
        PluginZip plugin2 = new PluginZip.Builder(plugin2Path, pluginId)
            .pluginVersion(pluginVersion)
            .build();

        // Verify the first plugin with the given id is loaded
        assertNotNull(pluginManager.loadPluginFromPath(plugin1.path()));
        Path loadedPlugin1Path = pluginManager.getPlugin(pluginId).getPluginPath();

        try {
            // Verify the second plugin is not loaded as it has the same metadata
            pluginManager.loadPluginFromPath(plugin2.path());
            fail("Expected loadPluginFromPath to fail");
        } catch (PluginRuntimeException e) {
            // Check the io of the loaded plugin remains the same
            PluginWrapper loadedPlugin = pluginManager.getPlugin(pluginId);
            assertThat(loadedPlugin.getPluginPath(), equalTo(loadedPlugin1Path));
            // Check the message includes relevant information
            String message = e.getMessage();
            assertThat(message, startsWith("There is an already loaded plugin"));
            assertThat(message, containsString(pluginId));
            assertThat(message, containsString("my-plugin-1.2.3-renamed"));
        }
    }

    /**
     * This test verifies the behaviour as of PF4J 2.x, where plugins of different
     * versions but with the pluginId cannot be loaded correctly because the API
     * uses pluginId as the unique identifier of the loaded plugin.
     */
    @Test
    public void loadPluginWithSameIdDifferentVersionsFails() throws Exception {
        String pluginId = "myPlugin";
        String plugin1Version = "1.2.3";
        Path plugin1Path = pluginsPath.resolve("my-plugin-1.2.3.zip");
        PluginZip plugin1 = new PluginZip.Builder(plugin1Path, pluginId)
            .pluginVersion(plugin1Version)
            .build();

        String plugin2Version = "2.0.0";
        Path plugin2Path = pluginsPath.resolve("my-plugin-2.0.0.zip");
        PluginZip plugin2 = new PluginZip.Builder(plugin2Path, pluginId)
            .pluginVersion(plugin2Version)
            .build();

        // Verify the first plugin with the given id is loaded
        assertNotNull(pluginManager.loadPluginFromPath(plugin1.path()));
        Path loadedPlugin1Path = pluginManager.getPlugin(pluginId).getPluginPath();
        try {
            // Verify the second plugin is not loaded as it has the same pluginId
            pluginManager.loadPluginFromPath(plugin2.path());
            fail("Expected loadPluginFromPath to fail");
        } catch (PluginRuntimeException e) {
            // Check the io and version of the loaded plugin remain the same
            PluginWrapper loadedPlugin = pluginManager.getPlugin(pluginId);
            assertThat(loadedPlugin.getPluginPath(), equalTo(loadedPlugin1Path));
            assertThat(loadedPlugin.getDescriptor().getVersion(), equalTo(plugin1Version));
        }
    }

    @Test
    public void loadUnloadLoad() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugins();

        assertEquals(1, pluginManager.getPlugins().size());
        assertTrue(pluginManager.unloadPlugin(pluginManager.idForPath(pluginZip.unzippedPath())));
        // duplicate check
        assertNull(pluginManager.idForPath(pluginZip.unzippedPath()));
        // Double unload ok
        assertFalse(pluginManager.unloadPlugin(pluginManager.idForPath(pluginZip.unzippedPath())));
        assertNotNull(pluginManager.loadPlugin(pluginZip.unzippedPath()));
    }

    @Test
    public void upgrade() throws Exception {
        String pluginId = "myPlugin";

        new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), pluginId)
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals(1, pluginManager.getStartedPlugins().size());

        PluginZip pluginZip2 = new PluginZip.Builder(pluginsPath.resolve("my-plugin-2.0.0.ZIP"), pluginId)
            .pluginVersion("2.0.0")
            .build();

        assertEquals("1.2.3", pluginManager.getPlugin(pluginId).getDescriptor().getVersion());

        pluginManager.unloadPlugin(pluginId);
        pluginManager.loadPlugin(pluginZip2.path()); // or `pluginManager.loadPlugins();`
        pluginManager.startPlugin(pluginId);

        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals("2.0.0", pluginManager.getPlugin(pluginId).getDescriptor().getVersion());
        assertEquals("2.0.0", pluginManager.getStartedPlugins().get(0).getDescriptor().getVersion());
    }

    @Test
    public void getRoot() {
        assertEquals(pluginsPath, pluginManager.getPluginsRoot());
    }

    @Test
    public void getRoots() {
        assertEquals(Collections.singletonList(pluginsPath), pluginManager.getPluginsRoots());
    }

    @Test
    public void notAPlugin() {
        pluginsPath.resolve("not-a-zip");

        pluginManager.loadPlugins();

        assertEquals(0, pluginManager.getPlugins().size());
    }

    @Test
    public void deletePlugin() throws Exception {
        PluginZip pluginZip1 = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        PluginZip pluginZip3 = new PluginZip.Builder(pluginsPath.resolve("other-3.0.0.Zip"), "other")
            .pluginVersion("3.0.0")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(2, pluginManager.getPlugins().size());

        pluginManager.deletePlugin(pluginZip1.pluginId());

        assertEquals(1, pluginManager.getPlugins().size());
        assertFalse(Files.exists(pluginZip1.path()));
        assertFalse(Files.exists(pluginZip1.unzippedPath()));
        assertTrue(Files.exists(pluginZip3.path()));
        assertTrue(Files.exists(pluginZip3.unzippedPath()));
    }

}
