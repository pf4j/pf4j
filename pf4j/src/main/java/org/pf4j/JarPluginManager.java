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

/**
 * It's a {@link PluginManager} that loads each plugin from a {@code jar} file.
 * Actually, a plugin is a fat jar, a jar which contains classes from all the libraries,
 * on which your project depends and, of course, the classes of current project.
 *
 * @author Decebal Suiu
 */
public class JarPluginManager extends DefaultPluginManager {

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new ManifestPluginDescriptorFinder();
    }

    @Override
    protected PluginLoader createPluginLoader() {
        return new JarPluginLoader(this);
    }

    @Override
    protected PluginRepository createPluginRepository() {
        return new JarPluginRepository(getPluginsRoot());
    }

}
