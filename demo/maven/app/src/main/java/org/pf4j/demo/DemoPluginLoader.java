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
package org.pf4j.demo;

import org.pf4j.DefaultPluginLoader;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.JarPluginLoader;
import org.pf4j.CompoundPluginLoader;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

import java.nio.file.Path;

/**
 * Custom PluginLoader that uses {@link DemoPluginClassLoader} for loading plugins.
 *
 * @author Decebal Suiu
 */
class DemoPluginLoader extends CompoundPluginLoader {

    public DemoPluginLoader(PluginManager pluginManager) {
        add(new DevelopmentPluginLoader(pluginManager) {
            @Override
            protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
                return new DemoPluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader());
            }
        }, pluginManager::isDevelopment);
        add(new JarPluginLoader(pluginManager) {
            @Override
            protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
                return new DemoPluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader());
            }
        }, pluginManager::isNotDevelopment);
        add(new DefaultPluginLoader(pluginManager) {
            @Override
            protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
                return new DemoPluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader());
            }
        }, pluginManager::isNotDevelopment);
    }

}
