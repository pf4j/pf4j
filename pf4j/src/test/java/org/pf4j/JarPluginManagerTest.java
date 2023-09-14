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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.test.PluginJar;
import org.pf4j.test.TestExtension;
import org.pf4j.test.TestExtensionPoint;
import org.pf4j.test.TestPlugin;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JarPluginManagerTest {

    private PluginJar pluginJar;
    private JarPluginManager pluginManager;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() throws IOException {
        pluginJar = new PluginJar.Builder(pluginsPath.resolve("test-plugin.jar"), "test-plugin")
            .pluginClass(TestPlugin.class.getName())
            .pluginVersion("1.2.3")
            .extension(TestExtension.class.getName())
            .build();

        pluginManager = new JarPluginManager(pluginsPath);
    }

    @AfterEach
    public void tearDown() {
        pluginManager.unloadPlugins();

        pluginJar = null;
        pluginManager = null;
    }

    @Test
    public void getExtensions() {
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        List<TestExtensionPoint> extensions = pluginManager.getExtensions(TestExtensionPoint.class);
        assertEquals(1, extensions.size());

        String something = extensions.get(0).saySomething();
        assertEquals(new TestExtension().saySomething(), something);
    }

    @Test
    public void unloadPlugin() throws Exception {
        pluginManager.loadPlugins();

        assertEquals(1, pluginManager.getPlugins().size());

        boolean unloaded = pluginManager.unloadPlugin(pluginJar.pluginId());
        assertTrue(unloaded);

        assertTrue(pluginJar.file().exists());
    }

    @Test
    public void deletePlugin() throws Exception {
        pluginManager.loadPlugins();

        assertEquals(1, pluginManager.getPlugins().size());

        boolean deleted = pluginManager.deletePlugin(pluginJar.pluginId());
        assertTrue(deleted);

        assertFalse(pluginJar.file().exists());
    }

    @Test
    public void releaseBrokenJar() throws IOException {
        PluginJar pluginZip = new PluginJar.Builder(pluginsPath.resolve("test.jar"), "test")
            .pluginVersion("1.2.3")
            .pluginClass("invalidClass")
            .build();

        pluginManager.loadPlugins();
        final Path pluginPath = pluginManager.getPlugin(pluginZip.pluginId()).getPluginPath();

        try {
            pluginManager.startPlugin(pluginZip.pluginId());
        } catch (final Exception exceptionStartPlugin) {
            Assertions.assertThrows(FileSystemException.class, () -> Files.delete(pluginPath));

            // Try to remove the plugin if it cannot be started
            try {
                pluginManager.unloadPlugin(pluginZip.pluginId());
            } catch (final Exception ignored2) {
            }
            Assertions.assertDoesNotThrow(() -> Files.delete(pluginPath));
        }
    }
}
