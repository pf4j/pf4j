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
package org.pf4j;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.Vector;

import org.pf4j.util.DirectoryFilter;
import org.pf4j.util.JarFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Load all informations needed by a plugin.
 * This means add all jar files from 'lib' directory, 'classes'
 * to classpath.
 * It's a class for only the internal use.
 *
 * @author Decebal Suiu
 */
class PluginLoader {

	private static final Logger LOG = LoggerFactory.getLogger(PluginLoader.class);

    /*
     * The plugin repository.
     */
    private File pluginRepository;

    /*
     * The directory with '.class' files.
     */
    private File classesDirectory;

    /*
     * The directory with '.jar' files.
     */
    private File libDirectory;

    private PluginClassLoader pluginClassLoader;

    public PluginLoader(PluginManager pluginManager, PluginWrapper pluginWrapper, File pluginRepository) {
        this.pluginRepository = pluginRepository;
        classesDirectory = new File(pluginRepository, "classes");
        libDirectory = new File(pluginRepository, "lib");
        ClassLoader parent = getClass().getClassLoader(); 
        pluginClassLoader = new PluginClassLoader(pluginManager, pluginWrapper, parent);        
        LOG.debug("Created class loader " + pluginClassLoader);
    }

    public File getPluginRepository() {
        return pluginRepository;
    }

    public boolean load() {
        return loadClassesAndJars();
    }

    public PluginClassLoader getPluginClassLoader() {
		return pluginClassLoader;
	}

	private boolean loadClassesAndJars() {
       return loadClasses() && loadJars();
    }

    private void getJars(Vector<String> v, File file) {
        FilenameFilter jarFilter = new JarFilter();
        FilenameFilter directoryFilter = new DirectoryFilter();

        if (file.exists() && file.isDirectory() && file.isAbsolute()) {
            String[] jars = file.list(jarFilter);
            for (int i = 0; (jars != null) && (i < jars.length); ++i) {
                v.addElement(jars[i]);
            }

            String[] directoryList = file.list(directoryFilter);
            for (int i = 0; (directoryList != null) && (i < directoryList.length); ++i) {
                File directory = new File(file, directoryList[i]);
                getJars(v, directory);
            }
        }
    }

    private boolean loadClasses() {
        // make 'classesDirectory' absolute
        classesDirectory = classesDirectory.getAbsoluteFile();

        if (classesDirectory.exists() && classesDirectory.isDirectory()) {
            LOG.debug("Found '" + classesDirectory.getPath() + "' directory");

            try {
                pluginClassLoader.addURL(classesDirectory.toURI().toURL());
                LOG.debug("Added '" + classesDirectory + "' to the class loader path");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
                return false;
            }
        }

        return true;
    }

    /**
     * Add all *.jar files from '/lib' directory.
     */
    private boolean loadJars() {
        // make 'jarDirectory' absolute
        libDirectory = libDirectory.getAbsoluteFile();

        Vector<String> jars = new Vector<String>();
        getJars(jars, libDirectory);
        for (String jar : jars) {
            File jarFile = new File(libDirectory, jar);
            try {
                pluginClassLoader.addURL(jarFile.toURI().toURL());
                LOG.debug("Added '" + jarFile + "' to the class loader path");
            } catch (MalformedURLException e) {
                e.printStackTrace();
                LOG.error(e.getMessage(), e);
                return false;
            }
        }

        return true;
    }

}
