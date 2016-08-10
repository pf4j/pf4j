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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Find a plugin descriptor in a properties file (in plugin repository).
 *
 * @author Decebal Suiu
 */
public class PropertiesPluginDescriptorFinder implements PluginDescriptorFinder {

	private static final Logger log = LoggerFactory.getLogger(PropertiesPluginDescriptorFinder.class);

	private static final String DEFAULT_PROPERTIES_FILE_NAME = "plugin.properties";

	private String propertiesFileName;

	public PropertiesPluginDescriptorFinder() {
		this(DEFAULT_PROPERTIES_FILE_NAME);
	}

	public PropertiesPluginDescriptorFinder(String propertiesFileName) {
		this.propertiesFileName = propertiesFileName;
	}

	@Override
	public PluginDescriptor find(File pluginRepository) throws PluginException {
        Properties properties = readProperties(pluginRepository);

        PluginDescriptor pluginDescriptor = createPluginDescriptor(properties);
        validatePluginDescriptor(pluginDescriptor);

        return pluginDescriptor;
	}

    protected Properties readProperties(File pluginRepository) throws PluginException {
        File propertiesFile = new File(pluginRepository, propertiesFileName);
        log.debug("Lookup plugin descriptor in '{}'", propertiesFile);
        if (!propertiesFile.exists()) {
            throw new PluginException("Cannot find '" + propertiesFile + "' file");
        }

        InputStream input = null;
        try {
            input = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            // not happening
        }

        Properties properties = new Properties();
        try {
            properties.load(input);
        } catch (IOException e) {
            throw new PluginException(e.getMessage(), e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                throw new PluginException(e.getMessage(), e);
            }
        }

        return properties;
    }

    protected PluginDescriptor createPluginDescriptor(Properties properties) {
        PluginDescriptor pluginDescriptor = createPluginDescriptorInstance();

        // TODO validate !!!
        String id = properties.getProperty("plugin.id");
        pluginDescriptor.setPluginId(id);

        String clazz = properties.getProperty("plugin.class");
        pluginDescriptor.setPluginClass(clazz);

        String version = properties.getProperty("plugin.version");
        if (StringUtils.isNotEmpty(version)) {
            pluginDescriptor.setPluginVersion(Version.valueOf(version));
        }

        String provider = properties.getProperty("plugin.provider");
        pluginDescriptor.setProvider(provider);

        String dependencies = properties.getProperty("plugin.dependencies");
        pluginDescriptor.setDependencies(dependencies);

        return pluginDescriptor;
    }

    protected PluginDescriptor createPluginDescriptorInstance() {
        return new PluginDescriptor();
    }

    protected void validatePluginDescriptor(PluginDescriptor pluginDescriptor) throws PluginException {
        if (StringUtils.isEmpty(pluginDescriptor.getPluginId())) {
            throw new PluginException("plugin.id cannot be empty");
        }
        if (StringUtils.isEmpty(pluginDescriptor.getPluginClass())) {
            throw new PluginException("plugin.class cannot be empty");
        }
        if (pluginDescriptor.getVersion() == null) {
            throw new PluginException("plugin.version cannot be empty");
        }
    }

}
