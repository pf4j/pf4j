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
package ro.fortsoft.pf4j;

import com.github.zafarkhaja.semver.Version;
import ro.fortsoft.pf4j.util.StringUtils;

import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Read the plugin descriptor from the manifest file.
 *
 * @author Decebal Suiu
 */
public abstract class ManifestPluginDescriptorFinder extends AbstractPluginDescriptorFinder {

	@Override
	public PluginDescriptor find(Path pluginPath) throws PluginException {
        Manifest manifest = readManifest(pluginPath);

        PluginDescriptor pluginDescriptor = createPluginDescriptor(manifest);
        validate(pluginDescriptor);

		return pluginDescriptor;
	}

    public abstract Manifest readManifest(Path pluginPath) throws PluginException;

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
            pluginDescriptor.setPluginVersion(Version.valueOf(version));
        }

        String provider = attributes.getValue("Plugin-Provider");
        pluginDescriptor.setProvider(provider);
        String dependencies = attributes.getValue("Plugin-Dependencies");
        pluginDescriptor.setDependencies(dependencies);

        String requires = attributes.getValue("Plugin-Requires");
        if (StringUtils.isNotEmpty(requires)) {
            pluginDescriptor.setRequires(requires);
        }

        return pluginDescriptor;
    }

    protected PluginDescriptor createPluginDescriptorInstance() {
        return new PluginDescriptor();
    }
}
