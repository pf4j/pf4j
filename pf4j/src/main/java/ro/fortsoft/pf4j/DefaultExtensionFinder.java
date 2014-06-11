/*
 * Copyright 2013 Decebal Suiu
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation for ExtensionFinder.
 * All extensions declared in a plugin are indexed in a file "META-INF/extensions.idx".
 * This class lookup extensions in all extensions index files "META-INF/extensions.idx".
 *
 * @author Decebal Suiu
 */
public class DefaultExtensionFinder implements ExtensionFinder, PluginStateListener {

	private static final Logger log = LoggerFactory.getLogger(DefaultExtensionFinder.class);

    private PluginManager pluginManager;
	private ExtensionFactory extensionFactory;
    private volatile Map<String, Set<String>> entries; // cache by pluginId

	public DefaultExtensionFinder(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
		this.extensionFactory = createExtensionFactory();
	}

    @Override
	public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
        log.debug("Checking extension point '{}'", type.getName());
        if (!isExtensionPoint(type)) {
            log.warn("'{}' is not an extension point", type.getName());

            return Collections.emptyList(); // or return null ?!
        }

		log.debug("Finding extensions for extension point '{}'", type.getName());
        readIndexFiles();

        List<ExtensionWrapper<T>> result = new ArrayList<ExtensionWrapper<T>>();
        for (Map.Entry<String, Set<String>> entry : entries.entrySet()) {
            String pluginId = entry.getKey();

            if (pluginId != null) {
                PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
                if (PluginState.STARTED != pluginWrapper.getPluginState()) {
                    continue;
                }
            }

            Set<String> extensionClassNames = entry.getValue();

            for (String className : extensionClassNames) {
                try {
                    Class<?> extensionType;
                    if (pluginId != null) {
                        extensionType = pluginManager.getPluginClassLoader(pluginId).loadClass(className);
                    } else {
                        extensionType = getClass().getClassLoader().loadClass(className);
                    }

                    log.debug("Checking extension type '{}'", extensionType.getName());
                    if (type.isAssignableFrom(extensionType)) {
                        Object instance = extensionFactory.create(extensionType);
                        if (instance != null) {
                            Extension extension = extensionType.getAnnotation(Extension.class);
                            log.debug("Added extension '{}' with ordinal {}", extensionType.getName(), extension.ordinal());
                            result.add(new ExtensionWrapper<T>(type.cast(instance), extension.ordinal()));
                        }
                    } else {
                        log.debug("'{}' is not an extension for extension point '{}'", extensionType.getName(), type.getName());
                    }
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        if (entries.isEmpty()) {
        	log.debug("No extensions found for extension point '{}'", type.getName());
        } else {
        	log.debug("Found {} extensions for extension point '{}'", entries.size(), type.getName());
        }

        // sort by "ordinal" property
        Collections.sort(result);

		return result;
	}

    @Override
    public Set<String> findClassNames(String pluginId) {
    	readIndexFiles();
        return entries.get(pluginId);
    }

    @Override
	public void pluginStateChanged(PluginStateEvent event) {
        // TODO optimize (do only for some transitions)
        // clear cache
        entries = null;
    }

    /**
     * Add the possibility to override the ExtensionFactory.
     * The default implementation uses Class.newInstance() method.
     */
	protected ExtensionFactory createExtensionFactory() {
		return new ExtensionFactory() {

			@Override
			public Object create(Class<?> extensionType) {
				log.debug("Create instance for extension '{}'", extensionType.getName());

				try {
					return extensionType.newInstance();
				} catch (InstantiationException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				}

				return null;
			}

		};
	}

    private Map<String, Set<String>> readIndexFiles() {
        // checking cache
        if (entries != null) {
            return entries;
        }

        entries = new LinkedHashMap<String, Set<String>>();

        readClasspathIndexFiles();
        readPluginsIndexFiles();

        return entries;
    }

    private void readClasspathIndexFiles() {
        log.debug("Reading extensions index files from classpath");

        Set<String> bucket = new HashSet<String>();
        try {
            Enumeration<URL> urls = getClass().getClassLoader().getResources(ExtensionsIndexer.EXTENSIONS_RESOURCE);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                log.debug("Read '{}'", url.getFile());
                Reader reader = new InputStreamReader(url.openStream(), "UTF-8");
                ExtensionsIndexer.readIndex(reader, bucket);
            }

            if (bucket.isEmpty()) {
                log.debug("No extensions found");
            } else {
                log.debug("Found possible {} extensions:", bucket.size());
                for (String entry : bucket) {
                    log.debug("   " + entry);
                }
            }

            entries.put(null, bucket);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void readPluginsIndexFiles() {
        log.debug("Reading extensions index files from plugins");

        List<PluginWrapper> plugins = pluginManager.getPlugins();
        for (PluginWrapper plugin : plugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            log.debug("Reading extensions index file for plugin '{}'", pluginId);
            Set<String> bucket = new HashSet<String>();

            try {
                URL url = plugin.getPluginClassLoader().getResource(ExtensionsIndexer.EXTENSIONS_RESOURCE);
                log.debug("Read '{}'", url.getFile());
                Reader reader = new InputStreamReader(url.openStream(), "UTF-8");
                ExtensionsIndexer.readIndex(reader, bucket);

                if (bucket.isEmpty()) {
                    log.debug("No extensions found");
                } else {
                    log.debug("Found possible {} extensions:", bucket.size());
                    for (String entry : bucket) {
                        log.debug("   " + entry);
                    }
                }

                entries.put(pluginId, bucket);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private boolean isExtensionPoint(Class<?> type) {
        return ExtensionPoint.class.isAssignableFrom(type);
    }

	/**
	 * Creates an extension instance.
	 */
	public static interface ExtensionFactory {

		public Object create(Class<?> extensionType);

	}

}
