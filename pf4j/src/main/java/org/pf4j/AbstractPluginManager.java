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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class implements the boilerplate plugin code that any {@link PluginManager}
 * implementation would have to support.
 * It helps cut the noise out of the subclass that handles plugin management.
 * <p>
 * This class is not thread-safe.
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
     * No need to re-read it because it won't change at runtime.
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
    protected ResolveRecoveryStrategy resolveRecoveryStrategy;

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
        return getPlugins().stream()
            .filter(plugin -> pluginState.equals(plugin.getPluginState()))
            .collect(Collectors.toList());
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

    /**
     * Load a plugin.
     *
     * @param pluginPath the plugin location
     * @return the pluginId of the loaded plugin as specified in its {@linkplain PluginDescriptor metadata}
     * @throws IllegalArgumentException if the plugin location does not exist
     * @throws PluginRuntimeException if something goes wrong
     */
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
                log.error("Cannot load plugin '{}'", pluginPath, e);
            }
        }

        resolvePlugins();
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
     *
     * @param pluginId the pluginId of the plugin to unload
     * @return true if the plugin was unloaded, otherwise false
     */
    @Override
    public boolean unloadPlugin(String pluginId) {
        return unloadPlugin(pluginId, true);
    }

    /**
     * Unload the specified plugin and it's dependents.
     *
     * @param pluginId the pluginId of the plugin to unload
     * @param unloadDependents if true, unload dependents
     * @return true if the plugin was unloaded, otherwise false
     */
    protected boolean unloadPlugin(String pluginId, boolean unloadDependents) {
        return unloadPlugin(pluginId, unloadDependents, true);
    }

    /**
     * Unload the specified plugin and it's dependents.
     *
     * @param pluginId the pluginId of the plugin to unload
     * @param unloadDependents if true, unload dependents
     * @param resolveDependencies if true, resolve dependencies
     * @return true if the plugin was unloaded, otherwise false
     */
    protected boolean unloadPlugin(String pluginId, boolean unloadDependents, boolean resolveDependencies) {
        if (unloadDependents) {
            List<String> dependents = dependencyResolver.getDependents(pluginId);
            while (!dependents.isEmpty()) {
                String dependent = dependents.remove(0);
                unloadPlugin(dependent, false, false);
                dependents.addAll(0, dependencyResolver.getDependents(dependent));
            }
        }

        if (!plugins.containsKey(pluginId)) {
            // nothing to do
            return false;
        }

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        PluginState pluginState;
        try {
            pluginState = stopPlugin(pluginId, false);
            if (pluginState.isStarted()) {
                return false;
            }

            log.info("Unload plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()));
        } catch (Exception e) {
            log.error("Cannot stop plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()), e);
            pluginState = PluginState.FAILED;
        }

        // remove the plugin
        pluginWrapper.setPluginState(PluginState.UNLOADED);
        plugins.remove(pluginId);
        getResolvedPlugins().remove(pluginWrapper);
        getUnresolvedPlugins().remove(pluginWrapper);

        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

        // remove the classloader
        Map<String, ClassLoader> pluginClassLoaders = getPluginClassLoaders();
        if (pluginClassLoaders.containsKey(pluginId)) {
            ClassLoader classLoader = pluginClassLoaders.remove(pluginId);
            if (classLoader instanceof Closeable) {
                try {
                    ((Closeable) classLoader).close();
                    classLoader = null; // help GC to collect the classloader
                } catch (IOException e) {
                    throw new PluginRuntimeException(e, "Cannot close classloader");
                }
            }
        }

        // resolve the plugins again (update plugins graph)
        if (resolveDependencies) {
            resolveDependencies();
        }

        return true;
    }

    @Override
    public boolean deletePlugin(String pluginId) {
        checkPluginId(pluginId);

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        // stop the plugin if it's started
        PluginState pluginState = stopPlugin(pluginId);
        if (pluginState.isStarted()) {
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

        return pluginRepository.deletePluginPath(pluginWrapper.getPluginPath());
    }

    /**
     * Start all active plugins.
     */
    @Override
    public void startPlugins() {
        for (PluginWrapper pluginWrapper : resolvedPlugins) {
            PluginState pluginState = pluginWrapper.getPluginState();
            if (!pluginState.isDisabled() && !pluginState.isStarted()) {
                doStartPlugin(pluginWrapper);
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
        if (pluginState.isStarted()) {
            log.debug("Already started plugin '{}'", getPluginLabel(pluginDescriptor));
            return PluginState.STARTED;
        }

        if (!resolvedPlugins.contains(pluginWrapper)) {
            log.warn("Cannot start an unresolved plugin '{}'", getPluginLabel(pluginDescriptor));
            return pluginState;
        }

        if (pluginState.isDisabled()) {
            // automatically enable plugin on manual plugin start
            if (!enablePlugin(pluginId)) {
                return pluginState;
            }
        }

        // Start and validate dependencies
        if (!startDependencies(pluginWrapper)) {
            return PluginState.FAILED;
        }

        return doStartPlugin(pluginWrapper);
    }

    /**
     * Starts all dependencies of a plugin and validates that required dependencies started successfully.
     * <p>
     * This method is called before {@link #doStartPlugin(PluginWrapper)} to ensure all dependencies
     * are in a valid state before starting the dependent plugin.
     * <p>
     * <b>Behavior for required dependencies:</b>
     * <ul>
     *   <li>If a required dependency fails to start (state is not {@link PluginState#STARTED}),
     *       the dependent plugin will NOT be started</li>
     *   <li>The dependent plugin state is set to {@link PluginState#FAILED}</li>
     *   <li>A {@link PluginRuntimeException} is set as the failure cause</li>
     *   <li>An error is logged indicating which dependency failed</li>
     *   <li>A state change event is fired</li>
     * </ul>
     * <p>
     * <b>Behavior for optional dependencies:</b>
     * <ul>
     *   <li>If an optional dependency is not loaded, it is skipped (no attempt to start)</li>
     *   <li>If an optional dependency is loaded but fails to start, a warning is logged</li>
     *   <li>The dependent plugin will still be started (allows degraded mode/reduced functionality)</li>
     *   <li>It is the plugin's responsibility to handle missing optional dependencies gracefully</li>
     * </ul>
     *
     * @param pluginWrapper the plugin whose dependencies should be started
     * @return {@code true} if all required dependencies started successfully and the plugin can proceed to start;
     *         {@code false} if a required dependency failed to start (plugin state is set to FAILED)
     */
    private boolean startDependencies(PluginWrapper pluginWrapper) {
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        String pluginId = pluginDescriptor.getPluginId();
        PluginState pluginState = pluginWrapper.getPluginState();

        for (PluginDependency dependency : pluginDescriptor.getDependencies()) {
            // Start dependency only if it marked as required (non-optional) or if it optional and loaded
            if (!dependency.isOptional() || plugins.containsKey(dependency.getPluginId())) {
                PluginState dependencyState = startPlugin(dependency.getPluginId());

                // Validate that the dependency started successfully
                if (!dependencyState.isStarted()) {
                    if (dependency.isOptional()) {
                        // Optional dependency failed: log warning but continue
                        log.warn("Optional dependency '{}' of plugin '{}' failed to start (state: {}). " +
                                "Plugin will start with reduced functionality.",
                                dependency.getPluginId(), pluginId, dependencyState);
                    } else {
                        // Required dependency failed: fail fast
                        log.error("Cannot start plugin '{}' because required dependency '{}' failed to start (state: {})",
                                pluginId, dependency.getPluginId(), dependencyState);

                        pluginWrapper.setPluginState(PluginState.FAILED);
                        pluginWrapper.setFailedException(
                            new PluginRuntimeException("Required dependency '" + dependency.getPluginId() + "' failed to start")
                        );
                        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Performs the actual plugin start operation with proper exception handling.
     * This method is used by both {@link #startPlugin(String)} and {@link #startPlugins()}.
     *
     * @param pluginWrapper the plugin wrapper to start
     * @return the plugin state after the start operation
     */
    private PluginState doStartPlugin(PluginWrapper pluginWrapper) {
        PluginState pluginState = pluginWrapper.getPluginState();
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
            if (pluginState.isStarted()) {
                try {
                    log.info("Stop plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()));
                    pluginWrapper.getPlugin().stop();
                    pluginWrapper.setPluginState(PluginState.STOPPED);
                    itr.remove();

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (PluginRuntimeException e) {
                    log.error(e.getMessage(), e);
                    pluginWrapper.setPluginState(PluginState.FAILED);
                    pluginWrapper.setFailedException(e);
                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                }
            } else {
                // do nothing
                log.debug("Plugin '{}' is not started, nothing to stop", getPluginLabel(pluginWrapper.getDescriptor()));
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

    /**
     * Stop the specified plugin and it's dependents.
     *
     * @param pluginId the pluginId of the plugin to stop
     * @param stopDependents if true, stop dependents
     * @return the plugin state after stopping
     */
    protected PluginState stopPlugin(String pluginId, boolean stopDependents) {
        checkPluginId(pluginId);

        // test for started plugin
        if (!checkPluginState(pluginId, PluginState.STARTED)) {
            // do nothing
            log.debug("Plugin '{}' is not started, nothing to stop", getPluginLabel(pluginId));
            return getPlugin(pluginId).getPluginState();
        }

        if (stopDependents) {
            List<String> dependents = dependencyResolver.getDependents(pluginId);
            while (!dependents.isEmpty()) {
                String dependent = dependents.remove(0);
                stopPlugin(dependent, false);
                dependents.addAll(0, dependencyResolver.getDependents(dependent));
            }
        }

        log.info("Stop plugin '{}'", getPluginLabel(pluginId));
        PluginWrapper pluginWrapper = getPlugin(pluginId);
        pluginWrapper.getPlugin().stop();
        pluginWrapper.setPluginState(PluginState.STOPPED);
        getStartedPlugins().remove(pluginWrapper);

        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, PluginState.STARTED));

        return PluginState.STOPPED;
    }

    /**
     * Check if the plugin exists in the list of plugins.
     *
     * @param pluginId the pluginId to check
     * @throws PluginNotFoundException if the plugin does not exist
     */
    protected void checkPluginId(String pluginId) {
        if (!plugins.containsKey(pluginId)) {
            throw new PluginNotFoundException(pluginId);
        }
    }

    /**
     * Check if the plugin state is equals with the value passed for parameter {@code pluginState}.
     *
     * @param pluginId the pluginId to check
     * @return {@code true} if the plugin state is equals with the value passed for parameter {@code pluginState}, otherwise {@code false}
     */
    protected boolean checkPluginState(String pluginId, PluginState pluginState) {
        return getPlugin(pluginId).getPluginState() == pluginState;
    }

    @Override
    public boolean disablePlugin(String pluginId) {
        checkPluginId(pluginId);

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (pluginState.isDisabled()) {
            log.debug("Already disabled plugin '{}'", getPluginLabel(pluginDescriptor));
            return true;
        } else if (pluginState.isStarted()) {
            if (!stopPlugin(pluginId).isStopped()) {
                log.error("Failed to stop plugin '{}' on disable", getPluginLabel(pluginDescriptor));
                return false;
            }
        }

        pluginWrapper.setPluginState(PluginState.DISABLED);

        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

        pluginStatusProvider.disablePlugin(pluginId);
        log.info("Disabled plugin '{}'", getPluginLabel(pluginDescriptor));

        return true;
    }

    @Override
    public boolean enablePlugin(String pluginId) {
        checkPluginId(pluginId);

        PluginWrapper pluginWrapper = getPlugin(pluginId);
        if (!isPluginValid(pluginWrapper)) {
            log.warn("Plugin '{}' can not be enabled", getPluginLabel(pluginWrapper.getDescriptor()));
            pluginWrapper.setFailedException(new PluginRuntimeException("Plugin validation failed"));
            return false;
        }

        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (!pluginState.isDisabled()) {
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
        resolveRecoveryStrategy = ResolveRecoveryStrategy.THROW_EXCEPTION;
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

        log.warn("Plugin '{}' requires a minimum system version of {}, and you have {}",
            getPluginLabel(pluginWrapper.getDescriptor()),
            requires,
            getSystemVersion());

        return false;
    }

    /**
     * Check if the plugin is disabled.
     *
     * @param pluginId the pluginId to check
     * @return true if the plugin is disabled, otherwise false
     */
    protected boolean isPluginDisabled(String pluginId) {
        return pluginStatusProvider.isPluginDisabled(pluginId);
    }

    /**
     * It resolves the plugins by checking the dependencies.
     * It also checks for cyclic dependencies, missing dependencies and wrong versions of the dependencies.
     *
     * @throws PluginRuntimeException if something goes wrong
     */
    protected void resolvePlugins() {
        DependencyResolver.Result result = resolveDependencies();
        List<String> sortedPlugins = result.getSortedPlugins();

        // move plugins from "unresolved" to "resolved"
        for (String pluginId : sortedPlugins) {
            PluginWrapper pluginWrapper = plugins.get(pluginId);
            if (unresolvedPlugins.remove(pluginWrapper)) {
                PluginState pluginState = pluginWrapper.getPluginState();
                if (!pluginState.isDisabled()) {
                    pluginWrapper.setPluginState(PluginState.RESOLVED);
                }

                resolvedPlugins.add(pluginWrapper);
                log.info("Plugin '{}' resolved", getPluginLabel(pluginWrapper.getDescriptor()));

                firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
            }
        }
    }

    /**
     * Fire a plugin state event.
     * This method is called when a plugin is loaded, started, stopped, etc.
     *
     * @param event the plugin state event
     */
    protected synchronized void firePluginStateEvent(PluginStateEvent event) {
        if (event.getPluginState() == event.getOldState()) {
            // ignore events without state change
            return;
        }

        for (PluginStateListener listener : pluginStateListeners) {
            log.trace("Fire '{}' to '{}'", event, listener);
            listener.pluginStateChanged(event);
        }
    }

    /**
     * Load the plugin from the specified path.
     *
     * @param pluginPath the path to the plugin
     * @return the loaded plugin
     * @throws PluginAlreadyLoadedException if the plugin is already loaded
     * @throws InvalidPluginDescriptorException if the plugin is invalid
     */
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
            pluginWrapper.setFailedException(new PluginRuntimeException("Plugin validation failed"));
        }

        log.debug("Created wrapper '{}' for plugin '{}'", pluginWrapper, pluginPath);

        pluginId = pluginDescriptor.getPluginId();

        // add plugin to the list with plugins
        addPlugin(pluginWrapper);
        getUnresolvedPlugins().add(pluginWrapper);

        // add plugin class loader to the list with class loaders
        getPluginClassLoaders().put(pluginId, pluginClassLoader);

        return pluginWrapper;
    }

    /**
     * Creates the plugin wrapper.
     * <p>
     * Override this if you want to prevent plugins having full access to the plugin manager.
     *
     * @param pluginDescriptor the plugin descriptor
     * @param pluginPath the path to the plugin
     * @param pluginClassLoader the class loader for the plugin
     * @return the plugin wrapper
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
     * @throws InvalidPluginDescriptorException if validation fails
     */
    protected void validatePluginDescriptor(PluginDescriptor descriptor) {
        if (StringUtils.isNullOrEmpty(descriptor.getPluginId())) {
            throw new InvalidPluginDescriptorException("Field 'id' cannot be empty");
        }

        if (descriptor.getVersion() == null) {
            throw new InvalidPluginDescriptorException("Field 'version' cannot be empty");
        }
    }

    /**
     * Check if the exact version in requires is allowed.
     *
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
     * The plugin label is used in logging, and it's a string in format {@code pluginId@pluginVersion}.
     *
     * @param pluginDescriptor the plugin descriptor
     * @return the plugin label
     */
    protected String getPluginLabel(PluginDescriptor pluginDescriptor) {
        return pluginDescriptor.getPluginId() + "@" + pluginDescriptor.getVersion();
    }

    /**
     * Shortcut for {@code getPluginLabel(getPlugin(pluginId).getDescriptor())}.
     *
     * @param pluginId the pluginId
     * @return the plugin label
     */
    protected String getPluginLabel(String pluginId) {
        return getPluginLabel(getPlugin(pluginId).getDescriptor());
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

    protected DependencyResolver.Result resolveDependencies() {
        // retrieves the plugins descriptors
        List<PluginDescriptor> descriptors = plugins.values().stream()
            .map(PluginWrapper::getDescriptor)
            .collect(Collectors.toList());

        DependencyResolver.Result result = dependencyResolver.resolve(descriptors);

        if (result.isOK()) {
            return result;
        }

        if (result.hasCyclicDependency()) {
            // cannot recover from cyclic dependency
            throw new DependencyResolver.CyclicDependencyException();
        }

        List<String> notFoundDependencies = result.getNotFoundDependencies();
        if (result.hasNotFoundDependencies() && resolveRecoveryStrategy.equals(ResolveRecoveryStrategy.THROW_EXCEPTION)) {
            throw new DependencyResolver.DependenciesNotFoundException(notFoundDependencies);
        }

        List<DependencyResolver.WrongDependencyVersion> wrongVersionDependencies = result.getWrongVersionDependencies();
        if (result.hasWrongVersionDependencies() && resolveRecoveryStrategy.equals(ResolveRecoveryStrategy.THROW_EXCEPTION)) {
            throw new DependencyResolver.DependenciesWrongVersionException(wrongVersionDependencies);
        }

        List<PluginDescriptor> resolvedDescriptors = new ArrayList<>(descriptors);

        for (String notFoundDependency : notFoundDependencies) {
            List<String> dependents = dependencyResolver.getDependents(notFoundDependency);
            dependents.forEach(dependent -> resolvedDescriptors.removeIf(descriptor -> descriptor.getPluginId().equals(dependent)));
        }

        for (DependencyResolver.WrongDependencyVersion wrongVersionDependency : wrongVersionDependencies) {
            resolvedDescriptors.removeIf(descriptor -> descriptor.getPluginId().equals(wrongVersionDependency.getDependencyId()));
        }

        List<PluginDescriptor> unresolvedDescriptors = new ArrayList<>(descriptors);
        unresolvedDescriptors.removeAll(resolvedDescriptors);

        for (PluginDescriptor unresolvedDescriptor : unresolvedDescriptors) {
            unloadPlugin(unresolvedDescriptor.getPluginId(), false);
        }

        return resolveDependencies();
    }

    /**
     * Retrieve the strategy for handling the recovery of a plugin resolve (load) failure.
     * Default is {@link ResolveRecoveryStrategy#THROW_EXCEPTION}.
     *
     * @return the strategy
     */
    protected ResolveRecoveryStrategy getResolveRecoveryStrategy() {
        return resolveRecoveryStrategy;
    }

    /**
     * Set the strategy for handling the recovery of a plugin resolve (load) failure.
     *
     * @param resolveRecoveryStrategy the strategy
     */
    protected void setResolveRecoveryStrategy(ResolveRecoveryStrategy resolveRecoveryStrategy) {
        Objects.requireNonNull(resolveRecoveryStrategy, "resolveRecoveryStrategy cannot be null");
        this.resolveRecoveryStrategy = resolveRecoveryStrategy;
    }

    void addPlugin(PluginWrapper pluginWrapper) {
        plugins.put(pluginWrapper.getPluginId(), pluginWrapper);
    }

    /**
     * Strategy for handling the recovery of a plugin that could not be resolved
     * (loaded) due to a dependency problem.
     */
    public enum ResolveRecoveryStrategy {

        /**
         * Throw an exception when a resolve (load) failure occurs.
         */
        THROW_EXCEPTION,
        /**
         * Ignore the plugin with the resolve (load) failure and continue.
         * The plugin with problems will be removed/unloaded from the plugins list.
         */
        IGNORE_PLUGIN_AND_CONTINUE
    }

}
