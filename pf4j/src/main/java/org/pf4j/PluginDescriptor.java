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

import java.util.List;

/**
 * A plugin descriptor contains information about a plug-in.
 *
 * @author Decebal Suiu
 */
public interface PluginDescriptor {

    /**
     * The unique identifier of the plugin.
     *
     * @return the plugin id
     */
    String getPluginId();

    /**
     * Returns a description of the plugin.
     *
     * @return the plugin description
     */
    String getPluginDescription();

    /**
     * Returns the fully qualified class name of the plugin class.
     * The plugin class must implement the {@link Plugin} interface.
     *
     * @return the plugin class
     */
    String getPluginClass();

    /**
     * Returns the plugin version.
     * The version must be unique for each release of the plugin.
     * The version is used to check if the plugin is compatible with the application.
     *
     * @see VersionManager
     * @return the plugin version
     */
    String getVersion();

    /**
     * Returns the required version of the application.
     *
     * @return the required version of the application
     */
    String getRequires();

    /**
     * Returns the author of the plugin.
     *
     * @return the author of the plugin
     */
    String getProvider();

    /**
     * Returns the license of the plugin.
     *
     * @return the license of the plugin
     */
    String getLicense();

    /**
     * Returns the dependencies of the plugin.
     * A dependency is represented by a {@link PluginDependency} object.
     *
     * @return the dependencies of the plugin
     */
    List<PluginDependency> getDependencies();

}
