/*
 * Copyright 2013 Decebal Suiu
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The default implementation for {@link PluginDescriptorFinder}.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginDescriptorFinder extends ManifestPluginDescriptorFinder {

    private PluginClasspath pluginClasspath;

    public DefaultPluginDescriptorFinder(PluginClasspath pluginClasspath) {
        this.pluginClasspath = pluginClasspath;
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath) && Files.isDirectory(pluginPath);
    }

    @Override
    protected Path getManifestPath(Path pluginPath) throws PluginException {
        // TODO it's ok with first classes root? Another idea is to specify in PluginClasspath the folder.
        if (pluginClasspath.getClassesDirectories().size() == 0) {
            throw new PluginException("Failed to read manifest, no classes folder in classpath");
        }
        String classes = pluginClasspath.getClassesDirectories().get(0);

        return pluginPath.resolve(Paths.get(classes,"/META-INF/MANIFEST.MF"));
    }

}
