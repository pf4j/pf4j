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

import org.pf4j.processor.ExtensionStorage;
import org.pf4j.processor.LegacyExtensionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * All extensions declared in a plugin are indexed in a file {@code META-INF/extensions.idx}.
 * This class lookup extensions in all extensions index files {@code META-INF/extensions.idx}.
 *
 * @author Decebal Suiu
 */
public class LegacyExtensionFinder extends AbstractExtensionFinder {

    private static final Logger log = LoggerFactory.getLogger(LegacyExtensionFinder.class);

    public static final String EXTENSIONS_RESOURCE = LegacyExtensionStorage.EXTENSIONS_RESOURCE;

    public LegacyExtensionFinder(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    public Map<String, Set<String>> readClasspathStorages() {
        log.debug("Reading extensions storages from classpath");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        Set<String> bucket = new HashSet<>();
        try {
            Enumeration<URL> urls = getClass().getClassLoader().getResources(EXTENSIONS_RESOURCE);
            if (urls.hasMoreElements()) {
                collectExtensions(urls, bucket);
            } else {
                log.debug("Cannot find '{}'", EXTENSIONS_RESOURCE);
            }

            debugExtensions(bucket);

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
            log.debug("Reading extensions storage from plugin '{}'", pluginId);
            Set<String> bucket = new HashSet<>();

            try {
                log.debug("Read '{}'", EXTENSIONS_RESOURCE);
                ClassLoader pluginClassLoader = plugin.getPluginClassLoader();
                List<URL> extensionFiles = Collections.list(pluginClassLoader.getResources(EXTENSIONS_RESOURCE));
                URL targetFile = null;
                
                if (extensionFiles.size() == 1) {
                    targetFile = extensionFiles.get(0);
                } else if (extensionFiles.size() > 1) {
                    targetFile = extensionFiles.get(0);
                    //Detect the correct extension file. in the past, getResourceAsStream was used but this fails
                    //if ClassLoadingStrategy.APD is used and the application also contains a META-INF/extensions.idx file
                    Path pluginPath = plugin.getPluginPath();
                    for (URL url : extensionFiles) {
                        URI extensionUri = "jar".equals(url.getProtocol()) ? new URI(url.getFile()) : url.toURI();
                        Path extensionPath = java.nio.file.Paths.get(extensionUri);
                        if (extensionPath.toString().startsWith(pluginPath.toString())) {
                            targetFile = url;
                            break;
                        }
                    }
                    
                }

                if (targetFile != null) {pluginClassLoader.getResourceAsStream(EXTENSIONS_RESOURCE);
                    URLConnection urlc = targetFile.openConnection();
                    try (InputStream resourceStream = urlc.getInputStream()) {
                        if (resourceStream == null) {
                            log.debug("Cannot find '{}'", EXTENSIONS_RESOURCE);
                        } else {
                            collectExtensions(resourceStream, bucket);
                        }
                    } finally {
                        //close jar file of jar url connection - otherwise the file cannot be deleted
                        if (urlc instanceof JarURLConnection) {
                            JarURLConnection juc = (JarURLConnection)urlc;
                            JarFile jar = juc.getJarFile();
                            jar.close();
                        }
                    }
                }

                debugExtensions(bucket);

                result.put(pluginId, bucket);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } catch (URISyntaxException e) {
                log.error(e.getMessage(), e);
            }
        }

        return result;
    }

    private void collectExtensions(Enumeration<URL> urls, Set<String> bucket) throws IOException {
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            log.debug("Read '{}'", url.getFile());
            collectExtensions(url.openStream(), bucket);
        }
    }

    private void collectExtensions(InputStream inputStream, Set<String> bucket) throws IOException {
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            ExtensionStorage.read(reader, bucket);
        }
    }

}
