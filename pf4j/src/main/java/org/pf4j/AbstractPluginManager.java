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

import org.pf4j.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class implements the boilerplate plugin code that any {@link PluginManager}
 * implementation would have to support.
 * It helps cut the noise out of the subclass that handles plugin management.
 *
 * <p>This class is not thread-safe.
 *
 * @author Decebal Suiu
 */
public abstract class AbstractPluginManager implements PluginManager {

    private static final Logger log = LoggerFactory.getLogger(AbstractPluginManager.class);

    public static final String PLUGINS_DIR_PROPERTY_NAME = "pf4j.pluginsDir";
    public static final String MODE_PROPERTY_NAME = "pf4j.mode";

    public static final String DEFAULT_PLUGINS_DIR = "plugins";
    public static final String DEVELOPMENT_PLUGINS_DIR = "../plugins";

    protected final List<Path> pluginsRoots = new ArrayList<>();

    protected ExtensionFinder extensionFinder;

    protected PluginDescriptorFinder pluginDescriptorFinder;

    /**
     * A map of plugins this manager is responsible for (the key is the 'pluginId').
     */
    protected Map<String, PluginWrapper> plugins;

    /**
     * A map of plugin class loaders (the key is the 'pluginId').
     */
    protected Map<String, ClassLoader> pluginClassLoaders;

    /**
     * A list with unresolved plugins (unresolved dependency).
     */
    protected List<PluginWrapper> unresolvedPlugins;

    /**
     * A list with all resolved plugins (resolved dependency).
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
     * Cache value for the runtime mode.
     * No need to re-read it because it wont change at runtime.
     */
    protected RuntimeMode runtimeMode;

    /**
     * The system version used for comparisons to the plugin requires attribute.
     */
    protected String systemVersion = "0.0.0";

    protected PluginRepository pluginRepository;
    protected PluginFactory pluginFactory;
    protected ExtensionFactory extensionFactory;
    protected PluginStatusProvider pluginStatusProvider;
    protected DependencyResolver dependencyResolver;
    protected PluginLoader pluginLoader;
    protected boolean exactVersionAllowed = false;

    protected VersionManager versionManager;

    /**
     * The plugins roots are supplied as comma-separated list by {@code System.getProperty("pf4j.pluginsDir", "plugins")}.
     */
    protected AbstractPluginManager() {
        initialize();
    }

    /**
     * Constructs {@code AbstractPluginManager} with the given plugins roots.
     *
     * @param pluginsRoots the roots to search for plugins
     */
    protected AbstractPluginManager(Path... pluginsRoots) {
        this(Arrays.asList(pluginsRoots));
    }

    /**
     * Constructs {@code AbstractPluginManager} with the given plugins roots.
     *
     * @param pluginsRoots the roots to search for plugins
     */
    protected AbstractPluginManager(List<Path> pluginsRoots) {
        this.pluginsRoots.addAll(pluginsRoots);

        initialize();
    }

    @Override
    public void setSystemVersion(String version) {
        systemVersion = version;
    }

    @Override
    public String getSystemVersion() {
        return systemVersion;
    }

