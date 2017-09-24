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

import org.pf4j.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pf4j.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Properties;

/**
 * Find a plugin descriptor in a properties file (in plugin repository).
 *
 * @author Decebal Suiu
 */
public class PropertiesPluginDescriptorFinder implements PluginDescriptorFinder {

	private static final Logger log = LoggerFactory.getLogger(PropertiesPluginDescriptorFinder.class);

	private static final String DEFAULT_PROPERTIES_FILE_NAME = "plugin.properties";

	protected String propertiesFileName;

	public PropertiesPluginDescriptorFinder() {
		this(DEFAULT_PROPERTIES_FILE_NAME);
	}

	public PropertiesPluginDescriptorFinder(String propertiesFileName) {
        this.propertiesFileName = propertiesFileName;
	}

	@Override
	public PluginDescriptor find(Path pluginPath) throws PluginException {
        Properties properties = readProperties(pluginPath);

        return createPluginDescriptor(properties);
	}

    protected Properties readProperties(Path pluginPath) throws PluginException {
        FileSystem fileSystem = null;
        Path propertiesPath = pluginPath.resolve(Paths.get(propertiesFileName));
        log.debug("Lookup plugin descriptor in '{}'", propertiesPath);
        try {
            // NOTE: as of JDK 8, Files.notExists() doesn't work here if properties file is inside .zip or .jar
            // For files inside .zip: Files.notExists() thinks the file exists, but Files.newInputStream() doesn't agree with it
            // So, to circumvent this, we open a FileSystem and close it at the end
            if (!Files.exists(propertiesPath)) {
                if (pluginPath.endsWith(".zip") || pluginPath.endsWith(".jar")) {
                    fileSystem = FileSystems.newFileSystem(pluginPath, this.getClass().getClassLoader());
                    propertiesPath = fileSystem.getPath(propertiesFileName);
                }
                if (!Files.exists(propertiesPath)) {
                    throw new PluginException("Cannot find '{}' path", pluginPath);
                }
            }
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(propertiesPath)) {
                properties.load(input);
            }
            return properties;
        } catch (IOException e) {
            throw new PluginException(e.getMessage(), e);
        } finally {
            FileUtils.closeQuietly(fileSystem);
        }
    }

    protected PluginDescriptor createPluginDescriptor(Properties properties) {
        PluginDescriptor pluginDescriptor = createPluginDescriptorInstance();

        // TODO validate !!!
        String id = properties.getProperty("plugin.id");
        pluginDescriptor.setPluginId(id);

        String description = properties.getProperty("plugin.description");
        if (StringUtils.isEmpty(description)) {
            pluginDescriptor.setPluginDescription("");
        } else {
            pluginDescriptor.setPluginDescription(description);
        }

        String clazz = properties.getProperty("plugin.class");
        pluginDescriptor.setPluginClass(clazz);

        String version = properties.getProperty("plugin.version");
        if (StringUtils.isNotEmpty(version)) {
            pluginDescriptor.setPluginVersion(version);
        }

        String provider = properties.getProperty("plugin.provider");
        pluginDescriptor.setProvider(provider);

        String dependencies = properties.getProperty("plugin.dependencies");
        pluginDescriptor.setDependencies(dependencies);

        String requires = properties.getProperty("plugin.requires");
        if (StringUtils.isNotEmpty(requires)) {
            pluginDescriptor.setRequires(requires);
        }

        pluginDescriptor.setLicense(properties.getProperty("plugin.license"));

        return pluginDescriptor;
    }

    protected PluginDescriptor createPluginDescriptorInstance() {
        return new PluginDescriptor();
    }

}
