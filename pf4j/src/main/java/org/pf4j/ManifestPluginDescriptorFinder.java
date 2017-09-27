/*
 * Copyright 2012 Decebal Suiu
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

import org.pf4j.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Read the plugin descriptor from the manifest file.
 *
 * @author Decebal Suiu
 */
public abstract class ManifestPluginDescriptorFinder implements PluginDescriptorFinder {

    private static final Logger log = LoggerFactory.getLogger(ManifestPluginDescriptorFinder.class);

    @Override
	public PluginDescriptor find(Path pluginPath) throws PluginException {
        Manifest manifest = readManifest(pluginPath);

        return createPluginDescriptor(manifest);
	}

    protected Manifest readManifest(Path pluginPath) throws PluginException {
        Path manifestPath = getManifestPath(pluginPath);
        log.debug("Lookup plugin descriptor in '{}'", manifestPath);
        if (Files.notExists(manifestPath)) {
            throw new PluginException("Cannot find '{}' path", manifestPath);
        }

        try (InputStream input = Files.newInputStream(manifestPath)) {
            return new Manifest(input);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    protected abstract Path getManifestPath(Path pluginPath) throws PluginException;

    protected PluginDescriptor createPluginDescriptor(Manifest manifest) {
        PluginDescriptor pluginDescriptor = createPluginDescriptorInstance();

        // TODO validate !!!
        Attributes attributes = manifest.getMainAttributes();
        String id = attributes.getValue("Plugin-Id");
        pluginDescriptor.setPluginId(id);

        String description = attributes.getValue("Plugin-Description");
        if (StringUtils.isEmpty(description)) {
            pluginDescriptor.setPluginDescription("");
        } else {
            pluginDescriptor.setPluginDescription(description);
        }

        String clazz = attributes.getValue("Plugin-Class");
        pluginDescriptor.setPluginClass(clazz);

        String version = attributes.getValue("Plugin-Version");
        if (StringUtils.isNotEmpty(version)) {
            pluginDescriptor.setPluginVersion(version);
        }

        String provider = attributes.getValue("Plugin-Provider");
        pluginDescriptor.setProvider(provider);
        String dependencies = attributes.getValue("Plugin-Dependencies");
        pluginDescriptor.setDependencies(dependencies);

        String requires = attributes.getValue("Plugin-Requires");
        if (StringUtils.isNotEmpty(requires)) {
            pluginDescriptor.setRequires(requires);
        }

        pluginDescriptor.setLicense(attributes.getValue("Plugin-License"));

        return pluginDescriptor;
    }

    protected PluginDescriptor createPluginDescriptorInstance() {
        return new PluginDescriptor();
    }

}
