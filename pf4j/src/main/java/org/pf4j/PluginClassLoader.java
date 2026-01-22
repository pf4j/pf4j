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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * One instance of this class should be created for every available plug-in.
 * It's responsible for loading classes and resources from the plug-in.
 * <p>
 * By default, this {@link ClassLoader} is a Parent Last ClassLoader - it loads the classes from the plugin's jars
 * before delegating to the parent class loader.
 * Use {@link #classLoadingStrategy} to change the loading strategy.
 *
 * @author Decebal Suiu
 */
public class PluginClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(PluginClassLoader.class);

    private static final String JAVA_PACKAGE_PREFIX = "java.";
    private static final String PLUGIN_PACKAGE_PREFIX = "org.pf4j.";

    private final PluginManager pluginManager;
    private final PluginDescriptor pluginDescriptor;
    private final ClassLoadingStrategy classLoadingStrategy;
    private boolean closed;

    public PluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
        this(pluginManager, pluginDescriptor, parent, ClassLoadingStrategy.PDA);
    }

    /**
     * Creates a new {@link PluginClassLoader} for the given plugin using parent first strategy.
     *
     * @deprecated Replaced by {@link #PluginClassLoader(PluginManager, PluginDescriptor, ClassLoader, ClassLoadingStrategy)}.
     * If {@code parentFirst} is {@code true}, indicates that the parent {@link ClassLoader} should be consulted
     * before trying to load a class through this loader.
     */
    @Deprecated
    public PluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent, boolean parentFirst) {
        this(pluginManager, pluginDescriptor, parent, parentFirst ? ClassLoadingStrategy.APD : ClassLoadingStrategy.PDA);
    }

    /**
     * Creates a new {@link PluginClassLoader} for the given plugin using the specified class loading strategy.
     *
     * @param pluginManager the plugin manager
     * @param pluginDescriptor the plugin descriptor
     * @param parent the parent class loader
     * @param classLoadingStrategy the strategy to use for loading classes and resources
     */
    public PluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent, ClassLoadingStrategy classLoadingStrategy) {
        super(new URL[0], parent);

        this.pluginManager = pluginManager;
        this.pluginDescriptor = pluginDescriptor;
        this.classLoadingStrategy = classLoadingStrategy;
    }

    /**
     * Adds the specified URL to the search path for classes and resources.
     *
     * @param url the URL to be added to the search path of URLs
     */
    @Override
    public void addURL(URL url) {
        log.debug("Add '{}'", url);
        super.addURL(url);
    }

    /**
     * Adds the specified file to the search path for classes and resources.
     *
     * @param file the file to be added to the search path of URLs
     */
    public void addFile(File file) {
        try {
            addURL(file.getCanonicalFile().toURI().toURL());
        } catch (IOException e) {
//            throw new RuntimeException(e);
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Loads the class with the specified name.
     * <p>
     * By default, it uses a child first delegation model rather than the standard parent first.
     * If the requested class cannot be found in this class loader, the parent class loader will be consulted
     * via the standard {@link ClassLoader#loadClass(String)} mechanism.
     * Use {@link #classLoadingStrategy} to change the loading strategy.
     *
     * @param className the name of the class
     * @return the loaded class
     */
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {
            // first check whether it's a system class, delegate to the system loader
            if (className.startsWith(JAVA_PACKAGE_PREFIX)) {
                return findSystemClass(className);
            }

            // if the class is part of the plugin engine use parent class loader
            if (shouldDelegateToParent(className)) {
//                log.trace("Delegate the loading of PF4J class '{}' to parent", className);
                return getParent().loadClass(className);
            }

            log.trace("Received request to load class '{}'", className);

            // second check whether it's already been loaded
            Class<?> loadedClass = findLoadedClass(className);
            if (loadedClass != null) {
                log.trace("Found loaded class '{}'", className);
                return loadedClass;
            }

            for (ClassLoadingStrategy.Source classLoadingSource : classLoadingStrategy.getSources()) {
                Class<?> c = null;
                try {
                    switch (classLoadingSource) {
                        case APPLICATION:
                            c = super.loadClass(className);
                            break;
                        case PLUGIN:
                            c = findClass(className);
                            break;
                        case DEPENDENCIES:
                            c = loadClassFromDependencies(className);
                            break;
                    }
                } catch (ClassNotFoundException ignored) {}

                if (c != null) {
                    log.trace("Found class '{}' in {} classpath", className, classLoadingSource);
                    return c;
                } else {
                    log.trace("Couldn't find class '{}' in {} classpath", className, classLoadingSource);
                }
            }

            throw new ClassNotFoundException(className);
        }
    }

    /**
     * Loads the named resource from this plugin.
     * <p>
     * By default, this implementation checks the plugin's classpath first then delegates to the parent.
     * Use {@link #classLoadingStrategy} to change the loading strategy.
     *
     * @param name the name of the resource.
     * @return the URL to the resource, {@code null} if the resource was not found.
     */
    @Override
    public URL getResource(String name) {
        ClassLoadingStrategy loadingStrategy = getClassLoadingStrategy(name);
        log.trace("Received request to load resource '{}'", name);
        for (ClassLoadingStrategy.Source classLoadingSource : loadingStrategy.getSources()) {
            URL url = null;
            switch (classLoadingSource) {
                case APPLICATION:
                    url = super.getResource(name);
                    break;
                case PLUGIN:
                    url = findResource(name);
                    break;
                case DEPENDENCIES:
                    url = findResourceFromDependencies(name);
                    break;
            }

            if (url != null) {
                log.trace("Found resource '{}' in {} classpath", name, classLoadingSource);
                return url;
            } else {
                log.trace("Couldn't find resource '{}' in {}", name, classLoadingSource);
            }
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> resources = new ArrayList<>();
        ClassLoadingStrategy loadingStrategy = getClassLoadingStrategy(name);
        log.trace("Received request to load resources '{}'", name);
        for (ClassLoadingStrategy.Source classLoadingSource : loadingStrategy.getSources()) {
            switch (classLoadingSource) {
                case APPLICATION:
                    if (getParent() != null) {
                        resources.addAll(Collections.list(getParent().getResources(name)));
                    }
                    break;
                case PLUGIN:
                    resources.addAll(Collections.list(findResources(name)));
                    break;
                case DEPENDENCIES:
                    resources.addAll(findResourcesFromDependencies(name));
                    break;
            }
        }

        return Collections.enumeration(resources);
    }

    private ClassLoadingStrategy getClassLoadingStrategy(String name) {
        ClassLoadingStrategy loadingStrategy = classLoadingStrategy;
        if (IndexedExtensionFinder.EXTENSIONS_RESOURCE.equals(name)) {
            loadingStrategy = ClassLoadingStrategy.PAD;
        }
        return loadingStrategy;
    }

    /**
     * Determines whether a class should be delegated to the parent class loader.
     * <p>
     * By default, classes in the {@code org.pf4j} package are delegated to the parent class loader,
     * except for test utilities ({@code org.pf4j.test}) which are part of the plugin's test classpath.
     * <p>
     * This method can be overridden by subclasses to customize which packages should be excluded
     * from parent delegation. This is useful for framework extensions or custom class loading policies.
     *
     * @param className the name of the class to check
     * @return {@code true} if the class should be loaded by the parent class loader, {@code false} otherwise
     */
    protected boolean shouldDelegateToParent(String className) {
        return className.startsWith(PLUGIN_PACKAGE_PREFIX)
            && !className.startsWith("org.pf4j.test");
    }

    /**
     * Closes this class loader.
     * <p>
     * This method should be called when the class loader is no longer needed.
     */
    @Override
    public void close() throws IOException {
        super.close();

        closed = true;
    }

    /**
     * Returns whether this class loader has been closed.
     *
     * @return {@code true} if this class loader has been closed, {@code false} otherwise
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Loads the class with the specified name from the dependencies of the plugin.
     *
     * @param className the name of the class
     * @return the loaded class
     */
    protected Class<?> loadClassFromDependencies(String className) {
        log.trace("Search in dependencies for class '{}'", className);
        List<PluginDependency> dependencies = pluginDescriptor.getDependencies();
        for (PluginDependency dependency : dependencies) {
            ClassLoader classLoader = pluginManager.getPluginClassLoader(dependency.getPluginId());

            // If the dependency is marked as optional, its class loader might not be available.
            if (classLoader == null && dependency.isOptional()) {
                continue;
            }

            try {
                return classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                // try next dependency
            }
        }

        return null;
    }

    /**
     * Finds the resource with the given name in the dependencies of the plugin.
     *
     * @param name the name of the resource
     * @return the URL to the resource, {@code null} if the resource was not found
     */
    protected URL findResourceFromDependencies(String name) {
        log.trace("Search in dependencies for resource '{}'", name);
        List<PluginDependency> dependencies = pluginDescriptor.getDependencies();
        for (PluginDependency dependency : dependencies) {
            PluginClassLoader classLoader = (PluginClassLoader) pluginManager.getPluginClassLoader(dependency.getPluginId());

            // If the dependency is marked as optional, its class loader might not be available.
            if (classLoader == null && dependency.isOptional()) {
                continue;
            }

            URL url = classLoader.findResource(name);
            if (Objects.nonNull(url)) {
                return url;
            }
        }

        return null;
    }

    /**
     * Finds all resources with the given name in the dependencies of the plugin.
     *
     * @param name the name of the resource
     * @return an enumeration of {@link URL} objects for the resource
     * @throws IOException if I/O errors occur
     */
    protected Collection<URL> findResourcesFromDependencies(String name) throws IOException {
        log.trace("Search in dependencies for resources '{}'", name);
        List<URL> results = new ArrayList<>();
        List<PluginDependency> dependencies = pluginDescriptor.getDependencies();
        for (PluginDependency dependency : dependencies) {
            PluginClassLoader classLoader = (PluginClassLoader) pluginManager.getPluginClassLoader(dependency.getPluginId());

            // If the dependency is marked as optional, its class loader might not be available.
            if (classLoader == null && dependency.isOptional()) {
                continue;
            }

            results.addAll(Collections.list(classLoader.findResources(name)));
        }

        return results;
    }

}
