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
import java.io.File;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Mario Franco
 */
public class ManifestPluginDescriptorFinderTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of find method, of class ManifestPluginDescriptorFinder.
     */
    @Test
    public void testFind() throws Exception {
        DefaultPluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new PluginClasspath());
        URL url = getClass().getResource("/test-plugin-1");
        PluginDescriptor plugin1 = instance.find(new File(url.getPath()));
        url = getClass().getResource("/test-plugin-2");
        PluginDescriptor plugin2 = instance.find(new File(url.getPath()));

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
        URL url = getClass().getResource("/test-plugin-3");
        PluginDescriptor result = instance.find(new File(url.getPath()));
    }

    /**
     * Test of find method, of class ManifestPluginDescriptorFinder.
     */
    @Test(expected = PluginException.class)
    public void testFindMissingPluginClass() throws Exception {

        ManifestPluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new PluginClasspath());
        URL url = getClass().getResource("/test-plugin-4");
        PluginDescriptor result = instance.find(new File(url.getPath()));
    }

    /**
     * Test of find method, of class ManifestPluginDescriptorFinder.
     */
    @Test(expected = PluginException.class)
    public void testFindMissingPluginVersion() throws Exception {

        ManifestPluginDescriptorFinder instance = new DefaultPluginDescriptorFinder(new PluginClasspath());
        URL url = getClass().getResource("/test-plugin-5");
        PluginDescriptor result = instance.find(new File(url.getPath()));
    }

}
