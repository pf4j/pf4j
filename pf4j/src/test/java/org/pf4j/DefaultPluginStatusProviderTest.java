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
import org.pf4j.util.FileUtils;

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

    private Path pluginsPath;

    @Rule
    public TemporaryFolder pluginsFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        pluginsPath = pluginsFolder.getRoot().toPath();
    }

    @Test
    public void testIsPluginDisabled() throws IOException {
        createEnabledFile();
        createDisabledFile();

        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);

        assertFalse(statusProvider.isPluginDisabled("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-2"));
        assertTrue(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testIsPluginDisabledWithEnableEmpty() throws IOException {
        createDisabledFile();

        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);

        assertFalse(statusProvider.isPluginDisabled("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-2"));
        assertFalse(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testDisablePlugin() throws IOException {
        createEnabledFile();
        createDisabledFile();

        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);

        assertTrue(statusProvider.disablePlugin("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-2"));
        assertTrue(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testDisablePluginWithEnableEmpty() throws IOException {
        createDisabledFile();

        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);

        assertTrue(statusProvider.disablePlugin("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-2"));
        assertFalse(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testEnablePlugin() throws IOException {
        createEnabledFile();

        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);

        assertTrue(statusProvider.enablePlugin("plugin-2"));
        assertFalse(statusProvider.isPluginDisabled("plugin-1"));
        assertFalse(statusProvider.isPluginDisabled("plugin-2"));
        assertTrue(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testEnablePluginWithEnableEmpty() {
        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);

        assertTrue(statusProvider.enablePlugin("plugin-2"));
        assertFalse(statusProvider.isPluginDisabled("plugin-1"));
        assertFalse(statusProvider.isPluginDisabled("plugin-2"));
        assertFalse(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testDisablePluginWithoutDisabledFile() {
        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);

        assertFalse(statusProvider.isPluginDisabled("plugin-1"));
        assertTrue(statusProvider.disablePlugin("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-1"));
    }

    private void createDisabledFile() throws IOException {
        List<String> disabledPlugins = new ArrayList<>();
        disabledPlugins.add("plugin-2");

        File disabledFile = pluginsFolder.newFile("disabled.txt");
        FileUtils.writeLines(disabledPlugins, disabledFile);
    }

    private void createEnabledFile() throws IOException {
        List<String> enabledPlugins = new ArrayList<>();
        enabledPlugins.add("plugin-1");
        enabledPlugins.add("plugin-2");

        File enabledFile = pluginsFolder.newFile("enabled.txt");
        FileUtils.writeLines(enabledPlugins, enabledFile);
    }

}
