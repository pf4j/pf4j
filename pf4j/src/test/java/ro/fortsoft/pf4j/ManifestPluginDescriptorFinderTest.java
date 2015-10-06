/*
 * Copyright 2015 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Mario Franco
 */
public class ManifestPluginDescriptorFinderTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        Charset charset = Charset.forName("UTF-8");

        File plugin = testFolder.newFolder("test-plugin-1", "classes", "META-INF");
        Files.write(Paths.get(plugin.getPath(), "extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(Paths.get(plugin.getPath(), "MANIFEST.MF"), getPlugin1Manifest(), charset);

        plugin = testFolder.newFolder("test-plugin-2", "classes", "META-INF");
        Files.write(Paths.get(plugin.getPath(), "extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(Paths.get(plugin.getPath(), "MANIFEST.MF"), getPlugin2Manifest(), charset);

        // Empty Plugin
        testFolder.newFolder("test-plugin-3");

        // No Plugin Class
        plugin = testFolder.newFolder("test-plugin-4", "classes", "META-INF");
        Files.write(Paths.get(plugin.getPath(), "extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(Paths.get(plugin.getPath(), "MANIFEST.MF"), getPlugin4Manifest(), charset);

        // No Plugin Version
        plugin = testFolder.newFolder("test-plugin-5", "classes", "META-INF");
        Files.write(Paths.get(plugin.getPath(), "extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(Paths.get(plugin.getPath(), "MANIFEST.MF"), getPlugin5Manifest(), charset);

        // No Plugin Id
        plugin = testFolder.newFolder("test-plugin-6", "classes", "META-INF");
        Files.write(Paths.get(plugin.getPath(), "extensions.idx"), "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting".getBytes());
        Files.write(Paths.get(plugin.getPath(), "MANIFEST.MF"), getPlugin6Manifest(), charset);
    }

    /**
     * Test of find method, of class ManifestPluginDescriptorFinder.
     */
    @Test
    public void testFind() throws Exception {
        DefaultPluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new PluginClasspath());

        PluginDescriptor plugin1 = instance.find(Paths.get(testFolder.getRoot().getPath(),"test-plugin-1").toFile());

        PluginDescriptor plugin2 = instance.find(Paths.get(testFolder.getRoot().getPath(),"test-plugin-2").toFile());

        assertEquals("test-plugin-1", plugin1.getPluginId());
        assertEquals("Test Plugin 1", plugin1.getPluginDescription());
        assertEquals("ro.fortsoft.pf4j.plugin.TestPlugin", plugin1.getPluginClass());
        assertEquals(Version.valueOf("0.0.1"), plugin1.getVersion());
        assertEquals("Decebal Suiu", plugin1.getProvider());
        assertEquals(2, plugin1.getDependencies().size());
        assertEquals("test-plugin-2", plugin1.getDependencies().get(0).getPluginId());
        assertEquals("test-plugin-3", plugin1.getDependencies().get(1).getPluginId());
        assertEquals("~1.0", plugin1.getDependencies().get(1).getPluginVersionSupport());
        assertTrue(plugin1.getRequires().interpret(Version.valueOf("1.0.0")));

        assertEquals("test-plugin-2", plugin2.getPluginId());
        assertEquals("", plugin2.getPluginDescription());
        assertEquals("ro.fortsoft.pf4j.plugin.TestPlugin", plugin2.getPluginClass());
        assertEquals(Version.valueOf("0.0.1"), plugin2.getVersion());
        assertEquals("Decebal Suiu", plugin2.getProvider());
        assertEquals(0, plugin2.getDependencies().size());
        assertTrue(plugin2.getRequires().interpret(Version.valueOf("1.0.0")));
    }

    /**
     * Test of find method, of class ManifestPluginDescriptorFinder.
     */
    @Test(expected = PluginException.class)
    public void testFindNotFound() throws Exception {

        ManifestPluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new PluginClasspath());
        PluginDescriptor result = instance.find(Paths.get(testFolder.getRoot().getPath(),"test-plugin-3").toFile());
    }

    /**
     * Test of find method, of class ManifestPluginDescriptorFinder.
     */
    @Test(expected = PluginException.class)
    public void testFindMissingPluginClass() throws Exception {

        ManifestPluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new PluginClasspath());
        PluginDescriptor result = instance.find(Paths.get(testFolder.getRoot().getPath(),"test-plugin-4").toFile());
    }

    /**
     * Test of find method, of class ManifestPluginDescriptorFinder.
     */
    @Test(expected = PluginException.class)
    public void testFindMissingPluginVersion() throws Exception {

        ManifestPluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new PluginClasspath());
        PluginDescriptor result = instance.find(Paths.get(testFolder.getRoot().getPath(),"test-plugin-5").toFile());
    }

    /**
     * Test of find method, of class ManifestPluginDescriptorFinder.
     */
    @Test(expected = PluginException.class)
    public void testFindMissingPluginId() throws Exception {

        ManifestPluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new PluginClasspath());
        PluginDescriptor result = instance.find(Paths.get(testFolder.getRoot().getPath(),"test-plugin-6").toFile());
    }

    private List<String> getPlugin1Manifest() {

        String[] lines = new String[]{"Manifest-Version: 1.0\n"
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
            + "Created-By: Apache Maven 3.0.5\n"
            + "Build-Jdk: 1.8.0_45\n"
            + "Specification-Version: 0.10.0-SNAPSHOT\n"
            + "\n"
            + ""};

        return Arrays.asList(lines);
    }

    private List<String> getPlugin2Manifest() {

        String[] lines = new String[]{"Manifest-Version: 1.0\n"
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
            + ""};

        return Arrays.asList(lines);
    }

    private List<String> getPlugin4Manifest() {

        String[] lines = new String[]{"Manifest-Version: 1.0\n"
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
            + ""};

        return Arrays.asList(lines);
    }

    private List<String> getPlugin5Manifest() {

        String[] lines = new String[]{"Manifest-Version: 1.0\n"
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
            + ""};

        return Arrays.asList(lines);
    }

    private List<String> getPlugin6Manifest() {

        String[] lines = new String[]{"Manifest-Version: 1.0\n"
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
            + ""};

        return Arrays.asList(lines);
    }
}
