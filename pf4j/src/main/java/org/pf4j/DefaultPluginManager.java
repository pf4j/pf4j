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

import org.pf4j.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Default implementation of the {@link PluginManager} interface.
 *
 * <p>This class is not thread-safe.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginManager extends AbstractPluginManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultPluginManager.class);

    protected PluginClasspath pluginClasspath;

    public DefaultPluginManager() {
        super();
    }

    public DefaultPluginManager(Path pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new CompoundPluginDescriptorFinder()
            .add(new PropertiesPluginDescriptorFinder())
            .add(new ManifestPluginDescriptorFinder());
    }

    @Override
    protected ExtensionFinder createExtensionFinder() {
        DefaultExtensionFinder extensionFinder = new DefaultExtensionFinder(this);
        addPluginStateListener(extensionFinder);

        return extensionFinder;
    }

    @Override
    protected PluginFactory createPluginFactory() {
        return new DefaultPluginFactory();
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new DefaultExtensionFactory();
    }

    @Override
    protected PluginStatusProvider createPluginStatusProvider() {
        String configDir = System.getProperty("pf4j.pluginsConfigDir");
        Path configPath = configDir != null ? Paths.get(configDir) : getPluginsRoot();
        return new DefaultPluginStatusProvider(configPath);
    }

    @Override
    protected PluginRepository createPluginRepository() {
        return new CompoundPluginRepository()
            .add(new JarPluginRepository(getPluginsRoot()))
            .add(new DefaultPluginRepository(getPluginsRoot(), isDevelopment()));
    }

    @Override
    protected PluginLoader createPluginLoader() {
        return new CompoundPluginLoader()
            .add(new JarPluginLoader(this))
            .add(new DefaultPluginLoader(this, pluginClasspath));
    }

    @Override
    protected VersionManager createVersionManager() {
        return new DefaultVersionManager();
    }

    /**
     * By default if {@link DefaultPluginManager#isDevelopment()} returns true
     * than a {@link DevelopmentPluginClasspath} is returned
     * else this method returns {@link DefaultPluginClasspath}.
     */
    protected PluginClasspath createPluginClasspath() {
        return isDevelopment() ? new DevelopmentPluginClasspath() : new DefaultPluginClasspath();
    }

    @Override
    protected void initialize() {
        pluginClasspath = createPluginClasspath();

        super.initialize();

        if (isDevelopment()) {
            addPluginStateListener(new LoggingPluginStateListener());
        }

        log.info("PF4J version {} in '{}' mode", getVersion(), getRuntimeMode());
    }

    /**
     * Load a plugin from disk. If the path is a zip file, first unpack
     * @param pluginPath plugin location on disk
     * @return PluginWrapper for the loaded plugin or null if not loaded
     * @throws PluginException if problems during load
     */
    @Override
    protected PluginWrapper loadPluginFromPath(Path pluginPath) throws PluginException {
        // First unzip any ZIP files
        try {
            pluginPath = FileUtils.expandIfZip(pluginPath);
        } catch (Exception e) {
            log.warn("Failed to unzip " + pluginPath, e);
            return null;
        }

        return super.loadPluginFromPath(pluginPath);
    }

}
