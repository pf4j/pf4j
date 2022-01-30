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
import org.pf4j.test.PluginProperties;
import org.pf4j.test.PluginZip;
import org.pf4j.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadPluginsFromMultipleRootsTest {

    private DefaultPluginManager pluginManager;

    Path pluginsPath1;
    Path pluginsPath2;

    @BeforeEach
    void setUp() throws IOException {
        pluginsPath1 = Files.createTempDirectory("junit-pf4j-");
        pluginsPath2 = Files.createTempDirectory("junit-pf4j-");
        pluginManager = new DefaultPluginManager(pluginsPath1, pluginsPath2);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.delete(pluginsPath1);
        FileUtils.delete(pluginsPath2);
    }

    @Test
    void load() throws Exception {
        PluginProperties plugin1Properties = new PluginProperties.Builder("myPlugin")
            .pluginVersion("1.2.3")
            .build();
        PluginZip pluginZip1 = new PluginZip.Builder(pluginsPath1.resolve("my-plugin-1.2.3.zip"), plugin1Properties)
            .build();

        PluginProperties plugin2Properties = new PluginProperties.Builder("myOtherPlugin")
            .pluginVersion("4.5.6")
            .build();
        PluginZip plugin2Zip = new PluginZip.Builder(pluginsPath2.resolve("my-other-plugin-4.5.6.zip"), plugin2Properties)
            .build();

        assertTrue(Files.exists(pluginZip1.path()));
        assertEquals(0, pluginManager.getPlugins().size());

        pluginManager.loadPlugins();

        assertTrue(Files.exists(pluginZip1.path()));
        assertTrue(Files.exists(pluginZip1.unzippedPath()));
        assertTrue(Files.exists(plugin2Zip.path()));
        assertTrue(Files.exists(plugin2Zip.unzippedPath()));
        assertEquals(2, pluginManager.getPlugins().size());
        assertEquals(pluginZip1.pluginId(), pluginManager.idForPath(pluginZip1.unzippedPath()));
        assertEquals(plugin2Zip.pluginId(), pluginManager.idForPath(plugin2Zip.unzippedPath()));
    }

}
