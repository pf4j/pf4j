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
import ro.fortsoft.pf4j.processor.LegacyExtensionStorage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * All extensions declared in a plugin are indexed in a file "META-INF/extensions.idx".
 * This class lookup extensions in all extensions index files "META-INF/extensions.idx".
 *
 * @author Decebal Suiu
 */
public class LegacyExtensionFinder extends AbstractExtensionFinder {

	private static final Logger log = LoggerFactory.getLogger(LegacyExtensionFinder.class);

	public LegacyExtensionFinder(PluginManager pluginManager) {
        super(pluginManager);
	}

    @Override
    public Map<String, Set<String>> readClasspathStorages() {
        log.debug("Reading extensions storages from classpath");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        Set<String> bucket = new HashSet<>();
        try {
            Enumeration<URL> urls = getClass().getClassLoader().getResources(getExtensionsResource());
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                log.debug("Read '{}'", url.getFile());
                Reader reader = new InputStreamReader(url.openStream(), "UTF-8");
                LegacyExtensionStorage.read(reader, bucket);
            }

            if (bucket.isEmpty()) {
                log.debug("No extensions found");
            } else {
                log.debug("Found possible {} extensions:", bucket.size());
                for (String entry : bucket) {
                    log.debug("   " + entry);
                }
            }

            result.put(null, bucket);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Map<String, Set<String>> readPluginsStorages() {
        log.debug("Reading extensions storages from plugins");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        List<PluginWrapper> plugins = pluginManager.getPlugins();
        for (PluginWrapper plugin : plugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            log.debug("Reading extensions storage for plugin '{}'", pluginId);
            Set<String> bucket = new HashSet<>();

            try {
                URL url = ((PluginClassLoader) plugin.getPluginClassLoader()).findResource(getExtensionsResource());
                if (url != null) {
                    log.debug("Read '{}'", url.getFile());
                    Reader reader = new InputStreamReader(url.openStream(), "UTF-8");
                    LegacyExtensionStorage.read(reader, bucket);
                } else {
                    log.debug("Cannot find '{}'", getExtensionsResource());
                }

                if (bucket.isEmpty()) {
                    log.debug("No extensions found");
                } else {
                    log.debug("Found possible {} extensions:", bucket.size());
                    for (String entry : bucket) {
                        log.debug("   " + entry);
                    }
                }

                result.put(pluginId, bucket);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        return result;
    }

    private static String getExtensionsResource() {
        return LegacyExtensionStorage.EXTENSIONS_RESOURCE;
    }

}
