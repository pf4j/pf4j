/*
 * Copyright 2016 Decebal Suiu
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

import ro.fortsoft.pf4j.util.AndFileFilter;
import ro.fortsoft.pf4j.util.DirectoryFileFilter;
import ro.fortsoft.pf4j.util.HiddenFilter;
import ro.fortsoft.pf4j.util.JarFileFilter;
import ro.fortsoft.pf4j.util.NameFileFilter;
import ro.fortsoft.pf4j.util.NotFileFilter;
import ro.fortsoft.pf4j.util.OrFileFilter;

import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * It's a {@link PluginManager} that loads plugin from a jar file.
 * Actually, a plugin is a fat jar, a jar which contains classes from all the libraries,
 * on which your project depends and, of course, the classes of current project.
 *
 * @author Decebal Suiu
 */
public class JarPluginManager extends DefaultPluginManager {

    @Override
    protected PluginRepository createPluginRepository() {
        return new JarPluginRepository(getPluginsRoot(), isDevelopment());
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return isDevelopment() ? new PropertiesPluginDescriptorFinder() : new JarPluginDescriptorFinder();
    }

    @Override
    protected PluginLoader createPluginLoader() {
        return new JarPluginLoader(this, pluginClasspath);
    }

    class JarPluginRepository extends BasePluginRepository {

        public JarPluginRepository(Path pluginsRoot, boolean development) {
            super(pluginsRoot);

            if (development) {
                AndFileFilter pluginsFilter = new AndFileFilter(new DirectoryFileFilter());
                pluginsFilter.addFileFilter(new NotFileFilter(createHiddenPluginFilter(development)));
                setFilter(pluginsFilter);
            } else {
                setFilter(new JarFileFilter());
            }
        }

        protected FileFilter createHiddenPluginFilter(boolean development) {
            OrFileFilter hiddenPluginFilter = new OrFileFilter(new HiddenFilter());

            if (development) {
                hiddenPluginFilter.addFileFilter(new NameFileFilter("target"));
            }

            return hiddenPluginFilter;
        }

    }

    class JarPluginDescriptorFinder extends ManifestPluginDescriptorFinder {

        @Override
        public Manifest readManifest(Path pluginPath) throws PluginException {
            try {
                return new JarFile(pluginPath.toFile()).getManifest();
            } catch (IOException e) {
                throw new PluginException(e);
            }
        }

    }

    class JarPluginLoader extends DefaultPluginLoader {

        public JarPluginLoader(PluginManager pluginManager, PluginClasspath pluginClasspath) {
            super(pluginManager, pluginClasspath);
        }

        @Override
        public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
            if (isDevelopment()) {
                return super.loadPlugin(pluginPath, pluginDescriptor);
            }

            PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader());
            pluginClassLoader.addFile(pluginPath.toFile());

            return pluginClassLoader;
        }

    }

}
