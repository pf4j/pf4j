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
package ro.fortsoft.pf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Manifest;

/**
 * The default implementation for {@link PluginDescriptorFinder}.
 * Now, this class it's a "link" to {@link ManifestPluginDescriptorFinder}.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginDescriptorFinder extends ManifestPluginDescriptorFinder {

    private static final Logger log = LoggerFactory.getLogger(ManifestPluginDescriptorFinder.class);

    private PluginClasspath pluginClasspath;

    public DefaultPluginDescriptorFinder(PluginClasspath pluginClasspath) {
        this.pluginClasspath = pluginClasspath;
    }

	@Override
    public Manifest readManifest(Path pluginPath) throws PluginException {
        // TODO it's ok with first classes root? Another idea is to specify in PluginClasspath the folder.
        if (pluginClasspath.getClassesDirectories().size() == 0) {
            throw new PluginException("Failed to read manifest, no classes folder in classpath");
        }
        String classes = pluginClasspath.getClassesDirectories().get(0);
        Path manifestPath = pluginPath.resolve(Paths.get(classes,"/META-INF/MANIFEST.MF"));
        log.debug("Lookup plugin descriptor in '{}'", manifestPath);
        if (Files.notExists(manifestPath)) {
            throw new PluginException("Cannot find '" + manifestPath + "' path");
        }

        try (InputStream input = Files.newInputStream(manifestPath)) {
            return new Manifest(input);
        } catch (IOException e) {
            throw new PluginException(e.getMessage(), e);
        }
    }

}
