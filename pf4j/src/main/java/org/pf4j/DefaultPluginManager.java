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
package org.pf4j;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pf4j.util.DirectoryFilter;
import org.pf4j.util.UberClassLoader;
import org.pf4j.util.Unzip;
import org.pf4j.util.ZipFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of the PluginManager interface.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginManager implements PluginManager {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultPluginManager.class);

    /**
     * The plugins repository.
     */
    private File pluginsDirectory;

    private ExtensionFinder extensionFinder;
    
    private PluginDescriptorFinder pluginDescriptorFinder;
    
    /**
     * A map of plugins this manager is responsible for (the key is the 'pluginId').
     */
    private Map<String, Plugin> plugins;

    /**
     * A map of plugin class loaders (he key is the 'pluginId').
     */
    private Map<String, PluginClassLoader> pluginClassLoaders;

    /**
     * A relation between 'pluginPath' and 'pluginId'
     */
    private Map<String, String> pathToIdMap;

    /**
     * A list with unresolved plugins (unresolved dependency).
     */
    private List<Plugin> unresolvedPlugins;

    /**
     * A list with resolved plugins (resolved dependency).
     */
    private List<Plugin> resolvedPlugins;

    /**
     * A list with disabled plugins.
     */
    private List<Plugin> disabledPlugins;
    
    private UberClassLoader uberClassLoader;

    /**
     * Th plugins directory is supplied by System.getProperty("pf4j.pluginsDir", "plugins").
     */
    public DefaultPluginManager() {
    	this(new File(System.getProperty("pf4j.pluginsDir", "plugins")));
    }
    
    /**
     * Constructs DefaultPluginManager which the given plugins directory.
     *
     * @param pluginsDirectory
     *            the directory to search for plugins
     */
    public DefaultPluginManager(File pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;
        plugins = new HashMap<String, Plugin>();
        pluginClassLoaders = new HashMap<String, PluginClassLoader>();
        pathToIdMap = new HashMap<String, String>();
        unresolvedPlugins = new ArrayList<Plugin>();
        resolvedPlugins = new ArrayList<Plugin>();
        disabledPlugins = new ArrayList<Plugin>();
        pluginDescriptorFinder = new DefaultPluginDescriptorFinder();
        uberClassLoader = new UberClassLoader();
        extensionFinder = new DefaultExtensionFinder(uberClassLoader);
    }

    /**
     * Retrieves all active plugins.
     */
    public List<Plugin> getPlugins() {
        return new ArrayList<Plugin>(plugins.values());
    }

	public List<Plugin> getResolvedPlugins() {
		return resolvedPlugins;
	}

	public Plugin getPlugin(String pluginId) {
		return plugins.get(pluginId);
	}

    public List<Plugin> getUnresolvedPlugins() {
		return unresolvedPlugins;
	}

	public List<Plugin> getDisabledPlugins() {
		return disabledPlugins;
	}

    /**
     * Start all active plugins.
     */
    public void startPlugins() {
    	List<Plugin> resolvedPlugins = getResolvedPlugins();
        for (Plugin plugin : resolvedPlugins) {
            try {
				plugin.start();
			} catch (PluginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /**
     * Stop all active plugins.
     */
    public void stopPlugins() {
    	List<Plugin> resolvedPlugins = getResolvedPlugins();
        for (Plugin plugin : resolvedPlugins) {
            try {
				plugin.stop();
			} catch (PluginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /**
     * Load plugins.
     */
    public void loadPlugins() {
    	// check for plugins directory
        if (!pluginsDirectory.exists() || !pluginsDirectory.isDirectory()) {
            LOG.error("No '" + pluginsDirectory + "' directory");
            return;
        }

        // expand all plugin archives
        FilenameFilter zipFilter = new ZipFilter();
        String[] zipFiles = pluginsDirectory.list(zipFilter);
        for (String zipFile : zipFiles) {
        	try {
				expandPluginArchive(zipFile);
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				e.printStackTrace();
			}
        }

        // load any plugin from plugins directory
        FilenameFilter directoryFilter = new DirectoryFilter();
        String[] directories = pluginsDirectory.list(directoryFilter);
        for (String directory : directories) {
            try {
                loadPlugin(directory);
            } catch (Exception e) {
				LOG.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }

        // check for no plugins
        if (directories.length == 0) {
        	LOG.info("No plugins");
        	return;
        }

        // resolve 'unresolvedPlugins'
        resolvePlugins();
    }

    /**
     * Get plugin class loader for this path.
     */
    public PluginClassLoader getPluginClassLoader(String pluginId) {
    	return pluginClassLoaders.get(pluginId);
    }

	public <T> List<ExtensionWrapper<T>> getExtensions(Class<T> type) {
		return extensionFinder.find(type);
	}
	
	private void loadPlugin(String fileName) throws Exception {
        // test for plugin directory
        File pluginDirectory = new File(pluginsDirectory, fileName);
        if (!pluginDirectory.isDirectory()) {
            return;
        }

        // try to load the plugin
        String pluginPath = "/".concat(fileName);

        // test for disabled plugin
        if (disabledPlugins.contains(pluginPath)) {
            return;
        }

        // it's a new plugin
        if (plugins.get(pathToIdMap.get(pluginPath)) != null) {
            return;
        }

        // retrieves the plugin descriptor
        LOG.debug("Find plugin descriptor '" + pluginPath + "'");
        PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginDirectory);
        LOG.debug("Descriptor " + pluginDescriptor);
        String pluginClassName = pluginDescriptor.getPluginClass();
        LOG.debug("Class '" + pluginClassName + "'" + " for plugin '" + pluginPath + "'");

        // load plugin
        LOG.debug("Loading plugin '" + pluginPath + "'");
        PluginWrapper pluginWrapper = new PluginWrapper(pluginDescriptor);
        PluginLoader pluginLoader = new PluginLoader(this, pluginWrapper, pluginDirectory);
        pluginLoader.load();
        LOG.debug("Loaded plugin '" + pluginPath + "'");
        
        // set some variables in plugin wrapper
        pluginWrapper.setPluginPath(pluginPath);
        pluginWrapper.setPluginClassLoader(pluginLoader.getPluginClassLoader());

        // create the plugin instance
        LOG.debug("Creating instance for plugin '" + pluginPath + "'");
        Plugin plugin = getPluginInstance(pluginWrapper, pluginLoader);
        LOG.debug("Created instance '" + plugin + "' for plugin '" + pluginPath + "'");

        String pluginId = pluginDescriptor.getPluginId();

        // add plugin to the list with plugins
        plugins.put(pluginId, plugin);
        unresolvedPlugins.add(plugin);

        // add plugin class loader to the list with class loaders
        PluginClassLoader pluginClassLoader = pluginLoader.getPluginClassLoader();
        pluginDescriptor.setPluginClassLoader(pluginClassLoader);
        pluginClassLoaders.put(pluginId, pluginClassLoader);
    }

    private void expandPluginArchive(String fileName) throws IOException {
        File pluginArchiveFile = new File(pluginsDirectory, fileName);
        long pluginArchiveDate = pluginArchiveFile.lastModified();
        String pluginName = fileName.substring(0, fileName.length() - 4);
        File pluginDirectory = new File(pluginsDirectory, pluginName);
        // check if exists directory or the '.zip' file is "newer" than directory
        if (!pluginDirectory.exists() || pluginArchiveDate > pluginDirectory.lastModified()) {
        	LOG.debug("Expand plugin archive '" + pluginArchiveFile + "' in '" + pluginDirectory + "'");
            // create directorie for plugin
            pluginDirectory.mkdirs();

            // expand '.zip' file
            Unzip unzip = new Unzip();
            unzip.setSource(pluginArchiveFile);
            unzip.setDestination(pluginDirectory);
            unzip.extract();
        }
    }

    private Plugin getPluginInstance(PluginWrapper pluginWrapper, PluginLoader pluginLoader)
    		throws Exception {
    	String pluginClassName = pluginWrapper.getDescriptor().getPluginClass();

        ClassLoader pluginClassLoader = pluginLoader.getPluginClassLoader();
        Class<?> pluginClass = pluginClassLoader.loadClass(pluginClassName);

        // once we have the class, we can do some checks on it to ensure
        // that it is a valid implementation of a plugin.
        int modifiers = pluginClass.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)
                || (!Plugin.class.isAssignableFrom(pluginClass))) {
            throw new PluginException("The plugin class '" + pluginClassName
                    + "' is not compatible.");
        }

        // create the plugin instance
        Constructor<?> constructor = pluginClass.getConstructor(new Class[] { PluginWrapper.class });
        Plugin plugin = (Plugin) constructor.newInstance(new Object[] { pluginWrapper });

        return plugin;
    }

	private void resolvePlugins() {
        resolveDependencies();
	}

	private void resolveDependencies() {
		DependencyResolver dependencyResolver = new DependencyResolver(unresolvedPlugins);
        resolvedPlugins = dependencyResolver.getSortedDependencies();
        for (Plugin plugin : resolvedPlugins) {
        	unresolvedPlugins.remove(plugin);
        	uberClassLoader.addLoader(plugin.getWrapper().getPluginClassLoader());
        }
	}

}
