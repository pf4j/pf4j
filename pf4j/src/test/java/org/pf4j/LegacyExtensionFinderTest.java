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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.test.PluginJar;
import org.pf4j.test.TestExtension;
import org.pf4j.test.TestPlugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

class LegacyExtensionFinderTest {

    @TempDir
    Path pluginsPath;

    @Test
    @EnabledOnOs(WINDOWS)
    void shouldUnlockFileAfterReadingExtensionsFromPlugin() throws Exception {
        PluginJar pluginJar = new PluginJar.Builder(pluginsPath.resolve("test-plugin.jar"), "test-plugin")
                .pluginClass(TestPlugin.class.getName())
                .pluginVersion("1.2.3")
                .extension(TestExtension.class.getName())
                .build();

        assertTrue(Files.exists(pluginJar.path()));

        PluginManager pluginManager = new JarPluginManager(pluginsPath);
        pluginManager.loadPlugins();

        assertEquals(1, pluginManager.getPlugins().size());

        LegacyExtensionFinder extensionFinder = new LegacyExtensionFinder(pluginManager);
        Map<String, Set<String>> pluginsStorages = extensionFinder.readPluginsStorages();
        assertNotNull(pluginsStorages);

        pluginManager.unloadPlugin(pluginJar.pluginId());
        boolean fileDeleted = Files.deleteIfExists(pluginJar.path());

        Set<String> pluginStorages = pluginsStorages.get(pluginJar.pluginId());
        assertNotNull(pluginStorages);
        assertEquals(1, pluginStorages.size());
        assertThat(pluginStorages, contains(TestExtension.class.getName()));
        assertTrue(fileDeleted);
        assertFalse(Files.exists(pluginJar.path()));
    }

}
