/*
 * Copyright 2012 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pf4j;

import java.util.List;

/**
 * Provides the functionality for plugin management such as load,
 * start and stop the plugins.
 *
 * @author Decebal Suiu
 */
public interface PluginManager {

    /**
     * Retrieves all plugins.
     */
    public List<PluginWrapper> getPlugins();

    /**
     * Retrieves all resolved plugins (with resolved dependency).
     */
	public List<PluginWrapper> getResolvedPlugins();

	/**
	 * Retrieves all unresolved plugins (with unresolved dependency).
	 */
	public List<PluginWrapper> getUnresolvedPlugins();

    /**
     * Retrieves all started plugins.
     */
    public List<PluginWrapper> getStartedPlugins();

    /**
     * Load plugins.
     */
    public void loadPlugins();

    /**
     * Start all active plugins.
     */
    public void startPlugins();

    /**
     * Start the specified plugin and it's dependencies.
     *
     * @return the plugin state
     */
    public PluginState startPlugin(String pluginId);

    /**
     * Stop all active plugins.
     */
    public void stopPlugins();

    /**
     * Stop the specified plugin and it's dependencies.
     *
     * @return the plugin state
     */
    public PluginState stopPlugin(String pluginId);

    /**
     * Unload a plugin.
     *
     * @param pluginId
     * @return true if the plugin was unloaded
     */
    public boolean unloadPlugin(String pluginId);

    /**
     * Deletes a plugin.
     *
     * @param pluginId
     * @return true if the plugin was deleted
     */
    public boolean deletePlugin(String pluginId);

	public PluginClassLoader getPluginClassLoader(String pluginId);

	public <T> List<T> getExtensions(Class<T> type);

	/**
	 * The runtime mode. Must currently be either DEVELOPMENT or DEPLOYMENT.
	 */
	public RuntimeMode getRuntimeMode();

}
