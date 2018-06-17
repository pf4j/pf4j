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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pf4j.plugin.PluginZip;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        new PluginZip.Builder(pluginsFolder.newFile("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals(1, pluginManager.getStartedPlugins().size());

        PluginZip pluginZip2 = new PluginZip.Builder(pluginsFolder.newFile("my-plugin-2.0.0.ZIP"), "myPlugin")
            .pluginVersion("2.0.0")
            .build();

        assertEquals("1.2.3", pluginManager.getPlugin(pluginZip2.pluginId()).getDescriptor().getVersion());

        pluginManager.loadPlugins();
        pluginManager.startPlugin(pluginZip2.pluginId());

        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals("2.0.0", pluginManager.getPlugin(pluginZip2.pluginId()).getDescriptor().getVersion());
        assertEquals("2.0.0", pluginManager.getStartedPlugins().get(1).getDescriptor().getVersion());
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
