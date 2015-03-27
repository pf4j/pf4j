/*
 * Copyright 2012 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.util.StringUtils;

import java.io.*;
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
        private PluginManager pluginManager;

	public PropertiesPluginDescriptorFinder(PluginManager pluginManager) {
		this(DEFAULT_PROPERTIES_FILE_NAME, pluginManager);
	}

	public PropertiesPluginDescriptorFinder(String propertiesFileName, PluginManager pluginManager) {
		this.propertiesFileName = propertiesFileName;
            this.pluginManager = pluginManager;
	}

	@Override
	public PluginDescriptor find(File pluginRepository) throws PluginException {
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

        PluginDescriptor pluginDescriptor = new PluginDescriptor(pluginManager);

        // TODO validate !!!
        String id = properties.getProperty("plugin.id");
        if (StringUtils.isEmpty(id)) {
        	throw new PluginException("plugin.id cannot be empty");
        }
        pluginDescriptor.setPluginId(id);

        String clazz = properties.getProperty("plugin.class");
        if (StringUtils.isEmpty(clazz)) {
        	throw new PluginException("plugin.class cannot be empty");
        }
        pluginDescriptor.setPluginClass(clazz);

        String version = properties.getProperty("plugin.version");
        if (StringUtils.isEmpty(version)) {
        	throw new PluginException("plugin.version cannot be empty");
        }
        pluginDescriptor.setPluginVersion(Version.createVersion(version));

        String provider = properties.getProperty("plugin.provider");
        pluginDescriptor.setProvider(provider);
        String dependencies = properties.getProperty("plugin.dependencies");
        pluginDescriptor.setDependencies(dependencies);

		return pluginDescriptor;
	}

}
