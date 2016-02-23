/*
 * Copyright 2012 Decebal Suiu
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
package ro.fortsoft.pf4j;

import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.expr.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.util.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

/**
 * Default implementation of the PluginManager interface.
 *
 * @author Decebal Suiu
 */
public class LegacyPluginManager implements PluginManager {

	private static final Logger log = LoggerFactory.getLogger(LegacyPluginManager.class);

	public static final String DEFAULT_PLUGINS_DIRECTORY = "plugins";
	public static final String DEVELOPMENT_PLUGINS_DIRECTORY = "../plugins";

    protected File pluginsDirectory;

    protected ExtensionFinder extensionFinder;

    protected PluginDescriptorFinder pluginDescriptorFinder;

    protected PluginClasspath pluginClasspath;

    /**
     * A map of plugins this manager is responsible for (the key is the 'pluginId').
     */
    protected Map<String, PluginWrapper> plugins;

    /**
     * A map of plugin class loaders (he key is the 'pluginId').
     */
    protected Map<String, PluginClassLoader> pluginClassLoaders;

    /**
     * A relation between 'pluginPath' and 'pluginId'
     */
    protected Map<String, String> pathToIdMap;

    /**
     * A list with unresolved plugins (unresolved dependency).
     */
    protected List<PluginWrapper> unresolvedPlugins;

    /**
     * A list with resolved plugins (resolved dependency).
     */
    protected List<PluginWrapper> resolvedPlugins;

    /**
     * A list with started plugins.
     */
    protected List<PluginWrapper> startedPlugins;

    /**
     * The registered {@link PluginStateListener}s.
     */
    protected List<PluginStateListener> pluginStateListeners;

    /**
     * Cache value for the runtime mode. No need to re-read it because it wont change at
	 * runtime.
     */
    protected RuntimeMode runtimeMode;

    /**
     * The system version used for comparisons to the plugin requires attribute.
     */
    protected Version systemVersion = Version.forIntegers(0, 0, 0);

    protected PluginFactory pluginFactory;
    protected ExtensionFactory extensionFactory;
    protected PluginStatusProvider pluginStatusProvider;
    protected DependencyResolver dependencyResolver;

    /**
     * The plugins repository.
     */
    protected PluginRepository pluginRepository;

    /**
     * The plugins directory is supplied by System.getProperty("pf4j.pluginsDir", "plugins").
     */
    public LegacyPluginManager() {
        this.pluginsDirectory = createPluginsDirectory();

        initialize();
    }

    /**
     * Constructs DefaultPluginManager which the given plugins directory.
     *
     * @param pluginsDirectory the directory to search for plugins
     */
    public LegacyPluginManager(File pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;

        initialize();
    }

    @Override
    public void setSystemVersion(Version version) {
    	systemVersion = version;
    }

    @Override
    public Version getSystemVersion() {
    	return systemVersion;
    }

	@Override
    public List<PluginWrapper> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    @Override
    public List<PluginWrapper> getPlugins(PluginState pluginState) {
        List<PluginWrapper> plugins= new ArrayList<>();
        for (PluginWrapper plugin : getPlugins()) {
            if (pluginState.equals(plugin.getPluginState())) {
                plugins.add(plugin);
            }
        }

        return plugins;
    }

    @Override
	public List<PluginWrapper> getResolvedPlugins() {
		return resolvedPlugins;
	}

	@Override
    public List<PluginWrapper> getUnresolvedPlugins() {
		return unresolvedPlugins;
	}

	@Override
	public List<PluginWrapper> getStartedPlugins() {
		return startedPlugins;
	}

