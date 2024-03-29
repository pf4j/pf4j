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

/**
 * Provides a way to check if a plugin is disabled and to disable/enable a plugin.
 * <p>
 * This is useful when you want to store the plugin status in a database or in a file.
 *
 * @see PluginState#DISABLED
 *
 * @author Decebal Suiu
 * @author Mário Franco
 */
public interface PluginStatusProvider {

    /**
     * Checks if the plugin is disabled or not
     *
     * @param pluginId the unique plugin identifier, specified in its metadata
     * @return if the plugin is disabled or not
     */
    boolean isPluginDisabled(String pluginId);

    /**
     * Disables a plugin from being loaded.
     *
     * @param pluginId the unique plugin identifier, specified in its metadata
     * @throws PluginRuntimeException if something goes wrong
     */
    void disablePlugin(String pluginId);

    /**
     * Enables a plugin that has previously been disabled.
     *
     * @param pluginId the unique plugin identifier, specified in its metadata
     * @throws PluginRuntimeException if something goes wrong
     */
    void enablePlugin(String pluginId);

}
