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
 * All extensions declared in a plug-in are indexed in a file "META-INF/extensions.idx".
 * This class lookup extensions in all extensions index files "META-INF/extensions.idx".
 * 
 * @author Decebal Suiu
 */
public class DefaultExtensionFinder implements ExtensionFinder, PluginStateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExtensionFinder.class);
	private static final String UTF8 = "UTF-8";

	private PluginManager pluginManager;
	private ExtensionFactory extensionFactory;
	private volatile Map<String, Set<String>> entries; // cache by pluginId

	/**
	 * Constructor to set PluginManager and ExtensionFactory.
	 * 
	 * @param pluginManager
	 *          PluginManager that provides functionalities for plug-in management.
	 * @param extensionFactory
	 *          ExtensionFactory that creates an instance of extension object.
	 */
	public DefaultExtensionFinder(PluginManager pluginManager, ExtensionFactory extensionFactory) {
		this.pluginManager = pluginManager;
		this.extensionFactory = extensionFactory;
	}

	@Override
	public <T> List<ExtensionWrapper<T>> find(Class<T> extensionPoint) {
		// The name of the extension point.
		String extensionPointName = extensionPoint.getName();

		LOGGER.debug("Checking extension point '{}'", extensionPointName);
		if (!isExtensionPoint(extensionPoint)) {
			LOGGER.warn("'{}' is not an extension point", extensionPointName);
			return Collections.emptyList(); // TODO : decide to return emptyList or null.
		}

		LOGGER.debug("Finding extensions for extension point '{}'", extensionPointName);
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
					ClassLoader classLoader;
					if (pluginId != null) {
						classLoader = pluginManager.getPluginClassLoader(pluginId);
					} else {
						classLoader = getClass().getClassLoader();
					}

					LOGGER.debug("Loading class '{}' using class loader '{}'", className, classLoader);
					Class<?> extensionClass = classLoader.loadClass(className);

					LOGGER.debug("Checking extension type '{}'", className);
					if (extensionPoint.isAssignableFrom(extensionClass) && extensionClass.isAnnotationPresent(Extension.class)) {
						Extension extension = extensionClass.getAnnotation(Extension.class);
						ExtensionDescriptor descriptor = new ExtensionDescriptor();
						descriptor.setOrdinal(extension.ordinal());
						descriptor.setExtensionClass(extensionClass);

						ExtensionWrapper<T> extensionWrapper = new ExtensionWrapper<T>(descriptor);
						extensionWrapper.setExtensionFactory(extensionFactory);
						result.add(extensionWrapper);
						LOGGER.debug("Added extension '{}' with ordinal {}", className, extension.ordinal());
					} else {
						LOGGER.debug("'{}' is not an extension for extension point '{}'", className, extensionPointName);
					}
				} catch (ClassNotFoundException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		if (entries.isEmpty()) {
			LOGGER.debug("No extensions found for extension point '{}'", extensionPointName);
		} else {
			LOGGER.debug("Found {} extensions for extension point '{}'", entries.size(), extensionPointName);
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
	 * Private helper method to find extension(s) for an extension point.
	 * 
	 * @return The cache Map with plug-in id as a key.
	 */
	private Map<String, Set<String>> readIndexFiles() {
		LOGGER.debug("Checking cache. Return the cache if it is not null.");
		if (entries != null) {
			return entries;
		}

		LOGGER.debug("Cache is null. Create a new LinkedHashMap.");
		entries = new LinkedHashMap<String, Set<String>>();

		readClasspathIndexFiles();
		readPluginsIndexFiles();

		return entries;
	}

	/**
	 * Private helper method to read the classpath index file (META-INF/extensions.idx).
	 */
	private void readClasspathIndexFiles() {
		LOGGER.debug("Reading extensions index files from classpath");
		Set<String> bucket = new HashSet<String>();

		try {
			Enumeration<URL> urls = getClass().getClassLoader().getResources(ExtensionsIndexer.EXTENSIONS_RESOURCE);
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				LOGGER.debug("Read '{}'", url.getFile());
				Reader reader = new InputStreamReader(url.openStream(), UTF8);
				ExtensionsIndexer.readIndex(reader, bucket);
			}

			if (bucket.isEmpty()) {
				LOGGER.debug("No extensions found");
			} else {
				LOGGER.debug("Found possible {} extensions:", bucket.size());
				for (String entry : bucket) {
					LOGGER.debug("   " + entry);
				}
			}

			entries.put(null, bucket);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void readPluginsIndexFiles() {
		LOGGER.debug("Reading extensions index files from plugins");

		List<PluginWrapper> plugins = pluginManager.getPlugins();
		for (PluginWrapper plugin : plugins) {
			String pluginId = plugin.getDescriptor().getPluginId();
			LOGGER.debug("Reading extensions index file for plugin '{}'", pluginId);
			Set<String> bucket = new HashSet<String>();

			try {
				URL url = plugin.getPluginClassLoader().getResource(ExtensionsIndexer.EXTENSIONS_RESOURCE);
				if (url != null) {
					LOGGER.debug("Read '{}'", url.getFile());
					Reader reader = new InputStreamReader(url.openStream(), "UTF-8");
					ExtensionsIndexer.readIndex(reader, bucket);
				} else {
					LOGGER.debug("Cannot find '{}'", ExtensionsIndexer.EXTENSIONS_RESOURCE);
				}

				if (bucket.isEmpty()) {
					LOGGER.debug("No extensions found");
				} else {
					LOGGER.debug("Found possible {} extensions:", bucket.size());
					for (String entry : bucket) {
						LOGGER.debug("   " + entry);
					}
				}

				entries.put(pluginId, bucket);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Private helper method to determine if the class or interface represented by this Class object is either the same
	 * as, or is a superclass or superinterface of, the class or interface represented by the specified Class parameter.
	 * 
	 * @param extensionPoint
	 *          Extension point class.
	 * @return True if same, false otherwise.
	 */
	private boolean isExtensionPoint(Class<?> extensionPoint) {
		return ExtensionPoint.class.isAssignableFrom(extensionPoint);
	}
}
