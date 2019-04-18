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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * It's an extension of {@link DefaultPluginManager} that supports asynchronous methods (@{AsyncPluginManager}).
 *
 * @author Decebal Suiu
 */
public class DefaultAsyncPluginManager extends DefaultPluginManager implements AsyncPluginManager {

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncPluginManager.class);

    @Override
    public CompletableFuture<Void> loadPluginsAsync() {
        Path pluginsRoot = getPluginsRoot();
        PluginRepository pluginRepository = getPluginRepository();

        log.debug("Lookup plugins in '{}'", pluginsRoot);
        // check for plugins root
        if (Files.notExists(pluginsRoot) || !Files.isDirectory(pluginsRoot)) {
            log.warn("No '{}' root", pluginsRoot);
            return CompletableFuture.completedFuture(null);
        }

        // get all plugins paths from repository
        List<Path> pluginsPaths = pluginRepository.getPluginsPaths();

        // check for no plugins
        if (pluginsPaths.isEmpty()) {
            log.info("No plugins");
            return CompletableFuture.completedFuture(null);
        }

        log.debug("Found {} possible plugins: {}", pluginsPaths.size(), pluginsPaths);

        // load plugins from plugin paths
        CompletableFuture<Void> feature = CompletableFuture.allOf(pluginsPaths.stream()
            .map(this::loadPluginFromPathAsync)
            .filter(Objects::nonNull)
            .toArray(CompletableFuture[]::new));

        // resolve plugins
        feature.thenRun(() -> {
            try {
                resolvePlugins();
            } catch (PluginException e) {
                log.error(e.getMessage(), e);
            }
        });

        return feature;
    }

    @Override
    public CompletableFuture<Void> startPluginsAsync() {
        /*
        // chain start plugins one after another
        CompletableFuture<Void> feature = CompletableFuture.completedFuture(null);
        for (PluginWrapper pluginWrapper : getResolvedPlugins()) {
            feature = feature.thenCompose(v -> startPluginAsync(pluginWrapper));
        }

        return feature;
        */

        return CompletableFuture.allOf(getResolvedPlugins().stream()
            .map(this::startPluginAsync)
            .toArray(CompletableFuture[]::new));
    }

    protected CompletableFuture<PluginWrapper> loadPluginFromPathAsync(Path pluginPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadPluginFromPath(pluginPath);
            } catch (PluginException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        });
    }

    protected CompletableFuture<Void> startPluginAsync(PluginWrapper pluginWrapper) {
        return CompletableFuture.runAsync(() -> {
            PluginState pluginState = pluginWrapper.getPluginState();
            if ((PluginState.DISABLED != pluginState) && (PluginState.STARTED != pluginState)) {
                try {
                    log.info("Start plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()));
                    pluginWrapper.getPlugin().start();
                    pluginWrapper.setPluginState(PluginState.STARTED);
                    getStartedPlugins().add(pluginWrapper);

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

}
