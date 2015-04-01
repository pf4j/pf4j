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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Retrieves all plugins with this state.
     */
    public List<PluginWrapper> getPlugins(PluginState pluginState);

    /**
     * Gets all plugin names paired with the corresponding plugin
     * @return all plugin names
     */
    public Map<String, PluginWrapper> getPluginMap();

    /**
     * Gets each instance of {@link IzouPlugin} for every addOn registered with izou.
     *
     * @return each instance of {@link IzouPlugin} for every addOn registered with izou
     */
    public Map<String, IzouPlugin> getIzouPluginMap();

    /**
     * Gets all sdks paired with their class loaders
     */
    public Map<String, IzouPluginClassLoader> getSdkClassLoaders();

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
     * Retrieves the plugin with this id.
     *
     * @param pluginId the plugin id
     * @return the plugin
     */
    public PluginWrapper getPlugin(String pluginId);

    /**
     * Load plugins.
     */
    public void loadPlugins();

    /**
     * Load a plugin.
     *
     * @param pluginArchiveFile
     * @return the pluginId of the installed plugin or null
     */
	public String loadPlugin(File pluginArchiveFile);

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
     * Disables a plugin from being loaded.
     *
     * @param pluginId
     * @return true if plugin is disabled
     */
    public boolean disablePlugin(String pluginId);

    /**
     * Enables a plugin that has previously been disabled.
     *
     * @param pluginId
     * @return true if plugin is enabled
     */
    public boolean enablePlugin(String pluginId);

    /**
     * Deletes a plugin.
     *
     * @param pluginId
     * @return true if the plugin was deleted
     */
    public boolean deletePlugin(String pluginId);

	public IzouPluginClassLoader getPluginClassLoader(String pluginId);

	public <T> List<T> getExtensions(Class<T> type);

    public Set<String> getExtensionClassNames(String pluginId);

    /**
	 * The runtime mode. Must currently be either DEVELOPMENT or DEPLOYMENT.
	 */
	public RuntimeMode getRuntimeMode();

    public void addPluginStateListener(PluginStateListener listener);

    public void removePluginStateListener(PluginStateListener listener);

    /**
     * Set the system version.  This is used to compare against the plugin
     * requires attribute.  The default system version is 0.0.0 which
     * disables all version checking.
     *
     * @param version
     */
    public void setSystemVersion(Version version);

    /**
     * Returns the system version.
     *
     * * @return the system version
     */
    public Version getSystemVersion();
}
