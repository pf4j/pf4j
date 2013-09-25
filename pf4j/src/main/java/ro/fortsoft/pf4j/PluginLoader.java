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

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.util.DirectoryFileFilter;
import ro.fortsoft.pf4j.util.JarFileFilter;

/**
 * Load all informations needed by a plugin.
 * This means add all jar files from 'lib' directory, 'classes'
 * to classpath.
 * It's a class for only the internal use.
 *
 * @author Decebal Suiu
 */
class PluginLoader {

	private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

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

    public PluginLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, File pluginRepository) {
        this.pluginRepository = pluginRepository;
        classesDirectory = new File(pluginRepository, "classes");
        libDirectory = new File(pluginRepository, "lib");
        ClassLoader parent = getClass().getClassLoader(); 
        pluginClassLoader = new PluginClassLoader(pluginManager, pluginDescriptor, parent);        
        log.debug("Created class loader {}", pluginClassLoader);
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

    private void getJars(Vector<File> bucket, File file) {
        FileFilter jarFilter = new JarFileFilter();
        FileFilter directoryFilter = new DirectoryFileFilter();

        if (file.exists() && file.isDirectory() && file.isAbsolute()) {
            File[] jars = file.listFiles(jarFilter);
            for (int i = 0; (jars != null) && (i < jars.length); ++i) {
                bucket.addElement(jars[i]);
            }

            File[] directories = file.listFiles(directoryFilter);
            for (int i = 0; (directories != null) && (i < directories.length); ++i) {
                File directory = directories[i];
                getJars(bucket, directory);
            }
        }
    }

    private boolean loadClasses() {
        // make 'classesDirectory' absolute
        classesDirectory = classesDirectory.getAbsoluteFile();

        if (classesDirectory.exists() && classesDirectory.isDirectory()) {
            log.debug("Found '{}' directory", classesDirectory.getPath());

            try {
                pluginClassLoader.addURL(classesDirectory.toURI().toURL());
                log.debug("Added '{}' to the class loader path", classesDirectory);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
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

        Vector<File> jars = new Vector<File>();
        getJars(jars, libDirectory);
        for (File jar : jars) {
            try {
                pluginClassLoader.addURL(jar.toURI().toURL());
                log.debug("Added '{}' to the class loader path", jar);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
                return false;
            }
        }

        return true;
    }

}