    /**
     * Returns a copy of plugins.
     */
    @Override
    public List<PluginWrapper> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    /**
     * Returns a copy of plugins with that state.
     */
    @Override
    public List<PluginWrapper> getPlugins(PluginState pluginState) {
        List<PluginWrapper> plugins = new ArrayList<>();
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
    public String loadPlugin(Path pluginPath) {
        if ((pluginPath == null) || Files.notExists(pluginPath)) {
            throw new IllegalArgumentException(String.format("Specified plugin %s does not exist!", pluginPath));
        }

        log.debug("Loading plugin from '{}'", pluginPath);

        PluginWrapper pluginWrapper = loadPluginFromPath(pluginPath);

        // try to resolve  the loaded plugin together with other possible plugins that depend on this plugin
        resolvePlugins();

        return pluginWrapper.getDescriptor().getPluginId();
    }

    /**
     * Load plugins.
     */
    @Override
    public void loadPlugins() {
        log.debug("Lookup plugins in '{}'", pluginsRoots);
        // check for plugins roots
        if (pluginsRoots.isEmpty()) {
            log.warn("No plugins roots configured");
            return;
        }
        pluginsRoots.forEach(path -> {
            if (Files.notExists(path) || !Files.isDirectory(path)) {
                log.warn("No '{}' root", path);
            }
        });

        // get all plugin paths from repository
        List<Path> pluginPaths = pluginRepository.getPluginPaths();

        // check for no plugins
        if (pluginPaths.isEmpty()) {
            log.info("No plugins");
            return;
        }

        log.debug("Found {} possible plugins: {}", pluginPaths.size(), pluginPaths);

        // load plugins from plugin paths
        for (Path pluginPath : pluginPaths) {
            try {
                loadPluginFromPath(pluginPath);
            } catch (PluginRuntimeException e) {
                log.error(e.getMessage(), e);
            }
        }

        // resolve plugins
        try {
            resolvePlugins();
        } catch (PluginRuntimeException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Unload all plugins
     */
    @Override
    public void unloadPlugins() {
        // wrap resolvedPlugins in new list because of concurrent modification
        for (PluginWrapper pluginWrapper : new ArrayList<>(resolvedPlugins)) {
            unloadPlugin(pluginWrapper.getPluginId());
        }
    }

    /**
     * Unload the specified plugin and it's dependents.
     */
    @Override
    public boolean unloadPlugin(String pluginId) {
        return unloadPlugin(pluginId, true);
    }

    protected boolean unloadPlugin(String pluginId, boolean unloadDependents) {
        try {
            if (unloadDependents) {
                List<String> dependents = dependencyResolver.getDependents(pluginId);
                while (!dependents.isEmpty()) {
                    String dependent = dependents.remove(0);
                    unloadPlugin(dependent, false);
                    dependents.addAll(0, dependencyResolver.getDependents(dependent));
                }
            }

            PluginState pluginState = stopPlugin(pluginId, false);
            if (PluginState.STARTED == pluginState) {
                return false;
            }

            PluginWrapper pluginWrapper = getPlugin(pluginId);
            log.info("Unload plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()));

            // remove the plugin
            plugins.remove(pluginId);
            getResolvedPlugins().remove(pluginWrapper);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

            // remove the classloader
            Map<String, ClassLoader> pluginClassLoaders = getPluginClassLoaders();
            if (pluginClassLoaders.containsKey(pluginId)) {
                ClassLoader classLoader = pluginClassLoaders.remove(pluginId);
                if (classLoader instanceof Closeable) {
                    try {
                        ((Closeable) classLoader).close();
                    } catch (IOException e) {
                        throw new PluginRuntimeException(e, "Cannot close classloader");
                    }
                }
            }

            return true;
        } catch (IllegalArgumentException e) {
            // ignore not found exceptions because this method is recursive
        }

        return false;
    }

    @Override
    public boolean deletePlugin(String pluginId) {
        checkPluginId(pluginId);

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        // stop the plugin if it's started
        PluginState pluginState = stopPlugin(pluginId);
        if (PluginState.STARTED == pluginState) {
            log.error("Failed to stop plugin '{}' on delete", pluginId);
            return false;
        }

        // get an instance of plugin before the plugin is unloaded
        // for reason see https://github.com/pf4j/pf4j/issues/309
        Plugin plugin = pluginWrapper.getPlugin();

        if (!unloadPlugin(pluginId)) {
            log.error("Failed to unload plugin '{}' on delete", pluginId);
            return false;
        }

        // notify the plugin as it's deleted
        plugin.delete();

        Path pluginPath = pluginWrapper.getPluginPath();

        return pluginRepository.deletePluginPath(pluginPath);
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
                    log.info("Start plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()));
                    pluginWrapper.getPlugin().start();
                    pluginWrapper.setPluginState(PluginState.STARTED);
                    pluginWrapper.setFailedException(null);
                    startedPlugins.add(pluginWrapper);
                } catch (Exception | LinkageError e) {
                    pluginWrapper.setPluginState(PluginState.FAILED);
                    pluginWrapper.setFailedException(e);
                    log.error("Unable to start plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()), e);
                } finally {
                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                }
            }
        }
    }

    /**
     * Start the specified plugin and its dependencies.
     */
    @Override
    public PluginState startPlugin(String pluginId) {
        checkPluginId(pluginId);

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.STARTED == pluginState) {
            log.debug("Already started plugin '{}'", getPluginLabel(pluginDescriptor));
            return PluginState.STARTED;
        }

        if (!resolvedPlugins.contains(pluginWrapper)) {
            log.warn("Cannot start an unresolved plugin '{}'", getPluginLabel(pluginDescriptor));
            return pluginState;
        }

        if (PluginState.DISABLED == pluginState) {
            // automatically enable plugin on manual plugin start
            if (!enablePlugin(pluginId)) {
                return pluginState;
            }
        }

        for (PluginDependency dependency : pluginDescriptor.getDependencies()) {
            // start dependency only if it marked as required (non optional) or if it optional and loaded
            if (!dependency.isOptional() || plugins.containsKey(dependency.getPluginId())) {
                startPlugin(dependency.getPluginId());
            }
        }

        log.info("Start plugin '{}'", getPluginLabel(pluginDescriptor));
        pluginWrapper.getPlugin().start();
        pluginWrapper.setPluginState(PluginState.STARTED);
        startedPlugins.add(pluginWrapper);

        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

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
                    log.info("Stop plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()));
                    pluginWrapper.getPlugin().stop();
                    pluginWrapper.setPluginState(PluginState.STOPPED);
                    itr.remove();

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (PluginRuntimeException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Stop the specified plugin and it's dependents.
     */
    @Override
    public PluginState stopPlugin(String pluginId) {
        return stopPlugin(pluginId, true);
    }

    protected PluginState stopPlugin(String pluginId, boolean stopDependents) {
        checkPluginId(pluginId);

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.STOPPED == pluginState) {
            log.debug("Already stopped plugin '{}'", getPluginLabel(pluginDescriptor));
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

        log.info("Stop plugin '{}'", getPluginLabel(pluginDescriptor));
        pluginWrapper.getPlugin().stop();
        pluginWrapper.setPluginState(PluginState.STOPPED);
        startedPlugins.remove(pluginWrapper);

        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

        return pluginWrapper.getPluginState();
    }

    protected void checkPluginId(String pluginId) {
        if (!plugins.containsKey(pluginId)) {
            throw new IllegalArgumentException(String.format("Unknown pluginId %s", pluginId));
        }
    }

    @Override
    public boolean disablePlugin(String pluginId) {
        checkPluginId(pluginId);

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.DISABLED == pluginState) {
            log.debug("Already disabled plugin '{}'", getPluginLabel(pluginDescriptor));
            return true;
        }

        if (PluginState.STOPPED == stopPlugin(pluginId)) {
            pluginWrapper.setPluginState(PluginState.DISABLED);

            firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, PluginState.STOPPED));

            pluginStatusProvider.disablePlugin(pluginId);
            log.info("Disabled plugin '{}'", getPluginLabel(pluginDescriptor));

            return true;
        }

        return false;
    }

    @Override
    public boolean enablePlugin(String pluginId) {
        checkPluginId(pluginId);

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        if (!isPluginValid(pluginWrapper)) {
            log.warn("Plugin '{}' can not be enabled", getPluginLabel(pluginWrapper.getDescriptor()));
            return false;
        }

        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.DISABLED != pluginState) {
            log.debug("Plugin '{}' is not disabled", getPluginLabel(pluginDescriptor));
            return true;
        }

        pluginStatusProvider.enablePlugin(pluginId);

        pluginWrapper.setPluginState(PluginState.CREATED);

        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

        log.info("Enabled plugin '{}'", getPluginLabel(pluginDescriptor));

        return true;
    }

    /**
     * Get the {@link ClassLoader} for plugin.
     */
    @Override
    public ClassLoader getPluginClassLoader(String pluginId) {
        return pluginClassLoaders.get(pluginId);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Class<?>> getExtensionClasses(String pluginId) {
        List<ExtensionWrapper> extensionsWrapper = extensionFinder.find(pluginId);
        List<Class<?>> extensionClasses = new ArrayList<>(extensionsWrapper.size());
        for (ExtensionWrapper extensionWrapper : extensionsWrapper) {
            Class<?> c = extensionWrapper.getDescriptor().extensionClass;
            extensionClasses.add(c);
        }

        return extensionClasses;
    }

    @Override
    public <T> List<Class<? extends T>> getExtensionClasses(Class<T> type) {
        return getExtensionClasses(extensionFinder.find(type));
    }

    @Override
    public <T> List<Class<? extends T>> getExtensionClasses(Class<T> type, String pluginId) {
        return getExtensionClasses(extensionFinder.find(type, pluginId));
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type) {
        return getExtensions(extensionFinder.find(type));
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type, String pluginId) {
        return getExtensions(extensionFinder.find(type, pluginId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List getExtensions(String pluginId) {
        List<ExtensionWrapper> extensionsWrapper = extensionFinder.find(pluginId);
        List extensions = new ArrayList<>(extensionsWrapper.size());
        for (ExtensionWrapper extensionWrapper : extensionsWrapper) {
            try {
                extensions.add(extensionWrapper.getExtension());
            } catch (PluginRuntimeException e) {
                log.error("Cannot retrieve extension", e);
            }
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

    public PluginLoader getPluginLoader() {
        return pluginLoader;
    }

    @Override
    public Path getPluginsRoot() {
        return pluginsRoots.stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("pluginsRoots have not been initialized, yet."));
    }

    public List<Path> getPluginsRoots() {
        return Collections.unmodifiableList(pluginsRoots);
    }

    @Override
    public RuntimeMode getRuntimeMode() {
        if (runtimeMode == null) {
            // retrieves the runtime mode from system
            String modeAsString = System.getProperty(MODE_PROPERTY_NAME, RuntimeMode.DEPLOYMENT.toString());
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

    public String getVersion() {
        return Pf4jInfo.VERSION;
    }

    protected abstract PluginRepository createPluginRepository();

    protected abstract PluginFactory createPluginFactory();

    protected abstract ExtensionFactory createExtensionFactory();

    protected abstract PluginDescriptorFinder createPluginDescriptorFinder();

    protected abstract ExtensionFinder createExtensionFinder();

    protected abstract PluginStatusProvider createPluginStatusProvider();

    protected abstract PluginLoader createPluginLoader();

    protected abstract VersionManager createVersionManager();

    protected PluginDescriptorFinder getPluginDescriptorFinder() {
        return pluginDescriptorFinder;
    }

    protected PluginFactory getPluginFactory() {
        return pluginFactory;
    }

    protected Map<String, ClassLoader> getPluginClassLoaders() {
        return pluginClassLoaders;
    }

    protected void initialize() {
        plugins = new HashMap<>();
        pluginClassLoaders = new HashMap<>();
        unresolvedPlugins = new ArrayList<>();
        resolvedPlugins = new ArrayList<>();
        startedPlugins = new ArrayList<>();

        pluginStateListeners = new ArrayList<>();

        if (pluginsRoots.isEmpty()) {
            pluginsRoots.addAll(createPluginsRoot());
        }

        pluginRepository = createPluginRepository();
        pluginFactory = createPluginFactory();
        extensionFactory = createExtensionFactory();
        pluginDescriptorFinder = createPluginDescriptorFinder();
        extensionFinder = createExtensionFinder();
        pluginStatusProvider = createPluginStatusProvider();
        pluginLoader = createPluginLoader();

        versionManager = createVersionManager();
        dependencyResolver = new DependencyResolver(versionManager);
    }

    /**
     * Add the possibility to override the plugins roots.
     * If a {@link #PLUGINS_DIR_PROPERTY_NAME} system property is defined than this method returns that roots.
     * If {@link #getRuntimeMode()} returns {@link RuntimeMode#DEVELOPMENT} than {@link #DEVELOPMENT_PLUGINS_DIR}
     * is returned else this method returns {@link #DEFAULT_PLUGINS_DIR}.
     *
     * @return the plugins root
     */
    protected List<Path> createPluginsRoot() {
        String pluginsDir = System.getProperty(PLUGINS_DIR_PROPERTY_NAME);
        if (pluginsDir != null && !pluginsDir.isEmpty()) {
            return Arrays.stream(pluginsDir.split(","))
                .map(String::trim)
                .map(Paths::get)
                .collect(Collectors.toList());
        }

        pluginsDir = isDevelopment() ? DEVELOPMENT_PLUGINS_DIR : DEFAULT_PLUGINS_DIR;
        return Collections.singletonList(Paths.get(pluginsDir));
    }

    /**
     * Check if this plugin is valid (satisfies "requires" param) for a given system version.
     *
     * @param pluginWrapper the plugin to check
     * @return true if plugin satisfies the "requires" or if requires was left blank
     */
    protected boolean isPluginValid(PluginWrapper pluginWrapper) {
        String requires = pluginWrapper.getDescriptor().getRequires().trim();
        if (!isExactVersionAllowed() && requires.matches("^\\d+\\.\\d+\\.\\d+$")) {
            // If exact versions are not allowed in requires, rewrite to >= expression
            requires = ">=" + requires;
        }
        if (systemVersion.equals("0.0.0") || versionManager.checkVersionConstraint(systemVersion, requires)) {
            return true;
        }

        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        log.warn("Plugin '{}' requires a minimum system version of {}, and you have {}",
            getPluginLabel(pluginDescriptor),
            requires,
            getSystemVersion());

        return false;
    }

    protected boolean isPluginDisabled(String pluginId) {
        return pluginStatusProvider.isPluginDisabled(pluginId);
    }

    protected void resolvePlugins() {
        // retrieves the plugins descriptors
        List<PluginDescriptor> descriptors = new ArrayList<>();
        for (PluginWrapper plugin : plugins.values()) {
            descriptors.add(plugin.getDescriptor());
        }

        DependencyResolver.Result result = dependencyResolver.resolve(descriptors);

        if (result.hasCyclicDependency()) {
            throw new DependencyResolver.CyclicDependencyException();
        }

        List<String> notFoundDependencies = result.getNotFoundDependencies();
        if (!notFoundDependencies.isEmpty()) {
            throw new DependencyResolver.DependenciesNotFoundException(notFoundDependencies);
        }

        List<DependencyResolver.WrongDependencyVersion> wrongVersionDependencies = result.getWrongVersionDependencies();
        if (!wrongVersionDependencies.isEmpty()) {
            throw new DependencyResolver.DependenciesWrongVersionException(wrongVersionDependencies);
        }

        List<String> sortedPlugins = result.getSortedPlugins();

        // move plugins from "unresolved" to "resolved"
        for (String pluginId : sortedPlugins) {
            PluginWrapper pluginWrapper = plugins.get(pluginId);
            if (unresolvedPlugins.remove(pluginWrapper)) {
                PluginState pluginState = pluginWrapper.getPluginState();
                if (pluginState != PluginState.DISABLED) {
                    pluginWrapper.setPluginState(PluginState.RESOLVED);
                }

                resolvedPlugins.add(pluginWrapper);
                log.info("Plugin '{}' resolved", getPluginLabel(pluginWrapper.getDescriptor()));

                firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
            }
        }
    }

    protected synchronized void firePluginStateEvent(PluginStateEvent event) {
        for (PluginStateListener listener : pluginStateListeners) {
            log.trace("Fire '{}' to '{}'", event, listener);
            listener.pluginStateChanged(event);
        }
    }

    protected PluginWrapper loadPluginFromPath(Path pluginPath) {
        // Test for plugin path duplication
        String pluginId = idForPath(pluginPath);
        if (pluginId != null) {
            throw new PluginAlreadyLoadedException(pluginId, pluginPath);
        }

        // Retrieve and validate the plugin descriptor
        PluginDescriptorFinder pluginDescriptorFinder = getPluginDescriptorFinder();
        log.debug("Use '{}' to find plugins descriptors", pluginDescriptorFinder);
        log.debug("Finding plugin descriptor for plugin '{}'", pluginPath);
        PluginDescriptor pluginDescriptor = pluginDescriptorFinder.find(pluginPath);
        validatePluginDescriptor(pluginDescriptor);

        // Check there are no loaded plugins with the retrieved id
        pluginId = pluginDescriptor.getPluginId();
        if (plugins.containsKey(pluginId)) {
            PluginWrapper loadedPlugin = getPlugin(pluginId);
            throw new PluginRuntimeException("There is an already loaded plugin ({}) "
                    + "with the same id ({}) as the plugin at path '{}'. Simultaneous loading "
                    + "of plugins with the same PluginId is not currently supported.\n"
                    + "As a workaround you may include PluginVersion and PluginProvider "
                    + "in PluginId.",
                loadedPlugin, pluginId, pluginPath);
        }

        log.debug("Found descriptor {}", pluginDescriptor);
        String pluginClassName = pluginDescriptor.getPluginClass();
        log.debug("Class '{}' for plugin '{}'",  pluginClassName, pluginPath);

        // load plugin
        log.debug("Loading plugin '{}'", pluginPath);
        ClassLoader pluginClassLoader = getPluginLoader().loadPlugin(pluginPath, pluginDescriptor);
        log.debug("Loaded plugin '{}' with class loader '{}'", pluginPath, pluginClassLoader);

        PluginWrapper pluginWrapper = createPluginWrapper(pluginDescriptor, pluginPath, pluginClassLoader);

        // test for disabled plugin
        if (isPluginDisabled(pluginDescriptor.getPluginId())) {
            log.info("Plugin '{}' is disabled", pluginPath);
            pluginWrapper.setPluginState(PluginState.DISABLED);
        }

        // validate the plugin
        if (!isPluginValid(pluginWrapper)) {
            log.warn("Plugin '{}' is invalid and it will be disabled", pluginPath);
            pluginWrapper.setPluginState(PluginState.DISABLED);
        }

        log.debug("Created wrapper '{}' for plugin '{}'", pluginWrapper, pluginPath);

        pluginId = pluginDescriptor.getPluginId();

        // add plugin to the list with plugins
        plugins.put(pluginId, pluginWrapper);
        getUnresolvedPlugins().add(pluginWrapper);

        // add plugin class loader to the list with class loaders
        getPluginClassLoaders().put(pluginId, pluginClassLoader);

        return pluginWrapper;
    }

    /**
     * creates the plugin wrapper. override this if you want to prevent plugins having full access to the plugin manager
     *
     * @return
     */
    protected PluginWrapper createPluginWrapper(PluginDescriptor pluginDescriptor, Path pluginPath, ClassLoader pluginClassLoader) {
        // create the plugin wrapper
        log.debug("Creating wrapper for plugin '{}'", pluginPath);
        PluginWrapper pluginWrapper = new PluginWrapper(this, pluginDescriptor, pluginPath, pluginClassLoader);
        pluginWrapper.setPluginFactory(getPluginFactory());
        return pluginWrapper;
    }

    /**
     * Tests for already loaded plugins on given path.
     *
     * @param pluginPath the path to investigate
     * @return id of plugin or null if not loaded
     */
    protected String idForPath(Path pluginPath) {
        for (PluginWrapper plugin : plugins.values()) {
            if (plugin.getPluginPath().equals(pluginPath)) {
                return plugin.getPluginId();
            }
        }

        return null;
    }

    /**
     * Override this to change the validation criteria.
     *
     * @param descriptor the plugin descriptor to validate
     * @throws PluginRuntimeException if validation fails
     */
    protected void validatePluginDescriptor(PluginDescriptor descriptor) {
        if (StringUtils.isNullOrEmpty(descriptor.getPluginId())) {
            throw new PluginRuntimeException("Field 'id' cannot be empty");
        }

        if (descriptor.getVersion() == null) {
            throw new PluginRuntimeException("Field 'version' cannot be empty");
        }
    }

    /**
     * @return true if exact versions in requires is allowed
     */
    public boolean isExactVersionAllowed() {
        return exactVersionAllowed;
    }

    /**
     * Set to true to allow requires expression to be exactly x.y.z.
     * The default is false, meaning that using an exact version x.y.z will
     * implicitly mean the same as &gt;=x.y.z
     *
     * @param exactVersionAllowed set to true or false
     */
    public void setExactVersionAllowed(boolean exactVersionAllowed) {
        this.exactVersionAllowed = exactVersionAllowed;
    }

    @Override
    public VersionManager getVersionManager() {
        return versionManager;
    }

    /**
     * The plugin label is used in logging and it's a string in format {@code pluginId@pluginVersion}.
     */
    protected String getPluginLabel(PluginDescriptor pluginDescriptor) {
        return pluginDescriptor.getPluginId() + "@" + pluginDescriptor.getVersion();
    }

    @SuppressWarnings("unchecked")
    protected <T> List<Class<? extends T>> getExtensionClasses(List<ExtensionWrapper<T>> extensionsWrapper) {
        List<Class<? extends T>> extensionClasses = new ArrayList<>(extensionsWrapper.size());
        for (ExtensionWrapper<T> extensionWrapper : extensionsWrapper) {
            Class<T> c = (Class<T>) extensionWrapper.getDescriptor().extensionClass;
            extensionClasses.add(c);
        }

        return extensionClasses;
    }

    protected <T> List<T> getExtensions(List<ExtensionWrapper<T>> extensionsWrapper) {
        List<T> extensions = new ArrayList<>(extensionsWrapper.size());
        for (ExtensionWrapper<T> extensionWrapper : extensionsWrapper) {
            try {
                extensions.add(extensionWrapper.getExtension());
            } catch (PluginRuntimeException e) {
                log.error("Cannot retrieve extension", e);
            }
        }

        return extensions;
    }

}
