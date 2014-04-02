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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.util.AndFileFilter;
import ro.fortsoft.pf4j.util.CompoundClassLoader;
import ro.fortsoft.pf4j.util.DirectoryFileFilter;
import ro.fortsoft.pf4j.util.FileUtils;
import ro.fortsoft.pf4j.util.HiddenFilter;
import ro.fortsoft.pf4j.util.NotFileFilter;
import ro.fortsoft.pf4j.util.Unzip;
import ro.fortsoft.pf4j.util.ZipFileFilter;

/**
 * Default implementation of the PluginManager interface.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginManager implements PluginManager {

	private static final Logger log = LoggerFactory.getLogger(DefaultPluginManager.class);

	public static final String DEFAULT_PLUGINS_DIRECTORY = "plugins";
	public static final String DEVELOPMENT_PLUGINS_DIRECTORY = "../plugins";

    /**
     * The plugins repository.
     */
    private File pluginsDirectory;

    private ExtensionFinder extensionFinder;

    private PluginDescriptorFinder pluginDescriptorFinder;

    private PluginClasspath pluginClasspath;

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
     * Cache value for the runtime mode. No need to re-read it because it wont change at
	 * runtime.
     */
    private RuntimeMode runtimeMode;

    /**
     * The plugins directory is supplied by System.getProperty("pf4j.pluginsDir", "plugins").
     */
    public DefaultPluginManager() {
    	this.pluginsDirectory = createPluginsDirectory();

    	initialize();
    }

    /**
     * Constructs DefaultPluginManager which the given plugins directory.
     *
     * @param pluginsDirectory
     *            the directory to search for plugins
     */
    public DefaultPluginManager(File pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;

        initialize();
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
     * Start the specified plugin and it's dependencies.
     */
    @Override
    public PluginState startPlugin(String pluginId) {
    	if (!plugins.containsKey(pluginId)) {
    		throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
    	}
    	PluginWrapper pluginWrapper = plugins.get(pluginId);
    	PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
    	if (pluginWrapper.getPluginState().equals(PluginState.STARTED)) {
    		log.debug("Already started plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
    		return PluginState.STARTED;
    	}
    	for (PluginDependency dependency : pluginDescriptor.getDependencies()) {
    		startPlugin(dependency.getPluginId());
    	}
    	try {
    		log.info("Start plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
    		pluginWrapper.getPlugin().start();
    		pluginWrapper.setPluginState(PluginState.STARTED);
    		startedPlugins.add(pluginWrapper);
    	} catch (PluginException e) {
    		log.error(e.getMessage(), e);
    	}
    	return pluginWrapper.getPluginState();
    }

    /**
     * Stop all active plugins.
     */
    @Override
    public void stopPlugins() {
    	// stop started plugins in reverse order
    	Collections.reverse(startedPlugins);
    	Iterator<PluginWrapper> itr = startedPlugins.iterator();
        while (itr.hasNext()) {
        	PluginWrapper pluginWrapper = itr.next();
        	PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
            try {
            	log.info("Stop plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
            	pluginWrapper.getPlugin().stop();
            	pluginWrapper.setPluginState(PluginState.STOPPED);
            	itr.remove();
			} catch (PluginException e) {
				log.error(e.getMessage(), e);
			}
        }
    }

    /**
     * Stop the specified plugin and it's dependencies.
     */
    @Override
    public PluginState stopPlugin(String pluginId) {
    	if (!plugins.containsKey(pluginId)) {
    		throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
    	}
    	PluginWrapper pluginWrapper = plugins.get(pluginId);
    	PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
    	if (pluginWrapper.getPluginState().equals(PluginState.STOPPED)) {
    		log.debug("Already stopped plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
    		return PluginState.STOPPED;
    	}
    	for (PluginDependency dependency : pluginDescriptor.getDependencies()) {
    		stopPlugin(dependency.getPluginId());
    	}
    	try {
    		log.info("Stop plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
    		pluginWrapper.getPlugin().stop();
    		pluginWrapper.setPluginState(PluginState.STOPPED);
    		startedPlugins.remove(pluginWrapper);
    	} catch (PluginException e) {
    		log.error(e.getMessage(), e);
    	}
    	return pluginWrapper.getPluginState();
    }

    /**
     * Load plugins.
     */
    @Override
    public void loadPlugins() {
    	log.debug("Lookup plugins in '{}'", pluginsDirectory.getAbsolutePath());
    	// check for plugins directory
        if (!pluginsDirectory.exists() || !pluginsDirectory.isDirectory()) {
            log.error("No '{}' directory", pluginsDirectory.getAbsolutePath());
            return;
        }

        // expand all plugin archives
        FileFilter zipFilter = new ZipFileFilter();
        File[] zipFiles = pluginsDirectory.listFiles(zipFilter);
        for (File zipFile : zipFiles) {
        	try {
				expandPluginArchive(zipFile);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
        }

        // check for no plugins
        List<FileFilter> filterList = new ArrayList<FileFilter>();
        filterList.add(new DirectoryFileFilter());
        filterList.add(new NotFileFilter(createHiddenPluginFilter()));
        FileFilter pluginsFilter = new AndFileFilter(filterList);
        File[] directories = pluginsDirectory.listFiles(pluginsFilter);
        log.debug("Found possible {} plugins: {}", directories.length, Arrays.asList(directories));
        if (directories.length == 0) {
        	log.info("No plugins");
        	return;
        }

        // load any plugin from plugins directory
        for (File directory : directories) {
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

    @Override
	public RuntimeMode getRuntimeMode() {
    	if (runtimeMode == null) {
        	// retrieves the runtime mode from system
        	String modeAsString = System.getProperty("pf4j.mode", RuntimeMode.DEPLOYMENT.toString());
        	runtimeMode = RuntimeMode.byName(modeAsString);

        	log.info("PF4J runtime mode is '{}'", runtimeMode);

    	}

		return runtimeMode;
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

	/**
	 * Add the possibility to override the PluginDescriptorFinder.
	 * By default if getRuntimeMode() returns RuntimeMode.DEVELOPMENT than a
	 * PropertiesPluginDescriptorFinder is returned else this method returns
	 * DefaultPluginDescriptorFinder.
	 */
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
    	if (RuntimeMode.DEVELOPMENT.equals(getRuntimeMode())) {
    		return new PropertiesPluginDescriptorFinder();
    	}

    	return new DefaultPluginDescriptorFinder(pluginClasspath);
    }

    /**
     * Add the possibility to override the ExtensionFinder.
     */
    protected ExtensionFinder createExtensionFinder() {
    	return new DefaultExtensionFinder(compoundClassLoader);
    }

    /**
     * Add the possibility to override the PluginClassPath.
     * By default if getRuntimeMode() returns RuntimeMode.DEVELOPMENT than a
	 * DevelopmentPluginClasspath is returned else this method returns
	 * PluginClasspath.
     */
    protected PluginClasspath createPluginClasspath() {
    	if (RuntimeMode.DEVELOPMENT.equals(getRuntimeMode())) {
    		return new DevelopmentPluginClasspath();
    	}

    	return new PluginClasspath();
    }

    protected boolean isPluginDisabled(String pluginId) {
    	if (enabledPlugins.isEmpty()) {
    		return disabledPlugins.contains(pluginId);
    	}

    	return !enabledPlugins.contains(pluginId);
    }

    protected FileFilter createHiddenPluginFilter() {
    	return new HiddenFilter();
    }

    /**
     * Add the possibility to override the plugins directory.
     * If a "pf4j.pluginsDir" system property is defined than this method returns
     * that directory.
     * If getRuntimeMode() returns RuntimeMode.DEVELOPMENT than a
	 * DEVELOPMENT_PLUGINS_DIRECTORY ("../plugins") is returned else this method returns
	 * DEFAULT_PLUGINS_DIRECTORY ("plugins").
     * @return
     */
    protected File createPluginsDirectory() {
    	String pluginsDir = System.getProperty("pf4j.pluginsDir");
    	if (pluginsDir == null) {
    		if (RuntimeMode.DEVELOPMENT.equals(getRuntimeMode())) {
    			pluginsDir = DEVELOPMENT_PLUGINS_DIRECTORY;
    		} else {
    			pluginsDir = DEFAULT_PLUGINS_DIRECTORY;
    		}
    	}

    	return new File(pluginsDir);
    }

	private void initialize() {
		plugins = new HashMap<String, PluginWrapper>();
        pluginClassLoaders = new HashMap<String, PluginClassLoader>();
        pathToIdMap = new HashMap<String, String>();
        unresolvedPlugins = new ArrayList<PluginWrapper>();
        resolvedPlugins = new ArrayList<PluginWrapper>();
        startedPlugins = new ArrayList<PluginWrapper>();
        disabledPlugins = new ArrayList<String>();
        compoundClassLoader = new CompoundClassLoader();

        pluginClasspath = createPluginClasspath();
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

	private void loadPlugin(File pluginDirectory) throws PluginException {
        // try to load the plugin
		String pluginName = pluginDirectory.getName();
        String pluginPath = "/".concat(pluginName);

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
        PluginLoader pluginLoader = new PluginLoader(this, pluginDescriptor, pluginDirectory, pluginClasspath);
        pluginLoader.load();
        log.debug("Loaded plugin '{}'", pluginPath);

        // create the plugin wrapper
        log.debug("Creating wrapper for plugin '{}'", pluginPath);
        PluginWrapper pluginWrapper = new PluginWrapper(pluginDescriptor, pluginPath, pluginLoader.getPluginClassLoader());
        pluginWrapper.setRuntimeMode(getRuntimeMode());
        log.debug("Created wrapper '{}' for plugin '{}'", pluginWrapper, pluginPath);

        String pluginId = pluginDescriptor.getPluginId();

        // add plugin to the list with plugins
        plugins.put(pluginId, pluginWrapper);
        unresolvedPlugins.add(pluginWrapper);

        // add plugin class loader to the list with class loaders
        PluginClassLoader pluginClassLoader = pluginLoader.getPluginClassLoader();
        pluginClassLoaders.put(pluginId, pluginClassLoader);
    }

    private void expandPluginArchive(File pluginArchiveFile) throws IOException {
    	String fileName = pluginArchiveFile.getName();
        long pluginArchiveDate = pluginArchiveFile.lastModified();
        String pluginName = fileName.substring(0, fileName.length() - 4);
        File pluginDirectory = new File(pluginsDirectory, pluginName);
        // check if exists directory or the '.zip' file is "newer" than directory
        if (!pluginDirectory.exists() || (pluginArchiveDate > pluginDirectory.lastModified())) {
        	log.debug("Expand plugin archive '{}' in '{}'", pluginArchiveFile, pluginDirectory);
            // create directory for plugin
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
