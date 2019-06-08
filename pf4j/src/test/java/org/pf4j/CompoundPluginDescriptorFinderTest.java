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
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.plugin.PluginJar;
import org.pf4j.plugin.PluginZip;
import org.pf4j.plugin.TestPlugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Decebal Suiu
 */
public class CompoundPluginDescriptorFinderTest {

    @TempDir
    Path pluginsPath;

    @Test
    public void add() {
        CompoundPluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder();
        assertEquals(0, descriptorFinder.size());

        descriptorFinder.add(new PropertiesPluginDescriptorFinder());
        assertEquals(1, descriptorFinder.size());
    }

    @Test
    public void find() throws Exception {
        Path pluginPath = Files.createDirectories(pluginsPath.resolve("test-plugin-1"));
        storePropertiesToPath(getPlugin1Properties(), pluginPath);

        PluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder()
            .add(new PropertiesPluginDescriptorFinder());

        PluginDescriptor pluginDescriptor = descriptorFinder.find(pluginPath);
        assertNotNull(pluginDescriptor);
        assertEquals("test-plugin-1", pluginDescriptor.getPluginId());
        assertEquals("0.0.1", pluginDescriptor.getVersion());
    }

    @Test
    public void findInJar() throws Exception {
        PluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder()
            .add(new ManifestPluginDescriptorFinder());

        PluginJar pluginJar = new PluginJar.Builder(pluginsPath.resolve("my-plugin-1.2.3.jar"), "myPlugin")
            .pluginClass(TestPlugin.class.getName())
            .pluginVersion("1.2.3")
            .build();

        PluginDescriptor pluginDescriptor = descriptorFinder.find(pluginJar.path());
        assertNotNull(pluginDescriptor);
        assertEquals("myPlugin", pluginJar.pluginId());
        assertEquals(TestPlugin.class.getName(), pluginJar.pluginClass());
        assertEquals("1.2.3", pluginJar.pluginVersion());
    }

    @Test
    public void testNotFound() {
        PluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder();
        assertThrows(PluginRuntimeException.class, () -> descriptorFinder.find(pluginsPath.resolve("test-plugin-3")));
    }

    @Test
    public void testSpaceCharacterInFileName() throws Exception {
        PluginDescriptorFinder descriptorFinder = new CompoundPluginDescriptorFinder()
            .add(new ManifestPluginDescriptorFinder());

        PluginJar pluginJar = new PluginJar.Builder(pluginsPath.resolve("my plugin-1.2.3.jar"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        PluginDescriptor pluginDescriptor = descriptorFinder.find(pluginJar.path());
        assertNotNull(pluginDescriptor);
    }

    private Properties getPlugin1Properties() {
        Map<String, String> map = new LinkedHashMap<>(7);
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_ID, "test-plugin-1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_CLASS, TestPlugin.class.getName());
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_VERSION, "0.0.1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_PROVIDER, "Decebal Suiu");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_DEPENDENCIES, "test-plugin-2,test-plugin-3@~1.0");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_REQUIRES, ">=1");
        map.put(PropertiesPluginDescriptorFinder.PLUGIN_LICENSE, "Apache-2.0");

        return PluginZip.createProperties(map);
    }

    private void storePropertiesToPath(Properties properties, Path pluginPath) throws IOException {
        Path path = pluginPath.resolve(PropertiesPluginDescriptorFinder.DEFAULT_PROPERTIES_FILE_NAME);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
            properties.store(writer, "");
        }
    }

}