    @Override
    public PluginWrapper getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }

	@Override
	public String loadPlugin(File pluginArchiveFile) {
		if ((pluginArchiveFile == null) || !pluginArchiveFile.exists()) {
			throw new IllegalArgumentException(String.format("Specified plugin %s does not exist!", pluginArchiveFile));
		}

        log.debug("Loading plugin from '{}'", pluginArchiveFile);

		File pluginDirectory = null;
		try {
			pluginDirectory = expandPluginArchive(pluginArchiveFile);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		if ((pluginDirectory == null) || !pluginDirectory.exists()) {
			throw new IllegalArgumentException(String.format("Failed to expand %s", pluginArchiveFile));
		}

		try {
			PluginWrapper pluginWrapper = loadPluginDirectory(pluginDirectory);
			// TODO uninstalled plugin dependencies?
        	unresolvedPlugins.remove(pluginWrapper);
        	resolvedPlugins.add(pluginWrapper);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, null));

			return pluginWrapper.getDescriptor().getPluginId();
		} catch (PluginException e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

    /**
     * Start all active plugins.
     */
	@Override
    public void startPlugins() {
        for (PluginWrapper pluginWrapper : resolvedPlugins) {
            PluginState pluginState = pluginWrapper.getPluginState();
            if ((PluginState.DISABLED != pluginState) && (PluginState.STARTED != pluginState)) {
                try {
                    PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
                    log.info("Start plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
                    pluginWrapper.getPlugin().start();
                    pluginWrapper.setPluginState(PluginState.STARTED);
                    startedPlugins.add(pluginWrapper);

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (PluginException e) {
                    log.error(e.getMessage(), e);
                }
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

    	PluginWrapper pluginWrapper = getPlugin(pluginId);
    	PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
    	if (PluginState.STARTED == pluginState) {
    		log.debug("Already started plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
    		return PluginState.STARTED;
    	}

        if (PluginState.DISABLED == pluginState) {
            // automatically enable plugin on manual plugin start
            if (!enablePlugin(pluginId)) {
                return pluginState;
            }
        }

        for (PluginDependency dependency : pluginDescriptor.getDependencies()) {
    		startPlugin(dependency.getPluginId());
    	}

    	try {
    		log.info("Start plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
    		pluginWrapper.getPlugin().start();
    		pluginWrapper.setPluginState(PluginState.STARTED);
    		startedPlugins.add(pluginWrapper);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
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
            PluginState pluginState = pluginWrapper.getPluginState();
            if (PluginState.STARTED == pluginState) {
                try {
                    PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
                    log.info("Stop plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
                    pluginWrapper.getPlugin().stop();
                    pluginWrapper.setPluginState(PluginState.STOPPED);
                    itr.remove();

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (PluginException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Stop the specified plugin and it's dependencies.
     */
    @Override
    public PluginState stopPlugin(String pluginId) {
        return stopPlugin(pluginId, true);
    }

    private PluginState stopPlugin(String pluginId, boolean stopDependents) {
    	if (!plugins.containsKey(pluginId)) {
    		throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
    	}

    	PluginWrapper pluginWrapper = getPlugin(pluginId);
    	PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
    	if (PluginState.STOPPED == pluginState) {
    		log.debug("Already stopped plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
    		return PluginState.STOPPED;
    	}

        // test for disabled plugin
        if (PluginState.DISABLED == pluginState) {
            // do nothing
            return pluginState;
        }

        if (stopDependents) {
            List<String> dependents = dependencyResolver.getDependents(pluginId);
            while (!dependents.isEmpty()) {
                String dependent = dependents.remove(0);
                stopPlugin(dependent, false);
                dependents.addAll(0, dependencyResolver.getDependents(dependent));
            }
        }

    	try {
    		log.info("Stop plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
    		pluginWrapper.getPlugin().stop();
    		pluginWrapper.setPluginState(PluginState.STOPPED);
    		startedPlugins.remove(pluginWrapper);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
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
        List<File> pluginArchives = pluginRepository.getPluginArchives();
        for (File archiveFile : pluginArchives) {
            try {
                expandPluginArchive(archiveFile);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        // check for no plugins
        AndFileFilter pluginsFilter = new AndFileFilter(new DirectoryFileFilter());
        pluginsFilter.addFileFilter(new NotFileFilter(createHiddenPluginFilter()));
        File[] directories = pluginsDirectory.listFiles(pluginsFilter);
        if (directories == null) {
        	directories = new File[0];
        }
        log.debug("Found {} possible plugins: {}", directories.length, directories);
        if (directories.length == 0) {
        	log.info("No plugins");
        	return;
        }

        // load any plugin from plugins directory
       	for (File directory : directories) {
       		try {
       			loadPluginDirectory(directory);
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

    @Override
    public boolean unloadPlugin(String pluginId) {
    	try {
    		PluginState pluginState = stopPlugin(pluginId);
    		if (PluginState.STARTED == pluginState) {
    			return false;
    		}

    		PluginWrapper pluginWrapper = getPlugin(pluginId);
    		PluginDescriptor descriptor = pluginWrapper.getDescriptor();
    		List<PluginDependency> dependencies = descriptor.getDependencies();
    		for (PluginDependency dependency : dependencies) {
    			if (!unloadPlugin(dependency.getPluginId())) {
    				return false;
    			}
    		}

    		// remove the plugin
    		plugins.remove(pluginId);
    		resolvedPlugins.remove(pluginWrapper);
    		pathToIdMap.remove(pluginWrapper.getPluginPath());

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

    		// remove the classloader
    		if (pluginClassLoaders.containsKey(pluginId)) {
    			PluginClassLoader classLoader = pluginClassLoaders.remove(pluginId);
                classLoader.dispose();
    		}

    		return true;
    	} catch (IllegalArgumentException e) {
    		// ignore not found exceptions because this method is recursive
    	}

    	return false;
    }

    @Override
    public boolean disablePlugin(String pluginId) {
        if (!plugins.containsKey(pluginId)) {
            throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
        }

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.DISABLED == pluginState) {
        	log.debug("Already disabled plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
           	return true;
        }

        if (PluginState.STOPPED == stopPlugin(pluginId)) {
            pluginWrapper.setPluginState(PluginState.DISABLED);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, PluginState.STOPPED));

            if (!pluginStatusProvider.disablePlugin(pluginId)) {
                return false;
            }

            log.info("Disabled plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());

            return true;
        }

        return false;
    }

    @Override
    public boolean enablePlugin(String pluginId) {
        if (!plugins.containsKey(pluginId)) {
            throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
        }

        PluginWrapper pluginWrapper = getPlugin(pluginId);
       	if (!isPluginValid(pluginWrapper)) {
        	log.warn("Plugin '{}:{}' can not be enabled", pluginWrapper.getPluginId(),
        			pluginWrapper.getDescriptor().getVersion());
            return false;
        }

        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.DISABLED != pluginState) {
            log.debug("Plugin '{}:{}' is not disabled", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());
            return true;
        }

        if (!pluginStatusProvider.enablePlugin(pluginId)) {
            return false;
        }

        pluginWrapper.setPluginState(PluginState.CREATED);

        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

        log.info("Enabled plugin '{}:{}'", pluginDescriptor.getPluginId(), pluginDescriptor.getVersion());

        return true;
    }

    @Override
	public boolean deletePlugin(String pluginId) {
    	if (!plugins.containsKey(pluginId)) {
    		throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
    	}

		PluginWrapper pluginWrapper = getPlugin(pluginId);
		PluginState pluginState = stopPlugin(pluginId);
		if (PluginState.STARTED == pluginState) {
			log.error("Failed to stop plugin {} on delete", pluginId);
			return false;
		}

		if (!unloadPlugin(pluginId)) {
			log.error("Failed to unload plugin {} on delete", pluginId);
			return false;
		}

		File pluginFolder = new File(pluginsDirectory, pluginWrapper.getPluginPath());

		if (pluginFolder.exists()) {
			FileUtils.delete(pluginFolder);
		}

        pluginRepository.deletePluginArchive(pluginWrapper.getPluginPath());

		return true;
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
		List<T> extensions = new ArrayList<>(extensionsWrapper.size());
		for (ExtensionWrapper<T> extensionWrapper : extensionsWrapper) {
			extensions.add(extensionWrapper.getExtension());
		}

		return extensions;
	}

    @Override
    public Set<String> getExtensionClassNames(String pluginId) {
        return extensionFinder.findClassNames(pluginId);
    }

    @Override
    public ExtensionFactory getExtensionFactory() {
        return extensionFactory;
    }

    @Override
	public RuntimeMode getRuntimeMode() {
    	if (runtimeMode == null) {
        	// retrieves the runtime mode from system
        	String modeAsString = System.getProperty("pf4j.mode", RuntimeMode.DEPLOYMENT.toString());
        	runtimeMode = RuntimeMode.byName(modeAsString);
    	}

		return runtimeMode;
	}

    @Override
    public PluginWrapper whichPlugin(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        for (PluginWrapper plugin : resolvedPlugins) {
            if (plugin.getPluginClassLoader() == classLoader) {
            	return plugin;
            }
        }

        return null;
    }

    @Override
    public synchronized void addPluginStateListener(PluginStateListener listener) {
        pluginStateListeners.add(listener);
    }

    @Override
    public synchronized void removePluginStateListener(PluginStateListener listener) {
        pluginStateListeners.remove(listener);
    }

    public Version getVersion() {
        String version = null;

        Package pf4jPackage = getClass().getPackage();
        if (pf4jPackage != null) {
            version = pf4jPackage.getImplementationVersion();
            if (version == null) {
                version = pf4jPackage.getSpecificationVersion();
            }
        }

        return (version != null) ? Version.valueOf(version) : Version.forIntegers(0, 0, 0);
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
    	LegacyExtensionFinder extensionFinder = new LegacyExtensionFinder(this);
        addPluginStateListener(extensionFinder);

        return extensionFinder;
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

    protected PluginStatusProvider createPluginStatusProvider() {
        return new DefaultPluginStatusProvider(pluginsDirectory);
    }

    protected PluginRepository createPluginRepository() {
        return new DefaultPluginRepository(pluginsDirectory, new ZipFileFilter());
    }

    protected boolean isPluginDisabled(String pluginId) {
    	return pluginStatusProvider.isPluginDisabled(pluginId);
    }

    protected boolean isPluginValid(PluginWrapper pluginWrapper) {
    	Expression requires = pluginWrapper.getDescriptor().getRequires();
    	Version system = getSystemVersion();
    	if (requires.interpret(system)) {
    		return true;
    	}

    	log.warn("Plugin '{}:{}' requires a minimum system version of {}",
    			pluginWrapper.getPluginId(),
    			pluginWrapper.getDescriptor().getVersion(),
    			requires);

    	return false;
    }

    protected FileFilter createHiddenPluginFilter() {
    	OrFileFilter hiddenPluginFilter = new OrFileFilter(new HiddenFilter());

        if (RuntimeMode.DEVELOPMENT.equals(getRuntimeMode())) {
            hiddenPluginFilter.addFileFilter(new NameFileFilter("target"));
        }

        return hiddenPluginFilter;
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

    /**
     * Add the possibility to override the PluginFactory..
     */
    protected PluginFactory createPluginFactory() {
        return new DefaultPluginFactory();
    }

    /**
     * Add the possibility to override the ExtensionFactory.
     */
    protected ExtensionFactory createExtensionFactory() {
        return new DefaultExtensionFactory();
    }

    /**
     * Add the possibility to override the PluginClassLoader.
     */
    protected PluginClassLoader createPluginClassLoader(PluginDescriptor pluginDescriptor) {
        return new PluginClassLoader(this, pluginDescriptor, getClass().getClassLoader());
    }

    private void initialize() {
		plugins = new HashMap<>();
        pluginClassLoaders = new HashMap<>();
        pathToIdMap = new HashMap<>();
        unresolvedPlugins = new ArrayList<>();
        resolvedPlugins = new ArrayList<>();
        startedPlugins = new ArrayList<>();

        pluginStateListeners = new ArrayList<>();

        log.info("PF4J version {} in '{}' mode", getVersion(), getRuntimeMode());

        pluginClasspath = createPluginClasspath();
        pluginFactory = createPluginFactory();
        extensionFactory = createExtensionFactory();
        pluginDescriptorFinder = createPluginDescriptorFinder();
        extensionFinder = createExtensionFinder();
        pluginStatusProvider = createPluginStatusProvider();
        pluginRepository = createPluginRepository();

        try {
            pluginsDirectory = pluginsDirectory.getCanonicalFile();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        System.setProperty("pf4j.pluginsDir", pluginsDirectory.getAbsolutePath());
	}

    private PluginWrapper loadPluginDirectory(File pluginDirectory) throws PluginException {
        // try to load the plugin
		String pluginName = pluginDirectory.getName();
        String pluginPath = "/".concat(pluginName);

        // test for plugin duplication
        if (plugins.get(pathToIdMap.get(pluginPath)) != null) {
            return null;
        }

        // retrieves the plugin descriptor
        log.debug("Find plugin descriptor '{}'", pluginPath);
        PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginDirectory);
        log.debug("Descriptor " + pluginDescriptor);
        String pluginClassName = pluginDescriptor.getPluginClass();
        log.debug("Class '{}' for plugin '{}'",  pluginClassName, pluginPath);

        // load plugin
        log.debug("Loading plugin '{}'", pluginPath);
        PluginClassLoader pluginClassLoader = createPluginClassLoader(pluginDescriptor);
        log.debug("Created class loader '{}'", pluginClassLoader);
        PluginLoader pluginLoader = new PluginLoader(pluginDirectory, pluginClassLoader, pluginClasspath);
        pluginLoader.load();
        log.debug("Loaded plugin '{}'", pluginPath);

        // create the plugin wrapper
        log.debug("Creating wrapper for plugin '{}'", pluginPath);
        PluginWrapper pluginWrapper = new PluginWrapper(this, pluginDescriptor, pluginPath, pluginClassLoader);
        pluginWrapper.setPluginFactory(pluginFactory);
        pluginWrapper.setRuntimeMode(getRuntimeMode());

        // test for disabled plugin
        if (isPluginDisabled(pluginDescriptor.getPluginId())) {
            log.info("Plugin '{}' is disabled", pluginPath);
            pluginWrapper.setPluginState(PluginState.DISABLED);
        }

        // validate the plugin
        if (!isPluginValid(pluginWrapper)) {
        	log.info("Plugin '{}' is disabled", pluginPath);
        	pluginWrapper.setPluginState(PluginState.DISABLED);
        }

        log.debug("Created wrapper '{}' for plugin '{}'", pluginWrapper, pluginPath);

        String pluginId = pluginDescriptor.getPluginId();

        // add plugin to the list with plugins
        plugins.put(pluginId, pluginWrapper);
        unresolvedPlugins.add(pluginWrapper);

        // add plugin class loader to the list with class loaders
        pluginClassLoaders.put(pluginId, pluginClassLoader);

        return pluginWrapper;
    }

    private File expandPluginArchive(File pluginArchiveFile) throws IOException {
    	String fileName = pluginArchiveFile.getName();
        long pluginArchiveDate = pluginArchiveFile.lastModified();
        String pluginName = fileName.substring(0, fileName.length() - 4);
        File pluginDirectory = new File(pluginsDirectory, pluginName);
        // check if exists directory or the '.zip' file is "newer" than directory
        if (!pluginDirectory.exists() || (pluginArchiveDate > pluginDirectory.lastModified())) {
        	log.debug("Expand plugin archive '{}' in '{}'", pluginArchiveFile, pluginDirectory);

        	// do not overwrite an old version, remove it
        	if (pluginDirectory.exists()) {
        		FileUtils.delete(pluginDirectory);
        	}

            // create directory for plugin
            pluginDirectory.mkdirs();

            // expand '.zip' file
            Unzip unzip = new Unzip();
            unzip.setSource(pluginArchiveFile);
            unzip.setDestination(pluginDirectory);
            unzip.extract();
        }

        return pluginDirectory;
    }

	private void resolvePlugins() throws PluginException {
		resolveDependencies();
	}

	private void resolveDependencies() throws PluginException {
		dependencyResolver = new DependencyResolver(unresolvedPlugins);
		resolvedPlugins = dependencyResolver.getSortedPlugins();
        for (PluginWrapper pluginWrapper : resolvedPlugins) {
        	unresolvedPlugins.remove(pluginWrapper);
        	log.info("Plugin '{}' resolved", pluginWrapper.getDescriptor().getPluginId());
        }
	}

    private synchronized void firePluginStateEvent(PluginStateEvent event) {
        for (PluginStateListener listener : pluginStateListeners) {
            log.debug("Fire '{}' to '{}'", event, listener);
            listener.pluginStateChanged(event);
        }
    }

}
