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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * One instance of this class should be created by plugin manager for every available plug-in.
 * 
 * @author Decebal Suiu
 */
public class PluginClassLoader extends URLClassLoader {

	private static final String JAVA_PACKAGE_PREFIX = "java.";
	private static final String JAVAX_PACKAGE_PREFIX = "javax.";
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

	@Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
//		System.out.println(">>>" + className);

		// first check whether it's a system class, delegate to the system loader
		if (className.startsWith(JAVA_PACKAGE_PREFIX) || className.startsWith(JAVAX_PACKAGE_PREFIX)) {
			return findSystemClass(className);
		}

        // second check whether it's already been loaded
        Class<?> loadedClass = findLoadedClass(className);
        if (loadedClass != null) {
        	return loadedClass;
        }

        // nope, try to load locally
        try {
        	return findClass(className);
        } catch (ClassNotFoundException e) {
        	// try next step
        }

        // if the class it's a part of the plugin engine use parent class loader
        if (className.startsWith(PLUGIN_PACKAGE_PREFIX)) {
        	try {
        		return PluginClassLoader.class.getClassLoader().loadClass(className);
        	} catch (ClassNotFoundException e) {
        		// try next step
        	}
        }

        // look in dependencies
        List<PluginDependency> dependencies = pluginDescriptor.getDependencies();
        for (PluginDependency dependency : dependencies) {
        	PluginClassLoader classLoader = pluginManager.getPluginClassLoader(dependency.getPluginId());
        	try {
        		return classLoader.loadClass(className);
        	} catch (ClassNotFoundException e) {
        		// try next dependency
        	}
        }

        // use the standard URLClassLoader (which follows normal parent delegation)
        return super.loadClass(className);
    }

}
