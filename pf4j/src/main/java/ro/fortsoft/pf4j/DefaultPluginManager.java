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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.util.CompoundClassLoader;
import ro.fortsoft.pf4j.util.DirectoryFilter;
import ro.fortsoft.pf4j.util.FileUtils;
import ro.fortsoft.pf4j.util.Unzip;
import ro.fortsoft.pf4j.util.ZipFilter;

/**
 * Default implementation of the PluginManager interface.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginManager implements PluginManager {

	private static final Logger log = LoggerFactory.getLogger(DefaultPluginManager.class);

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
     * A list with started plugins.
     */
    private List<PluginWrapper> startedPlugins;
    
    private List<String> enabledPlugins;
    private List<String> disabledPlugins;
    
    /**
     * A compound class loader of resolved plugins. 
     */
    protected CompoundClassLoader compoundClassLoader;
    
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
        startedPlugins = new ArrayList<PluginWrapper>();
        disabledPlugins = new ArrayList<String>();
        compoundClassLoader = new CompoundClassLoader();
        
        pluginDescriptorFinder = createPluginDescriptorFinder();
        extensionFinder = createExtensionFinder();

        try {
        	// create a list with plugin identifiers that should be only accepted by this manager (whitelist from plugins/enabled.txt file)
        	enabledPlugins = FileUtils.readLines(new File(pluginsDirectory, "enabled.txt"), true);
        	log.info("Enabled plugins: {}", enabledPlugins);
        	
        	// create a list with plugin identifiers that should not be accepted by this manager (blacklist from plugins/disabled.txt file)
        	disabledPlugins = FileUtils.readLines(new File(pluginsDirectory, "disabled.txt"), true);
        	log.info("Disabled plugins: {}", disabledPlugins);
        } catch (IOException e) {
        	log.error(e.getMessage(), e);
        }

        System.setProperty("pf4j.pluginsDir", pluginsDirectory.getAbsolutePath());
    }

	@Override
    public List<PluginWrapper> getPlugins() {
        return new ArrayList<PluginWrapper>(plugins.values());
    }

    @Override
	public List<PluginWrapper> getResolvedPlugins() {
		return resolvedPlugins;
	}

	public PluginWrapper getPlugin(String pluginId) {
		return plugins.get(pluginId);
	}

	@Override
    public List<PluginWrapper> getUnresolvedPlugins() {
		return unresolvedPlugins;
	}

	@Override
	public List<PluginWrapper> getStartedPlugins() {
		return startedPlugins;
	}
	
    /**
     * Start all active plugins.
     */
	@Override
    public void startPlugins() {
        for (PluginWrapper pluginWrapper : resolvedPlugins) {
            try {
            	log.info("Start plugin '{}'", pluginWrapper.getDescriptor().getPluginId());
				pluginWrapper.getPlugin().start();
				pluginWrapper.setPluginState(PluginState.STARTED);
				startedPlugins.add(pluginWrapper);
			} catch (PluginException e) {
				log.error(e.getMessage(), e);
			}
        }
    }

    /**
     * Stop all active plugins.
     */
    @Override
    public void stopPlugins() {
    	// stop started plugins in reverse order
    	Collections.reverse(startedPlugins);
        for (PluginWrapper pluginWrapper : startedPlugins) {
            try {
            	log.info("Stop plugin '{}'", pluginWrapper.getDescriptor().getPluginId());
            	pluginWrapper.getPlugin().stop();
            	pluginWrapper.setPluginState(PluginState.STOPPED);
			} catch (PluginException e) {
				log.error(e.getMessage(), e);
			}
        }
    }

    /**
     * Load plugins.
     */
    @Override
    public void loadPlugins() {
    	// check for plugins directory
        if (!pluginsDirectory.exists() || !pluginsDirectory.isDirectory()) {
            log.error("No '{}' directory", pluginsDirectory);
            return;
        }

        // expand all plugin archives
        FilenameFilter zipFilter = new ZipFilter();
        String[] zipFiles = pluginsDirectory.list(zipFilter);
        for (String zipFile : zipFiles) {
        	try {
				expandPluginArchive(zipFile);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
        }

        // check for no plugins
        FilenameFilter directoryFilter = new DirectoryFilter();
        String[] directories = pluginsDirectory.list(directoryFilter);
        if (directories.length == 0) {
        	log.info("No plugins");
        	return;
        }

        // load any plugin from plugins directory
        for (String directory : directories) {
            try {
                loadPlugin(directory);
            } catch (PluginException e) {
				log.error(e.getMessage(), e);
            }
        }

        // resolve 'unresolvedPlugins'
        try {
			resolvePlugins();
		} catch (PluginException e) {
			log.error(e.getMessage(), e);
		}
    }

    /**
     * Get plugin class loader for this path.
     */
    @Override
    public PluginClassLoader getPluginClassLoader(String pluginId) {
    	return pluginClassLoaders.get(pluginId);
    }

    @Override
	public <T> List<T> getExtensions(Class<T> type) {
		List<ExtensionWrapper<T>> extensionsWrapper = extensionFinder.find(type);
		List<T> extensions = new ArrayList<T>(extensionsWrapper.size());
		for (ExtensionWrapper<T> extensionWrapper : extensionsWrapper) {
			extensions.add(extensionWrapper.getInstance());
		}
		
		return extensions;
	}
	
    /**
     * Retrieves the {@link PluginWrapper} that loaded the given class 'clazz'.
     */
    public PluginWrapper whichPlugin(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        for (PluginWrapper plugin : resolvedPlugins) {
            if (plugin.getPluginClassLoader() == classLoader) {
            	return plugin;
            }
        }
        
        return null;
    }

	private void loadPlugin(String fileName) throws PluginException {
        // test for plugin directory
        File pluginDirectory = new File(pluginsDirectory, fileName);
        if (!pluginDirectory.isDirectory()) {
            return;
        }
        
        // try to load the plugin
        String pluginPath = "/".concat(fileName);

        // test for plugin duplication
        if (plugins.get(pathToIdMap.get(pluginPath)) != null) {
            return;
        }

        // retrieves the plugin descriptor
        log.debug("Find plugin descriptor '{}'", pluginPath);
        PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginDirectory);
        log.debug("Descriptor " + pluginDescriptor);
        String pluginClassName = pluginDescriptor.getPluginClass();
        log.debug("Class '{}' for plugin '{}'",  pluginClassName, pluginPath);

        // test for disabled plugin
        if (isPluginDisabled(pluginDescriptor.getPluginId())) {
        	log.info("Plugin '{}' is disabled", pluginPath);
            return;
        }

        // load plugin
        log.debug("Loading plugin '{}'", pluginPath);
        PluginLoader pluginLoader = new PluginLoader(this, pluginDescriptor, pluginDirectory);
        pluginLoader.load();
        log.debug("Loaded plugin '{}'", pluginPath);
        
        // create the plugin wrapper
        log.debug("Creating wrapper for plugin '{}'", pluginPath);
        PluginWrapper pluginWrapper = new PluginWrapper(pluginDescriptor, pluginPath, pluginLoader.getPluginClassLoader());
        log.debug("Created wrapper '{}' for plugin '{}'", pluginWrapper, pluginPath);

        String pluginId = pluginDescriptor.getPluginId();

        // add plugin to the list with plugins
        plugins.put(pluginId, pluginWrapper);
        unresolvedPlugins.add(pluginWrapper);

        // add plugin class loader to the list with class loaders
        PluginClassLoader pluginClassLoader = pluginLoader.getPluginClassLoader();
        pluginClassLoaders.put(pluginId, pluginClassLoader);
    }

	/**
	 * Add the possibility to override the PluginDescriptorFinder. 
	 */
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
    	return new DefaultPluginDescriptorFinder();
    }

    /**
     * Add the possibility to override the ExtensionFinder. 
     */
    protected ExtensionFinder createExtensionFinder() {
    	return new DefaultExtensionFinder(compoundClassLoader);
    }

    protected boolean isPluginDisabled(String pluginId) {
    	if (enabledPlugins.isEmpty()) {
    		return disabledPlugins.contains(pluginId);
    	}
    	
    	return !enabledPlugins.contains(pluginId);
    }
    
    private void expandPluginArchive(String fileName) throws IOException {
        File pluginArchiveFile = new File(pluginsDirectory, fileName);
        long pluginArchiveDate = pluginArchiveFile.lastModified();
        String pluginName = fileName.substring(0, fileName.length() - 4);
        File pluginDirectory = new File(pluginsDirectory, pluginName);
        // check if exists directory or the '.zip' file is "newer" than directory
        if (!pluginDirectory.exists() || (pluginArchiveDate > pluginDirectory.lastModified())) {
        	log.debug("Expand plugin archive '{}' in '{}'", pluginArchiveFile, pluginDirectory);
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
        	compoundClassLoader.addLoader(pluginWrapper.getPluginClassLoader());
        	log.info("Plugin '{}' resolved", pluginWrapper.getDescriptor().getPluginId());
        }
	}
	
}
