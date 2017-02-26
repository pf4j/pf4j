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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ro.fortsoft.pf4j.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
public class DefaultPluginStatusProviderTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Test of isPluginDisabled method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testIsPluginDisabled() throws IOException {
        createEnabledFile();
        createDisabledFile();

        PluginStatusProvider instance = new DefaultPluginStatusProvider(getPluginsRoot());

        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-2"));
        assertTrue(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of isPluginDisabled method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testIsPluginDisabledWithEnableEmpty() throws IOException {
        createDisabledFile();

        PluginStatusProvider instance = new DefaultPluginStatusProvider(getPluginsRoot());

        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of disablePlugin method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testDisablePlugin() throws IOException {
        createEnabledFile();
        createDisabledFile();

        PluginStatusProvider instance = new DefaultPluginStatusProvider(getPluginsRoot());

        assertTrue(instance.disablePlugin("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-2"));
        assertTrue(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of disablePlugin method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testDisablePluginWithEnableEmpty() throws IOException {
        createDisabledFile();

        PluginStatusProvider instance = new DefaultPluginStatusProvider(getPluginsRoot());

        assertTrue(instance.disablePlugin("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of enablePlugin method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testEnablePlugin() throws IOException {
        createEnabledFile();

        PluginStatusProvider instance = new DefaultPluginStatusProvider(getPluginsRoot());

        assertTrue(instance.enablePlugin("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertFalse(instance.isPluginDisabled("plugin-2"));
        assertTrue(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of enablePlugin method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testEnablePluginWithEnableEmpty() {
        PluginStatusProvider instance = new DefaultPluginStatusProvider(getPluginsRoot());

        assertTrue(instance.enablePlugin("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertFalse(instance.isPluginDisabled("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of disablePlugin method without a disabled.txt file.
     */
    @Test
    public void testDisablePluginWithoutDisabledFile() throws IOException {
        PluginStatusProvider instance = new DefaultPluginStatusProvider(getPluginsRoot());

        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.disablePlugin("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-1"));
    }

    private void createDisabledFile() throws IOException {
        List<String> plugins = new ArrayList<>();
        plugins.add("plugin-2");

        writeLines(plugins, "disabled.txt");
    }

    private void createEnabledFile() throws IOException {
        List<String> plugins = new ArrayList<>();
        plugins.add("plugin-1");
        plugins.add("plugin-2");

        writeLines(plugins, "enabled.txt");
    }

    private void writeLines(List<String> lines, String fileName) throws IOException {
        File file = testFolder.newFile(fileName);
        FileUtils.writeLines(lines, file);
    }

    private Path getPluginsRoot() {
        return testFolder.getRoot().toPath();
    }

}
