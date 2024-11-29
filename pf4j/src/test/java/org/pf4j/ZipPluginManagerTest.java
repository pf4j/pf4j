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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipPluginManagerTest {

    @Test
    void createPluginDescriptorFinderReturnsPropertiesPluginDescriptorFinder() {
        ZipPluginManager zipPluginManager = new ZipPluginManager();
        assertTrue(zipPluginManager.createPluginDescriptorFinder() instanceof PropertiesPluginDescriptorFinder);
    }

    @Test
    void createPluginLoaderReturnsCompoundPluginLoader() {
        ZipPluginManager zipPluginManager = new ZipPluginManager();
        PluginLoader pluginLoader = zipPluginManager.createPluginLoader();
        assertTrue(pluginLoader instanceof CompoundPluginLoader);
        CompoundPluginLoader compoundPluginLoader = (CompoundPluginLoader) pluginLoader;
        List<PluginLoader> loaders = compoundPluginLoader.getLoaders();
        if (zipPluginManager.isDevelopment()) {
            assertTrue(loaders.stream().anyMatch(loader -> loader instanceof DevelopmentPluginLoader));
        } else {
            assertTrue(loaders.stream().anyMatch(loader -> loader instanceof DefaultPluginLoader));
        }
    }

    @Test
    void createPluginRepositoryReturnsCompoundPluginRepository() {
        ZipPluginManager zipPluginManager = new ZipPluginManager();
        PluginRepository pluginRepository = zipPluginManager.createPluginRepository();
        assertTrue(pluginRepository instanceof CompoundPluginRepository);
        CompoundPluginRepository compoundPluginLoader = (CompoundPluginRepository) pluginRepository;
        List<PluginRepository> repositories = compoundPluginLoader.getRepositories();
        if (zipPluginManager.isDevelopment()) {
            assertTrue(repositories.stream().anyMatch(repo -> repo instanceof DevelopmentPluginRepository));
        } else {
            assertTrue(repositories.stream().anyMatch(repo -> repo instanceof DefaultPluginRepository));
        }
    }

}
