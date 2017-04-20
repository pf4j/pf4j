/*
 * Copyright 2015 Decebal Suiu
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
package ro.fortsoft.pf4j;

import com.github.zafarkhaja.semver.Version;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ro.fortsoft.pf4j.util.SemVerUtils.versionMatches;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
public class ManifestPluginDescriptorFinderTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        Charset charset = Charset.forName("UTF-8");

        Path pluginPath = testFolder.newFolder("test-plugin-1", "classes", "META-INF").toPath();
        Files.write(pluginPath.resolve("extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(pluginPath.resolve("MANIFEST.MF"), getPlugin1Manifest(), charset);

        pluginPath = testFolder.newFolder("test-plugin-2", "classes", "META-INF").toPath();
        Files.write(pluginPath.resolve("extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(pluginPath.resolve("MANIFEST.MF"), getPlugin2Manifest(), charset);

        // empty plugin
        testFolder.newFolder("test-plugin-3");

        // no plugin class
        pluginPath = testFolder.newFolder("test-plugin-4", "classes", "META-INF").toPath();
        Files.write(pluginPath.resolve("extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(pluginPath.resolve("MANIFEST.MF"), getPlugin4Manifest(), charset);

        // no plugin version
        pluginPath = testFolder.newFolder("test-plugin-5", "classes", "META-INF").toPath();
        Files.write(pluginPath.resolve("extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(pluginPath.resolve("MANIFEST.MF"), getPlugin5Manifest(), charset);

        // no plugin id
        pluginPath = testFolder.newFolder("test-plugin-6", "classes", "META-INF").toPath();
        Files.write(pluginPath.resolve("extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(pluginPath.resolve("MANIFEST.MF"), getPlugin6Manifest(), charset);
    }

    /**
     * Test of {@link DefaultPluginDescriptorFinder#find(Path)} method.
     */
    @Test
    public void testFind() throws Exception {
        PluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new DefaultPluginClasspath());

        PluginDescriptor plugin1 = instance.find(getPluginsRoot().resolve("test-plugin-1"));
        PluginDescriptor plugin2 = instance.find(getPluginsRoot().resolve("test-plugin-2"));

        assertEquals("test-plugin-1", plugin1.getPluginId());
        assertEquals("Test Plugin 1", plugin1.getPluginDescription());
        assertEquals("ro.fortsoft.pf4j.plugin.TestPlugin", plugin1.getPluginClass());
        assertEquals(Version.valueOf("0.0.1"), plugin1.getVersion());
        assertEquals("Decebal Suiu", plugin1.getProvider());
        assertEquals(2, plugin1.getDependencies().size());
        assertEquals("test-plugin-2", plugin1.getDependencies().get(0).getPluginId());
        assertEquals("test-plugin-3", plugin1.getDependencies().get(1).getPluginId());
        assertEquals("~1.0", plugin1.getDependencies().get(1).getPluginVersionSupport());
        assertEquals("Apache-2.0", plugin1.getLicense());
        assertTrue(versionMatches(plugin1.getRequires(), "1.0.0"));

        assertEquals("test-plugin-2", plugin2.getPluginId());
        assertEquals("", plugin2.getPluginDescription());
        assertEquals("ro.fortsoft.pf4j.plugin.TestPlugin", plugin2.getPluginClass());
        assertEquals(Version.valueOf("0.0.1"), plugin2.getVersion());
        assertEquals("Decebal Suiu", plugin2.getProvider());
        assertEquals(0, plugin2.getDependencies().size());
        assertTrue(versionMatches(plugin2.getRequires(),"1.0.0"));
    }

    /**
     * Test of {@link DefaultPluginDescriptorFinder#find(Path)} method.
     */
    @Test(expected = PluginException.class)
    public void testFindNotFound() throws Exception {
        PluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new DefaultPluginClasspath());
        instance.find(getPluginsRoot().resolve("test-plugin-3"));
    }

    private List<String> getPlugin1Manifest() {
        String[] lines = new String[] {
            "Manifest-Version: 1.0\n"
            + "Implementation-Title: Test Plugin #1\n"
            + "Implementation-Version: 0.10.0-SNAPSHOT\n"
            + "Archiver-Version: Plexus Archiver\n"
            + "Built-By: Mario Franco\n"
            + "Specification-Title: Test Plugin #1\n"
            + "Implementation-Vendor-Id: ro.fortsoft.pf4j.demo\n"
            + "Plugin-Version: 0.0.1\n"
            + "Plugin-Id: test-plugin-1\n"
            + "Plugin-Description: Test Plugin 1\n"
            + "Plugin-Provider: Decebal Suiu\n"
            + "Plugin-Class: ro.fortsoft.pf4j.plugin.TestPlugin\n"
            + "Plugin-Dependencies: test-plugin-2,test-plugin-3@~1.0\n"
            + "Plugin-Requires: *\n"
            + "Plugin-License: Apache-2.0\n"
            + "Created-By: Apache Maven 3.0.5\n"
            + "Build-Jdk: 1.8.0_45\n"
            + "Specification-Version: 0.10.0-SNAPSHOT\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private List<String> getPlugin2Manifest() {
        String[] lines = new String[] {
            "Manifest-Version: 1.0\n"
            + "Plugin-Dependencies: \n"
            + "Implementation-Title: Test Plugin #2\n"
            + "Implementation-Version: 0.10.0-SNAPSHOT\n"
            + "Archiver-Version: Plexus Archiver\n"
            + "Built-By: Mario Franco\n"
            + "Specification-Title: Test Plugin #2\n"
            + "Implementation-Vendor-Id: ro.fortsoft.pf4j.demo\n"
            + "Plugin-Version: 0.0.1\n"
            + "Plugin-Id: test-plugin-2\n"
            + "Plugin-Provider: Decebal Suiu\n"
            + "Plugin-Class: ro.fortsoft.pf4j.plugin.TestPlugin\n"
            + "Created-By: Apache Maven 3.0.5\n"
            + "Build-Jdk: 1.8.0_45\n"
            + "Specification-Version: 0.10.0-SNAPSHOT\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private List<String> getPlugin4Manifest() {
        String[] lines = new String[] {
            "Manifest-Version: 1.0\n"
            + "Implementation-Title: Test Plugin #4\n"
            + "Implementation-Version: 0.10.0-SNAPSHOT\n"
            + "Archiver-Version: Plexus Archiver\n"
            + "Built-By: Mario Franco\n"
            + "Specification-Title: Test Plugin #4\n"
            + "Implementation-Vendor-Id: ro.fortsoft.pf4j.demo\n"
            + "Plugin-Version: 0.0.1\n"
            + "Plugin-Id: test-plugin-2\n"
            + "Plugin-Provider: Decebal Suiu\n"
            + "Created-By: Apache Maven 3.0.5\n"
            + "Build-Jdk: 1.8.0_45\n"
            + "Specification-Version: 0.10.0-SNAPSHOT\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private List<String> getPlugin5Manifest() {
        String[] lines = new String[] {
            "Manifest-Version: 1.0\n"
            + "Implementation-Title: Test Plugin #5\n"
            + "Implementation-Version: 0.10.0-SNAPSHOT\n"
            + "Archiver-Version: Plexus Archiver\n"
            + "Built-By: Mario Franco\n"
            + "Specification-Title: Test Plugin #5\n"
            + "Implementation-Vendor-Id: ro.fortsoft.pf4j.demo\n"
            + "Plugin-Id: test-plugin-2\n"
            + "Plugin-Provider: Decebal Suiu\n"
            + "Plugin-Class: ro.fortsoft.pf4j.plugin.TestPlugin\n"
            + "Created-By: Apache Maven 3.0.5\n"
            + "Build-Jdk: 1.8.0_45\n"
            + "Specification-Version: 0.10.0-SNAPSHOT\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private List<String> getPlugin6Manifest() {
        String[] lines = new String[] {
            "Manifest-Version: 1.0\n"
            + "Implementation-Title: Test Plugin #6\n"
            + "Implementation-Version: 0.10.0-SNAPSHOT\n"
            + "Archiver-Version: Plexus Archiver\n"
            + "Built-By: Mario Franco\n"
            + "Specification-Title: Test Plugin #6\n"
            + "Implementation-Vendor-Id: ro.fortsoft.pf4j.demo\n"
            + "Plugin-Provider: Decebal Suiu\n"
            + "Plugin-Class: ro.fortsoft.pf4j.plugin.TestPlugin\n"
            + "Created-By: Apache Maven 3.0.5\n"
            + "Build-Jdk: 1.8.0_45\n"
            + "Specification-Version: 0.10.0-SNAPSHOT\n"
            + "\n"
            + ""
        };

        return Arrays.asList(lines);
    }

    private Path getPluginsRoot() {
        return testFolder.getRoot().toPath();
    }

}
