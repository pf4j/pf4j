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

import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pf4j.plugin.PluginZip;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LoadPluginsTest {

    private DefaultPluginManager pluginManager;

    @Rule
    public TemporaryFolder pluginsFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        pluginManager = new DefaultPluginManager(pluginsFolder.getRoot().toPath());
    }

    @Test
    public void load() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsFolder.newFile("my-plugin-1.2.3.zip"), "myPlugin")
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

    @Test(expected = IllegalArgumentException.class)
    public void loadNonExisting() {
        pluginManager.loadPlugin(Paths.get("nonexisting"));
    }

    @Test(expected = PluginAlreadyLoadedException.class)
    public void loadTwiceFails() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsFolder.newFile("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        assertNotNull(pluginManager.loadPluginFromPath(pluginZip.path()));
        assertNull(pluginManager.loadPluginFromPath(pluginZip.path()));
    }

    @Test(expected = PluginAlreadyLoadedException.class)
    public void loadPluginWithSameIdDifferentPathFails() throws Exception {
        String pluginId = "myPlugin";
        String pluginVersion = "1.2.3";
        File plugin1Path = pluginsFolder.newFile("my-plugin-1.2.3.zip");
        PluginZip plugin1 = new PluginZip.Builder(plugin1Path, pluginId)
            .pluginVersion(pluginVersion)
            .build();

        File plugin2Path = pluginsFolder.newFile("my-plugin-1.2.3-renamed.zip");
        PluginZip plugin2 = new PluginZip.Builder(plugin2Path, pluginId)
            .pluginVersion(pluginVersion)
            .build();

        // Verify the first plugin with the given id is loaded
        assertNotNull(pluginManager.loadPluginFromPath(plugin1.path()));
        // Verify the second plugin is not loaded as it has the same metadata
        assertNull(pluginManager.loadPluginFromPath(plugin2.path()));
        // Check the path remains the same
        PluginWrapper loadedPlugin = pluginManager.getPlugin(pluginId);
        assertThat(loadedPlugin.getPluginPath(), equalTo(plugin1.path()));
    }

    /**
     * This test verifies the behaviour as of PF4J 2.x, where plugins of different
     * versions but with the pluginId cannot be loaded correctly because the API
     * uses pluginId as the unique identifier of the loaded plugin.
     */
    @Test(expected = PluginAlreadyLoadedException.class)
    public void loadPluginWithSameIdDifferentVersionsFails() throws Exception {
        String pluginId = "myPlugin";
        String plugin1Version = "1.2.3";
        File plugin1Path = pluginsFolder.newFile("my-plugin-1.2.3.zip");
        PluginZip plugin1 = new PluginZip.Builder(plugin1Path, pluginId)
            .pluginVersion(plugin1Version)
            .build();

        String plugin2Version = "2.0.0";
        File plugin2Path = pluginsFolder.newFile("my-plugin-2.0.0.zip");
        PluginZip plugin2 = new PluginZip.Builder(plugin2Path, pluginId)
            .pluginVersion(plugin2Version)
            .build();

        // Verify the first plugin with the given id is loaded
        assertNotNull(pluginManager.loadPluginFromPath(plugin1.path()));
        // Verify the second plugin is not loaded as it has the same metadata
        assertNull(pluginManager.loadPluginFromPath(plugin2.path()));
        // Check the path and version remain the same
        PluginWrapper loadedPlugin = pluginManager.getPlugin(pluginId);
        assertThat(loadedPlugin.getPluginPath(), equalTo(plugin1.path()));
        assertThat(loadedPlugin.getDescriptor().getVersion(), equalTo(plugin1Version));
    }

    @Test
    public void loadUnloadLoad() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsFolder.newFile("my-plugin-1.2.3.zip"), "myPlugin")
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

        new PluginZip.Builder(pluginsFolder.newFile("my-plugin-1.2.3.zip"), pluginId)
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals(1, pluginManager.getStartedPlugins().size());

        PluginZip pluginZip2 = new PluginZip.Builder(pluginsFolder.newFile("my-plugin-2.0.0.ZIP"), pluginId)
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
        assertEquals(pluginsFolder.getRoot().toPath(), pluginManager.getPluginsRoot());
    }

    @Test
    public void notAPlugin() throws Exception {
        pluginsFolder.newFile("not-a-zip");

        pluginManager.loadPlugins();

        assertEquals(0, pluginManager.getPlugins().size());
    }

    @Test
    public void deletePlugin() throws Exception {
        PluginZip pluginZip1 = new PluginZip.Builder(pluginsFolder.newFile("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        PluginZip pluginZip3 = new PluginZip.Builder(pluginsFolder.newFile("other-3.0.0.Zip"), "other")
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
