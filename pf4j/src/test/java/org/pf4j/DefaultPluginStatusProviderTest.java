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
import org.pf4j.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
public class DefaultPluginStatusProviderTest {

    @TempDir
    Path pluginsPath;

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
    public void testDisablePlugin() throws Exception {
        createEnabledFile();
        createDisabledFile();

        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);
        statusProvider.disablePlugin("plugin-1");

        assertTrue(statusProvider.isPluginDisabled("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-2"));
        assertTrue(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testDisablePluginWithEnableEmpty() throws Exception {
        createDisabledFile();

        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);
        statusProvider.disablePlugin("plugin-1");

        assertTrue(statusProvider.isPluginDisabled("plugin-1"));
        assertTrue(statusProvider.isPluginDisabled("plugin-2"));
        assertFalse(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testEnablePlugin() throws Exception {
        createEnabledFile();

        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);
        statusProvider.enablePlugin("plugin-2");

        assertFalse(statusProvider.isPluginDisabled("plugin-1"));
        assertFalse(statusProvider.isPluginDisabled("plugin-2"));
        assertTrue(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testEnablePluginWithEnableEmpty() throws Exception{
        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);
        statusProvider.enablePlugin("plugin-2");

        assertFalse(statusProvider.isPluginDisabled("plugin-1"));
        assertFalse(statusProvider.isPluginDisabled("plugin-2"));
        assertFalse(statusProvider.isPluginDisabled("plugin-3"));
    }

    @Test
    public void testDisablePluginWithoutDisabledFile() throws Exception {
        PluginStatusProvider statusProvider = new DefaultPluginStatusProvider(pluginsPath);

        assertFalse(statusProvider.isPluginDisabled("plugin-1"));

        statusProvider.disablePlugin("plugin-1");
        assertTrue(statusProvider.isPluginDisabled("plugin-1"));
    }

    private void createDisabledFile() throws IOException {
        List<String> disabledPlugins = new ArrayList<>();
        disabledPlugins.add("plugin-2");

        Path disabledPath = pluginsPath.resolve("disabled.txt");
        FileUtils.writeLines(disabledPlugins, disabledPath.toFile());
    }

    private void createEnabledFile() throws IOException {
        List<String> enabledPlugins = new ArrayList<>();
        enabledPlugins.add("plugin-1");
        enabledPlugins.add("plugin-2");

        Path enabledPath = pluginsPath.resolve("enabled.txt");
        FileUtils.writeLines(enabledPlugins, enabledPath.toFile());
    }

}
