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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PropertiesPluginDescriptorFinderTest {

    private VersionManager versionManager;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        Charset charset = Charset.forName("UTF-8");

        Path pluginPath = testFolder.newFolder("test-plugin-1").toPath();
        Files.write(pluginPath.resolve("plugin.properties"), getPlugin1Properties(), charset);

        pluginPath = testFolder.newFolder("test-plugin-2").toPath();
        Files.write(pluginPath.resolve("plugin.properties"), getPlugin2Properties(), charset);

        // empty plugin
        testFolder.newFolder("test-plugin-3");

        // no plugin class
        pluginPath = testFolder.newFolder("test-plugin-4").toPath();
        Files.write(pluginPath.resolve("plugin.properties"), getPlugin4Properties(), charset);

        // no plugin version
        pluginPath = testFolder.newFolder("test-plugin-5").toPath();
        Files.write(pluginPath.resolve("plugin.properties"), getPlugin5Properties(), charset);

        // no plugin id
        pluginPath = testFolder.newFolder("test-plugin-6").toPath();
        Files.write(pluginPath.resolve("plugin.properties"), getPlugin6Properties(), charset);

        versionManager = new DefaultVersionManager();
    }

    @Test
    public void testFind() throws Exception {
        PluginDescriptorFinder instance = new PropertiesPluginDescriptorFinder();

        PluginDescriptor plugin1 = instance.find(getPluginsRoot().resolve("test-plugin-1"));
        PluginDescriptor plugin2 = instance.find(getPluginsRoot().resolve("test-plugin-2"));

        assertEquals("test-plugin-1", plugin1.getPluginId());
        assertEquals("Test Plugin 1", plugin1.getPluginDescription());
        assertEquals("org.pf4j.plugin.TestPlugin", plugin1.getPluginClass());
        assertEquals("0.0.1", plugin1.getVersion());
        assertEquals("Decebal Suiu", plugin1.getProvider());
        assertEquals(2, plugin1.getDependencies().size());
        assertEquals("test-plugin-2", plugin1.getDependencies().get(0).getPluginId());
        assertEquals("test-plugin-3", plugin1.getDependencies().get(1).getPluginId());
        assertEquals("~1.0", plugin1.getDependencies().get(1).getPluginVersionSupport());
        assertEquals("Apache-2.0", plugin1.getLicense());
        assertEquals(">=1", plugin1.getRequires());
        assertTrue(versionManager.checkVersionConstraint("1.0.0", plugin1.getRequires()));
        assertFalse(versionManager.checkVersionConstraint("0.1.0", plugin1.getRequires()));

        assertEquals("test-plugin-2", plugin2.getPluginId());
        assertEquals("", plugin2.getPluginDescription());
        assertEquals("org.pf4j.plugin.TestPlugin", plugin2.getPluginClass());
        assertEquals("0.0.1", plugin2.getVersion());
        assertEquals("Decebal Suiu", plugin2.getProvider());
        assertEquals(0, plugin2.getDependencies().size());
        assertEquals("*", plugin2.getRequires()); // Default is *
        assertTrue(versionManager.checkVersionConstraint("1.0.0", plugin2.getRequires()));
    }

    @Test(expected = PluginException.class)
    public void testNotFound() throws Exception {
        PluginDescriptorFinder instance = new PropertiesPluginDescriptorFinder();
        instance.find(getPluginsRoot().resolve("test-plugin-3"));
    }

    @Test
    public void findInJar() throws Exception {
        PluginZip pluginJar = new PluginZip.Builder(testFolder.newFile("my-plugin-1.2.3.jar"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        assertTrue(Files.exists(pluginJar.path()));

        PluginDescriptorFinder instance = new PropertiesPluginDescriptorFinder();
        PluginDescriptor pluginDescriptor = instance.find(pluginJar.path());
        assertNotNull(pluginDescriptor);
        assertEquals("myPlugin", pluginJar.pluginId());
        assertEquals("1.2.3", pluginJar.pluginVersion());
    }

    private List<String> getPlugin1Properties() {
        String[] lines = new String[] {
            "plugin.id=test-plugin-1\n"
            + "plugin.version=0.0.1\n"
            + "plugin.description=Test Plugin 1\n"
            + "plugin.provider=Decebal Suiu\n"
            + "plugin.class=org.pf4j.plugin.TestPlugin\n"
            + "plugin.dependencies=test-plugin-2,test-plugin-3@~1.0\n"
            + "plugin.requires=>=1\n"
            + "plugin.license=Apache-2.0\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private List<String> getPlugin2Properties() {
        String[] lines = new String[] {
            "plugin.id=test-plugin-2\n"
            + "plugin.version=0.0.1\n"
            + "plugin.provider=Decebal Suiu\n"
            + "plugin.class=org.pf4j.plugin.TestPlugin\n"
            + "plugin.dependencies=\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private List<String> getPlugin4Properties() {
        String[] lines = new String[] {
            "plugin.id=test-plugin-2\n"
            + "plugin.version=0.0.1\n"
            + "plugin.provider=Decebal Suiu\n"
            + "plugin.dependencies=\n"
            + "plugin.requires=*\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private List<String> getPlugin5Properties() {
        String[] lines = new String[] {
            "plugin.id=test-plugin-2\n"
            + "plugin.provider=Decebal Suiu\n"
            + "plugin.class=org.pf4j.plugin.TestPlugin\n"
            + "plugin.dependencies=\n"
            + "plugin.requires=*\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private List<String> getPlugin6Properties() {
        String[] lines = new String[] {
            "plugin.version=0.0.1\n"
            + "plugin.provider=Decebal Suiu\n"
            + "plugin.class=org.pf4j.plugin.TestPlugin\n"
            + "plugin.dependencies=\n"
            + "plugin.requires=*\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private Path getPluginsRoot() {
        return testFolder.getRoot().toPath();
    }

}
