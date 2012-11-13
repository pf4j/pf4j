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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.util.DirectoryFilter;
import ro.fortsoft.pf4j.util.UberClassLoader;
import ro.fortsoft.pf4j.util.Unzip;
import ro.fortsoft.pf4j.util.ZipFilter;

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
    private Map<String, PluginWrapper> plugins;

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
    private List<PluginWrapper> unresolvedPlugins;

    /**
     * A list with resolved plugins (resolved dependency).
     */
    private List<PluginWrapper> resolvedPlugins;

    /**
     * A list with disabled plugins.
     */
    private List<PluginWrapper> disabledPlugins;
    
    /**
     * A list with started plugins.
     */
    private List<PluginWrapper> startedPlugins;
    
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
        
        plugins = new HashMap<String, PluginWrapper>();
        pluginClassLoaders = new HashMap<String, PluginClassLoader>();
        pathToIdMap = new HashMap<String, String>();
        unresolvedPlugins = new ArrayList<PluginWrapper>();
        resolvedPlugins = new ArrayList<PluginWrapper>();
        disabledPlugins = new ArrayList<PluginWrapper>();
        startedPlugins = new ArrayList<PluginWrapper>();
        pluginDescriptorFinder = new DefaultPluginDescriptorFinder();
        uberClassLoader = new UberClassLoader();
        extensionFinder = new DefaultExtensionFinder(uberClassLoader);
        
        System.setProperty("pf4j.pluginsDir", pluginsDirectory.getAbsolutePath());
    }

    public List<PluginWrapper> getPlugins() {
        return new ArrayList<PluginWrapper>(plugins.values());
    }

	public List<PluginWrapper> getResolvedPlugins() {
		return resolvedPlugins;
	}

	public PluginWrapper getPlugin(String pluginId) {
		return plugins.get(pluginId);
	}

    public List<PluginWrapper> getUnresolvedPlugins() {
		return unresolvedPlugins;
	}

	public List<PluginWrapper> getDisabledPlugins() {
		return disabledPlugins;
	}

	public List<PluginWrapper> getStartedPlugins() {
		return startedPlugins;
	}
	
    /**
     * Start all active plugins.
     */
    public void startPlugins() {
        for (PluginWrapper pluginWrapper : resolvedPlugins) {
            try {
            	LOG.info("Start plugin '" + pluginWrapper.getDescriptor().getPluginId() + "'");
				pluginWrapper.getPlugin().start();
				startedPlugins.add(pluginWrapper);
			} catch (PluginException e) {
				LOG.error(e.getMessage(), e);
			}
        }
    }

    /**
     * Stop all active plugins.
     */
    public void stopPlugins() {
        for (PluginWrapper pluginWrapper : startedPlugins) {
            try {
            	LOG.info("Stop plugin '" + pluginWrapper.getDescriptor().getPluginId() + "'");
            	pluginWrapper.getPlugin().stop();
			} catch (PluginException e) {
				LOG.error(e.getMessage(), e);
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
			}
        }

        // load any plugin from plugins directory
        FilenameFilter directoryFilter = new DirectoryFilter();
        String[] directories = pluginsDirectory.list(directoryFilter);
        for (String directory : directories) {
            try {
                loadPlugin(directory);
            } catch (PluginException e) {
				LOG.error(e.getMessage(), e);
            }
        }

        // check for no plugins
        if (directories.length == 0) {
        	LOG.info("No plugins");
        	return;
        }

        // resolve 'unresolvedPlugins'
        try {
			resolvePlugins();
		} catch (PluginException e) {
			LOG.error(e.getMessage(), e);
		}
    }

    /**
     * Get plugin class loader for this path.
     */
    public PluginClassLoader getPluginClassLoader(String pluginId) {
    	return pluginClassLoaders.get(pluginId);
    }

	public <T> List<T> getExtensions(Class<T> type) {
		List<ExtensionWrapper<T>> extensionsWrapper = extensionFinder.find(type);
		List<T> extensions = new ArrayList<T>(extensionsWrapper.size());
		for (ExtensionWrapper<T> extensionWrapper : extensionsWrapper) {
			extensions.add(extensionWrapper.getInstance());
		}
		
		return extensions;
	}
	
	private void loadPlugin(String fileName) throws PluginException {
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
        PluginLoader pluginLoader = new PluginLoader(this, pluginDescriptor, pluginDirectory);
        pluginLoader.load();
        LOG.debug("Loaded plugin '" + pluginPath + "'");
        
        // create the plugin wrapper
        LOG.debug("Creating wrapper for plugin '" + pluginPath + "'");
        PluginWrapper pluginWrapper = new PluginWrapper(pluginDescriptor, pluginPath, pluginLoader.getPluginClassLoader());
        LOG.debug("Created wrapper '" + pluginWrapper + "' for plugin '" + pluginPath + "'");

        String pluginId = pluginDescriptor.getPluginId();

        // add plugin to the list with plugins
        plugins.put(pluginId, pluginWrapper);
        unresolvedPlugins.add(pluginWrapper);

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

	private void resolvePlugins() throws PluginException {
		resolveDependencies();
	}

	private void resolveDependencies() throws PluginException {
		DependencyResolver dependencyResolver = new DependencyResolver(unresolvedPlugins);
		resolvedPlugins = dependencyResolver.getSortedPlugins();
        for (PluginWrapper pluginWrapper : resolvedPlugins) {
        	unresolvedPlugins.remove(pluginWrapper);
        	uberClassLoader.addLoader(pluginWrapper.getPluginClassLoader());
        	LOG.info("Plugin '" + pluginWrapper.getDescriptor().getPluginId() + "' resolved");
        }
	}

}
