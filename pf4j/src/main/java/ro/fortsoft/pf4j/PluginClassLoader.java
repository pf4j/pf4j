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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * One instance of this class should be created by plugin manager for every available plug-in.
 * This class loader is a Parent Last ClassLoader - it loads the classes from the plugin's jars before delegating
 * to the parent class loader.
 *
 * @author Decebal Suiu
 */
public class PluginClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(PluginClassLoader.class);

	private static final String PLUGIN_PACKAGE_PREFIX = "ro.fortsoft.pf4j.";

	private PluginManager pluginManager;
	private PluginDescriptor pluginDescriptor;

	public PluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
		super(new URL[0], parent);

		this.pluginManager = pluginManager;
		this.pluginDescriptor = pluginDescriptor;
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}

    /**
     * This implementation of loadClass uses a child first delegation model rather than the standard parent first.
     * If the requested class cannot be found in this class loader, the parent class loader will be consulted
     * via the standard ClassLoader.loadClass(String) mechanism.
     */
	@Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {
            log.debug("Received request to load class '{}'", className);
            // if the class it's a part of the plugin engine use parent class loader
            if (className.startsWith(PLUGIN_PACKAGE_PREFIX)) {
                log.debug("Delegate the loading of class '{}' to parent", className);
                try {
                    return getClass().getClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    // try next step
                    // TODO if I uncomment below lines (the correct approach) I received ClassNotFoundException for demo (ro.fortsoft.pf4j.demo)
                    //                log.error(e.getMessage(), e);
                    //                throw e;
                }
            }

            // second check whether it's already been loaded
            Class<?> clazz = findLoadedClass(className);
            if (clazz != null) {
                log.debug("Found loaded class '{}'", className);
                return clazz;
            }

            // nope, try to load locally
            try {
                clazz = findClass(className);
                log.debug("Found class '{}' in plugin classpath", className);
                return clazz;
            } catch (ClassNotFoundException e) {
                // try next step
            }

            // look in dependencies
            log.debug("Look in dependencies for class '{}'", className);
            List<PluginDependency> dependencies = pluginDescriptor.getDependencies();
            for (PluginDependency dependency : dependencies) {
                PluginClassLoader classLoader = pluginManager.getPluginClassLoader(dependency.getPluginId());
                try {
                    return classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    // try next dependency
                }
            }

            log.debug("Couldn't find class '{}' in plugin classpath. Delegating to parent");

            // use the standard URLClassLoader (which follows normal parent delegation)
            return super.loadClass(className);
        }
    }

    /**
     * Load the named resource from this plugin. This implementation checks the plugin's classpath first
     * then delegates to the parent.
     *
     * @param name the name of the resource.
     * @return the URL to the resource, <code>null</code> if the resource was not found.
     */
    @Override
    public URL getResource(String name) {
        log.debug("Trying to find resource '{}' in plugin classpath", name);
        URL url = findResource(name);
        if (url != null) {
            log.debug("Found resource '{}' in plugin classpath", name);
            return url;
        }

        log.debug("Couldn't find resource '{}' in plugin classpath. Delegating to parent");

        return super.getResource(name);
    }

    /**
     * Release all resources acquired by this class loader.
     * The current implementation is incomplete.
     * For now, this instance can no longer be used to load
     * new classes or resources that are defined by this loader.
     */
    public void dispose() {
        try {
            close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
