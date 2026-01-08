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

import java.nio.file.Path;

/**
 * A wrapper over plugin instance.
 *
 * @author Decebal Suiu
 */
public class PluginWrapper {

    private final PluginManager pluginManager;
    private final PluginDescriptor descriptor;
    private final Path pluginPath;
    private final ClassLoader pluginClassLoader;
    private PluginFactory pluginFactory;
    private PluginState pluginState;
    private final RuntimeMode runtimeMode;

    private Throwable failedException;

    Plugin plugin; // cache

    public PluginWrapper(PluginManager pluginManager, PluginDescriptor descriptor, Path pluginPath, ClassLoader pluginClassLoader) {
        this.pluginManager = pluginManager;
        this.descriptor = descriptor;
        this.pluginPath = pluginPath;
        this.pluginClassLoader = pluginClassLoader;

        pluginState = PluginState.CREATED;
        runtimeMode = pluginManager.getRuntimeMode();
    }

    /**
     * Returns the plugin manager.
     *
     * @return the plugin manager
     */
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Returns the plugin descriptor.
     *
     * @return the plugin descriptor
     */
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the path of this plugin.
     *
     * @return the path of this plugin
     */
    public Path getPluginPath() {
        return pluginPath;
    }

    /**
     * Returns the plugin {@link ClassLoader} used to load classes and resources for this plug-in.
     * <p>
     * The class loader can be used to directly access plug-in resources and classes.
     *
     * @return the plugin class loader
     */
    public ClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    /**
     * Returns the plugin instance.
     *
     * @return the plugin instance
     */
    public Plugin getPlugin() {
        if (plugin == null && pluginState != PluginState.UNLOADED) {
            plugin = pluginFactory.create(this);
        }

        return plugin;
    }

    /**
     * Returns the plugin factory.
     *
     * @return the plugin factory
     */
    public PluginState getPluginState() {
        return pluginState;
    }

    /**
     * Returns the runtime mode.
     *
     * @return the runtime mode
     */
    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    /**
     * Shortcut for {@code getDescriptor().getPluginId()}.
     */
    public String getPluginId() {
        return getDescriptor().getPluginId();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + descriptor.getPluginId().hashCode();

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        PluginWrapper other = (PluginWrapper) obj;

        return descriptor.getPluginId().equals(other.descriptor.getPluginId());

    }

    @Override
    public String toString() {
        return "PluginWrapper [descriptor=" + descriptor + ", pluginPath=" + pluginPath + "]";
    }

    /**
     * Used internally by the framework to set the plugin factory.
     *
     * @param pluginState the plugin state
     */
    public void setPluginState(PluginState pluginState) {
        this.pluginState = pluginState;
    }

    /**
     * Used internally by the framework to set the plugin factory.
     *
     * @param pluginFactory the plugin factory
     */
    public void setPluginFactory(PluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    /**
     * Returns the exception that caused the plugin to fail.
     * <p>
     * When a plugin's state is {@link PluginState#FAILED}, this method returns the exception
     * that caused the failure (e.g., exception during start, dependency failure, etc.).
     * Returns {@code null} if the plugin has not failed or if no exception information is available.
     *
     * @return the exception that caused the plugin to fail, or {@code null} if not applicable
     * @see PluginState#FAILED
     */
    public Throwable getFailedException() {
        return failedException;
    }

    /**
     * Used internally by the framework to set the exception with which the plugin fails to start.
     *
     * @param failedException the exception with which the plugin fails to start
     */
    public void setFailedException(Throwable failedException) {
        this.failedException = failedException;
    }

}
