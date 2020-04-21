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

import org.pf4j.asm.ExtensionInfo;
import org.pf4j.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public abstract class AbstractExtensionFinder implements ExtensionFinder, PluginStateListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractExtensionFinder.class);

    protected PluginManager pluginManager;
    protected volatile Map<String, Set<String>> entries; // cache by pluginId
    protected volatile Map<String, ExtensionInfo> extensionInfos; // cache extension infos by class name
    protected Boolean checkForExtensionDependencies = null;

    public AbstractExtensionFinder(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public abstract Map<String, Set<String>> readPluginsStorages();

    public abstract Map<String, Set<String>> readClasspathStorages();

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
        log.debug("Finding extensions of extension point '{}'", type.getName());
        Map<String, Set<String>> entries = getEntries();
        List<ExtensionWrapper<T>> result = new ArrayList<>();

        // add extensions found in classpath and plugins
        for (String pluginId : entries.keySet()) {
            // classpath's extensions <=> pluginId = null
            List<ExtensionWrapper<T>> pluginExtensions = find(type, pluginId);
            result.addAll(pluginExtensions);
        }

        if (entries.isEmpty()) {
            log.debug("No extensions found for extension point '{}'", type.getName());
        } else {
            log.debug("Found {} extensions for extension point '{}'", result.size(), type.getName());
        }

        // sort by "ordinal" property
        Collections.sort(result);

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ExtensionWrapper<T>> find(Class<T> type, String pluginId) {
        log.debug("Finding extensions of extension point '{}' for plugin '{}'", type.getName(), pluginId);
        List<ExtensionWrapper<T>> result = new ArrayList<>();

        // classpath's extensions <=> pluginId = null
        Set<String> classNames = findClassNames(pluginId);
        if (classNames == null || classNames.isEmpty()) {
            return result;
        }

        if (pluginId != null) {
            PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
            if (PluginState.STARTED != pluginWrapper.getPluginState()) {
                return result;
            }

            log.trace("Checking extensions from plugin '{}'", pluginId);
        } else {
            log.trace("Checking extensions from classpath");
        }

        ClassLoader classLoader = (pluginId != null) ? pluginManager.getPluginClassLoader(pluginId) : getClass().getClassLoader();

        for (String className : classNames) {
            try {
                if (isCheckForExtensionDependencies()) {
                    // Load extension annotation without initializing the class itself.
                    //
                    // If optional dependencies are used, the class loader might not be able
                    // to load the extension class because of missing optional dependencies.
                    //
                    // Therefore we're extracting the extension annotation via asm, in order
                    // to extract the required plugins for an extension. Only if all required
                    // plugins are currently available and started, the corresponding
                    // extension is loaded through the class loader.
                    ExtensionInfo extensionInfo = getExtensionInfo(className, classLoader);
                    if (extensionInfo == null) {
                        log.error("No extension annotation was found for '{}'", className);
                        continue;
                    }

                    // Make sure, that all plugins required by this extension are available.
                    List<String> missingPluginIds = new ArrayList<>();
                    for (String requiredPluginId : extensionInfo.getPlugins()) {
                        PluginWrapper requiredPlugin = pluginManager.getPlugin(requiredPluginId);
                        if (requiredPlugin == null || !PluginState.STARTED.equals(requiredPlugin.getPluginState())) {
                            missingPluginIds.add(requiredPluginId);
                        }
                    }
                    if (!missingPluginIds.isEmpty()) {
                        StringBuilder missing = new StringBuilder();
                        for (String missingPluginId : missingPluginIds) {
                            if (missing.length() > 0) missing.append(", ");
                            missing.append(missingPluginId);
                        }
                        log.trace("Extension '{}' is ignored due to missing plugins: {}", className, missing);
                        continue;
                    }
                }

                log.debug("Loading class '{}' using class loader '{}'", className, classLoader);
                Class<?> extensionClass = classLoader.loadClass(className);

                log.debug("Checking extension type '{}'", className);
                if (type.isAssignableFrom(extensionClass)) {
                    ExtensionWrapper extensionWrapper = createExtensionWrapper(extensionClass);
                    result.add(extensionWrapper);
                    log.debug("Added extension '{}' with ordinal {}", className, extensionWrapper.getOrdinal());
                } else {
                    log.trace("'{}' is not an extension for extension point '{}'", className, type.getName());
                    if (RuntimeMode.DEVELOPMENT.equals(pluginManager.getRuntimeMode())) {
                        checkDifferentClassLoaders(type, extensionClass);
                    }
                }
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }

        if (result.isEmpty()) {
            log.debug("No extensions found for extension point '{}'", type.getName());
        } else {
            log.debug("Found {} extensions for extension point '{}'", result.size(), type.getName());
        }

        // sort by "ordinal" property
        Collections.sort(result);

        return result;
    }

    @Override
    public List<ExtensionWrapper> find(String pluginId) {
        log.debug("Finding extensions from plugin '{}'", pluginId);
        List<ExtensionWrapper> result = new ArrayList<>();

        Set<String> classNames = findClassNames(pluginId);
        if (classNames.isEmpty()) {
            return result;
        }

        if (pluginId != null) {
            PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
            if (PluginState.STARTED != pluginWrapper.getPluginState()) {
                return result;
            }

            log.trace("Checking extensions from plugin '{}'", pluginId);
        } else {
            log.trace("Checking extensions from classpath");
        }

        ClassLoader classLoader = (pluginId != null) ? pluginManager.getPluginClassLoader(pluginId) : getClass().getClassLoader();

        for (String className : classNames) {
            try {
                log.debug("Loading class '{}' using class loader '{}'", className, classLoader);
                Class<?> extensionClass = classLoader.loadClass(className);

                ExtensionWrapper extensionWrapper = createExtensionWrapper(extensionClass);
                result.add(extensionWrapper);
                log.debug("Added extension '{}' with ordinal {}", className, extensionWrapper.getOrdinal());
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                log.error(e.getMessage(), e);
            }
        }

        if (result.isEmpty()) {
            log.debug("No extensions found for plugin '{}'", pluginId);
        } else {
            log.debug("Found {} extensions for plugin '{}'", result.size(), pluginId);
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

        // By default we're assuming, that no checks for extension dependencies are necessary.
        //
        // A plugin, that has an optional dependency to other plugins, might lead to unloadable
        // Java classes (NoClassDefFoundError) at application runtime due to possibly missing
        // dependencies. Therefore we're enabling the check for optional extensions, if the
        // started plugin contains at least one optional plugin dependency.
        if (checkForExtensionDependencies == null && PluginState.STARTED.equals(event.getPluginState())) {
            for (PluginDependency dependency : event.getPlugin().getDescriptor().getDependencies()) {
                if (dependency.isOptional()) {
                    log.debug("Enable check for extension dependencies via ASM.");
                    checkForExtensionDependencies = true;
                    break;
                }
            }
        }
    }

    /**
     * Returns true, if the extension finder checks extensions for its required plugins.
     * This feature has to be enabled, in order check the availability of
     * {@link Extension#plugins()} configured by an extension.
     * <p>
     * This feature is enabled by default, if at least one available plugin makes use of
     * optional plugin dependencies. Those optional plugins might not be available at runtime.
     * Therefore any extension is checked by default against available plugins before its
     * instantiation.
     * <p>
     * Notice: This feature requires the optional <a href="https://asm.ow2.io/">ASM library</a>
     * to be available on the applications classpath.
     *
     * @return true, if the extension finder checks extensions for its required plugins
     */
    public final boolean isCheckForExtensionDependencies() {
        return Boolean.TRUE.equals(checkForExtensionDependencies);
    }

    /**
     * Plugin developers may enable / disable checks for required plugins of an extension.
     * This feature has to be enabled, in order check the availability of
     * {@link Extension#plugins()} configured by an extension.
     * <p>
     * This feature is enabled by default, if at least one available plugin makes use of
     * optional plugin dependencies. Those optional plugins might not be available at runtime.
     * Therefore any extension is checked by default against available plugins before its
     * instantiation.
     * <p>
     * Notice: This feature requires the optional <a href="https://asm.ow2.io/">ASM library</a>
     * to be available on the applications classpath.
     *
     * @param checkForExtensionDependencies true to enable checks for optional extensions, otherwise false
     */
    public void setCheckForExtensionDependencies(boolean checkForExtensionDependencies) {
        this.checkForExtensionDependencies = checkForExtensionDependencies;
    }

    protected void debugExtensions(Set<String> extensions) {
        if (log.isDebugEnabled()) {
            if (extensions.isEmpty()) {
                log.debug("No extensions found");
            } else {
                log.debug("Found possible {} extensions:", extensions.size());
                for (String extension : extensions) {
                    log.debug("   " + extension);
                }
            }
        }
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

    /**
     * Returns the parameters of an {@link Extension} annotation without loading
     * the corresponding class into the class loader.
     *
     * @param className name of the class, that holds the requested {@link Extension} annotation
     * @param classLoader class loader to access the class
     * @return the contents of the {@link Extension} annotation or null, if the class does not
     * have an {@link Extension} annotation
     */
    private ExtensionInfo getExtensionInfo(String className, ClassLoader classLoader) {
        if (extensionInfos == null) {
            extensionInfos = new HashMap<>();
        }

        if (!extensionInfos.containsKey(className)) {
            log.trace("Load annotation for '{}' using asm", className);
            ExtensionInfo info = ExtensionInfo.load(className, classLoader);
            if (info == null) {
                log.warn("No extension annotation was found for '{}'", className);
                extensionInfos.put(className, null);
            } else {
                extensionInfos.put(className, info);
            }
        }

        return extensionInfos.get(className);
    }

    private ExtensionWrapper createExtensionWrapper(Class<?> extensionClass) {
        Extension extensionAnnotation = findExtensionAnnotation(extensionClass);
        int ordinal = extensionAnnotation != null ? extensionAnnotation.ordinal() : 0;
        ExtensionDescriptor descriptor = new ExtensionDescriptor(ordinal, extensionClass);

        return new ExtensionWrapper<>(descriptor, pluginManager.getExtensionFactory());
    }

    private Extension findExtensionAnnotation(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Extension.class)) {
            return clazz.getAnnotation(Extension.class);
        }

        // search recursively through all annotations
        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> annotationClass = annotation.annotationType();
            Extension extensionAnnotation = findExtensionAnnotation(annotationClass);
            if (extensionAnnotation != null) {
                return extensionAnnotation;
            }
        }

        return null;
    }

    private void checkDifferentClassLoaders(Class<?> type, Class<?> extensionClass) {
        ClassLoader typeClassLoader = type.getClassLoader(); // class loader of extension point
        ClassLoader extensionClassLoader = extensionClass.getClassLoader();
        boolean match = ClassUtils.getAllInterfacesNames(extensionClass).contains(type.getSimpleName());
        if (match && !extensionClassLoader.equals(typeClassLoader)) {
            // in this scenario the method 'isAssignableFrom' returns only FALSE
            // see http://www.coderanch.com/t/557846/java/java/FWIW-FYI-isAssignableFrom-isInstance-differing
            log.error("Different class loaders: '{}' (E) and '{}' (EP)", extensionClassLoader, typeClassLoader);
        }
    }

}
