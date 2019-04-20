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

import org.pf4j.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The default implementation for {@link PluginStatusProvider}.
 * The enabled plugins are read from {@code enabled.txt} file and
 * the disabled plugins are read from {@code disabled.txt} file.
 *
 * @author Decebal Suiu
 * @author Mário Franco
 */
public class DefaultPluginStatusProvider implements PluginStatusProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultPluginStatusProvider.class);

    private final Path pluginsRoot;

    private List<String> enabledPlugins;
    private List<String> disabledPlugins;

    public DefaultPluginStatusProvider(Path pluginsRoot) {
        this.pluginsRoot = pluginsRoot;

        try {
            // create a list with plugin identifiers that should be only accepted by this manager (whitelist from plugins/enabled.txt file)
            enabledPlugins = FileUtils.readLines(pluginsRoot.resolve("enabled.txt"), true);
            log.info("Enabled plugins: {}", enabledPlugins);

            // create a list with plugin identifiers that should not be accepted by this manager (blacklist from plugins/disabled.txt file)
            disabledPlugins = FileUtils.readLines(pluginsRoot.resolve("disabled.txt"), true);
            log.info("Disabled plugins: {}", disabledPlugins);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isPluginDisabled(String pluginId) {
        if (disabledPlugins.contains(pluginId)) {
            return true;
        }

        return !enabledPlugins.isEmpty() && !enabledPlugins.contains(pluginId);
    }

    @Override
    public void disablePlugin(String pluginId) throws PluginException {
        disabledPlugins.add(pluginId);
        try {
            FileUtils.writeLines(disabledPlugins, pluginsRoot.resolve("disabled.txt").toFile());
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    @Override
    public void enablePlugin(String pluginId) throws PluginException {
        disabledPlugins.remove(pluginId);
        try {
            FileUtils.writeLines(disabledPlugins, pluginsRoot.resolve("disabled.txt").toFile());
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

}
