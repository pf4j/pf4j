package org.pf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use this class to wrap the original plugin manager to prevent full access from within plugins.
 * Override AbstractPluginManager.createPluginWrapper to use this class.
 * @deprecated Use application custom {@code PluginContext} instead of {@code PluginWrapper} to communicate with {@link Plugin}.
 * See demo for more details.
 *
 * @author Wolfram Haussig
 */
@Deprecated()
public class SecurePluginManagerWrapper implements PluginManager {

    private static final String PLUGIN_PREFIX = "Plugin ";
    /**
     * the current plugin
     */
    private String currentPluginId;
    /**
     * the original plugin manager
     */
    private PluginManager original;

    /**
     * The registered {@link PluginStateListener}s.
     */
    protected List<PluginStateListener> pluginStateListeners = new ArrayList<>();
    /**
     * wrapper for pluginStateListeners
     */
    private PluginStateListenerWrapper listenerWrapper = new PluginStateListenerWrapper();

    /**
     * constructor
     * @param original the original plugin manager
     * @param currentPluginId the current pluginId
     */
    public SecurePluginManagerWrapper(PluginManager original, String currentPluginId) {
        this.original = original;
        this.currentPluginId = currentPluginId;
    }

    @Override
    public boolean isDevelopment() {
        return original.isDevelopment();
    }

    @Override
    public boolean isNotDevelopment() {
        return original.isNotDevelopment();
    }

    @Override
    public List<PluginWrapper> getPlugins() {
        return Arrays.asList(getPlugin(currentPluginId));
    }

    @Override
    public List<PluginWrapper> getPlugins(PluginState pluginState) {
        return getPlugins().stream().filter(p -> p.getPluginState() == pluginState).collect(Collectors.toList());
    }

    @Override
    public List<PluginWrapper> getResolvedPlugins() {
        return getPlugins().stream().filter(p -> p.getPluginState().ordinal() >= PluginState.RESOLVED.ordinal()).collect(Collectors.toList());
    }

    @Override
    public List<PluginWrapper> getUnresolvedPlugins() {
        return Collections.emptyList();
    }

    @Override
    public List<PluginWrapper> getStartedPlugins() {
        return getPlugins(PluginState.STARTED);
    }

    @Override
    public PluginWrapper getPlugin(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.getPlugin(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getPlugin for foreign pluginId!");
        }
    }

    @Override
    public void loadPlugins() {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute loadPlugins!");
    }

    @Override
    public String loadPlugin(Path pluginPath) {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute loadPlugin!");
    }

    @Override
    public void startPlugins() {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute startPlugins!");
    }

    @Override
    public PluginState startPlugin(String pluginId) {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute startPlugin!");
    }

    @Override
    public void stopPlugins() {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute stopPlugins!");
    }

    @Override
    public PluginState stopPlugin(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.stopPlugin(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute stopPlugin for foreign pluginId!");
        }
    }

    @Override
    public void unloadPlugins() {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute unloadPlugins!");
    }

    @Override
    public boolean unloadPlugin(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.unloadPlugin(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute unloadPlugin for foreign pluginId!");
        }
    }

    @Override
    public boolean disablePlugin(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.disablePlugin(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute disablePlugin for foreign pluginId!");
        }
    }

    @Override
    public boolean enablePlugin(String pluginId) {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute enablePlugin!");
    }

    @Override
    public boolean deletePlugin(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.deletePlugin(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute deletePlugin for foreign pluginId!");
        }
    }

    @Override
    public ClassLoader getPluginClassLoader(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.getPluginClassLoader(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getPluginClassLoader for foreign pluginId!");
        }
    }

    @Override
    public List<Class<?>> getExtensionClasses(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.getExtensionClasses(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getExtensionClasses for foreign pluginId!");
        }
    }

    @Override
    public <T> List<Class<? extends T>> getExtensionClasses(Class<T> type) {
        return getExtensionClasses(type, currentPluginId);
    }

    @Override
    public <T> List<Class<? extends T>> getExtensionClasses(Class<T> type, String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.getExtensionClasses(type, pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getExtensionClasses for foreign pluginId!");
        }
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type) {
        return getExtensions(type, currentPluginId);
    }

    @Override
    public <T> List<T> getExtensions(Class<T> type, String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.getExtensions(type, pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getExtensions for foreign pluginId!");
        }
    }

    @Override
    public List<?> getExtensions(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.getExtensions(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getExtensions for foreign pluginId!");
        }
    }

    @Override
    public Set<String> getExtensionClassNames(String pluginId) {
        if (currentPluginId.equals(pluginId)) {
            return original.getExtensionClassNames(pluginId);
        } else {
            throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getExtensionClassNames for foreign pluginId!");
        }
    }

    @Override
    public ExtensionFactory getExtensionFactory() {
        return original.getExtensionFactory();
    }

    @Override
    public RuntimeMode getRuntimeMode() {
        return original.getRuntimeMode();
    }

    @Override
    public PluginWrapper whichPlugin(Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        PluginWrapper plugin = getPlugin(currentPluginId);
        if (plugin.getPluginClassLoader() == classLoader) {
            return plugin;
        }
        return null;
    }

    @Override
    public void addPluginStateListener(PluginStateListener listener) {
        if (pluginStateListeners.isEmpty()) {
            this.original.addPluginStateListener(listenerWrapper);
        }
        pluginStateListeners.add(listener);
    }

    @Override
    public void removePluginStateListener(PluginStateListener listener) {
        pluginStateListeners.remove(listener);
        if (pluginStateListeners.isEmpty()) {
            this.original.removePluginStateListener(listenerWrapper);
        }
    }

    @Override
    public void setSystemVersion(String version) {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute setSystemVersion!");
    }

    @Override
    public String getSystemVersion() {
        return original.getSystemVersion();
    }

    @Override
    public Path getPluginsRoot() {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getPluginsRoot!");
    }

    @Override
    public List<Path> getPluginsRoots() {
        throw new IllegalAccessError(PLUGIN_PREFIX + currentPluginId + " tried to execute getPluginsRoots!");
    }

    @Override
    public VersionManager getVersionManager() {
        return original.getVersionManager();
    }

    /**
     * Wrapper for PluginStateListener events. will only propagate events if they match the current pluginId
     * @author Wolfram Haussig
     *
     */
    private class PluginStateListenerWrapper implements PluginStateListener {

        @Override
        public void pluginStateChanged(PluginStateEvent event) {
            if (event.getPlugin().getPluginId().equals(currentPluginId)) {
                for (PluginStateListener listener : pluginStateListeners) {
                    listener.pluginStateChanged(event);
                }
            }
        }

    }
}
