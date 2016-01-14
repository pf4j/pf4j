/*
 * Copyright 2015 Decebal Suiu
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取拓展
 * @author Decebal Suiu
 */
public abstract class AbstractExtensionFinder
    implements ExtensionFinder, PluginStateListener {

  protected static final Logger log = LoggerFactory
      .getLogger(AbstractExtensionFinder.class);

  protected PluginManager pluginManager;

  protected volatile Map<String, Set<String>> entries; // cache by pluginId

  public AbstractExtensionFinder(PluginManager pluginManager) {
    this.pluginManager = pluginManager;
  }

  public abstract Map<String, Set<String>> readPluginsStorages();

  public abstract Map<String, Set<String>> readClasspathStorages();

  @Override
  public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
    log.debug("Finding extensions for extension point '{}'", type.getName());
    Map<String, Set<String>> entries = getEntries();

    List<ExtensionWrapper<T>> result = new ArrayList<>();
    for (Map.Entry<String, Set<String>> entry : entries.entrySet()) {
      String pluginId = entry.getKey();

      if (pluginId != null) {
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
        if (PluginState.STARTED != pluginWrapper.getPluginState()) {
          continue;
        }
      }

      for (String className : entry.getValue()) {
        try {
          ClassLoader classLoader;
          if (pluginId != null) {
            classLoader = pluginManager.getPluginClassLoader(pluginId);
          }
          else {
            classLoader = getClass().getClassLoader();
          }
          log.debug("Loading class '{}' using class loader '{}'", className,
              classLoader);
          Class<?> extensionClass = classLoader.loadClass(className);

          log.debug("Checking extension type '{}'", className);
          if (type.isAssignableFrom(extensionClass)) {
            ExtensionDescriptor descriptor = new ExtensionDescriptor();
            int ordinal = 0;
            if (extensionClass.isAnnotationPresent(Extension.class)) {
              ordinal = extensionClass.getAnnotation(Extension.class).ordinal();
            }
            descriptor.setOrdinal(ordinal);
            descriptor.setExtensionClass(extensionClass);

            ExtensionWrapper extensionWrapper = new ExtensionWrapper<>(
                descriptor);
            extensionWrapper
                .setExtensionFactory(pluginManager.getExtensionFactory());
            result.add(extensionWrapper);
            log.debug("Added extension '{}' with ordinal {}", className,
                ordinal);
          }
          else {
            log.debug("'{}' is not an extension for extension point '{}'",
                className, type.getName());
          }
        }
        catch (ClassNotFoundException e) {
          log.error(e.getMessage(), e);
        }
      }
    }

    if (entries.isEmpty()) {
      log.debug("No extensions found for extension point '{}'", type.getName());
    }
    else {
      log.debug("Found {} extensions for extension point '{}'", result.size(),
          type.getName());
    }

    // sort by "ordinal" property
    Collections.sort(result);

    return result;
  }

  @Override
  public Set<String> findClassNames(String pluginId) {
    return getEntries().get(pluginId);
  }

  @Override
  public void pluginStateChanged(PluginStateEvent event) {
    // TODO optimize (do only for some transitions)
    // clear cache
    entries = null;
  }

  private Map<String, Set<String>> readStorages() {
    Map<String, Set<String>> result = new LinkedHashMap<>();

    result.putAll(readClasspathStorages());
    result.putAll(readPluginsStorages());

    return result;
  }

  private Map<String, Set<String>> getEntries() {
    if (entries == null) {
      entries = readStorages();
    }

    return entries;
  }

}
