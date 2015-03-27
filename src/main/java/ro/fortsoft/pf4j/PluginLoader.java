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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.util.DirectoryFileFilter;
import ro.fortsoft.pf4j.util.JarFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

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

    private PluginClasspath pluginClasspath;
    private IzouPluginClassLoader pluginClassLoader;

    public PluginLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, File pluginRepository, PluginClasspath pluginClasspath) {
        this.pluginRepository = pluginRepository;
        this.pluginClasspath = pluginClasspath;

        ClassLoader parent = getClass().getClassLoader();
        pluginClassLoader = new IzouPluginClassLoader(pluginManager, pluginDescriptor, parent);
        log.debug("Created class loader '{}'", pluginClassLoader);
    }

    public File getPluginRepository() {
        return pluginRepository;
    }

    public boolean load() {
        return loadClassesAndJars();
    }

    public IzouPluginClassLoader getPluginClassLoader() {
		return pluginClassLoader;
	}

	private boolean loadClassesAndJars() {
       return loadClasses() && loadJars();
    }

    private boolean loadClasses() {
    	List<String> classesDirectories = pluginClasspath.getClassesDirectories();

    	// add each classes directory to plugin class loader
    	for (String classesDirectory : classesDirectories) {
	        // make 'classesDirectory' absolute
	        File file = new File(pluginRepository, classesDirectory).getAbsoluteFile();

	        if (file.exists() && file.isDirectory()) {
	            log.debug("Found '{}' directory", file.getPath());

	            try {
	                pluginClassLoader.addURL(file.toURI().toURL());
	                log.debug("Added '{}' to the class loader path", file);
	            } catch (MalformedURLException e) {
	                e.printStackTrace();
	                log.error(e.getMessage(), e);
	                return false;
	            }
	        }
    	}

        return true;
    }

    /**
     * Add all *.jar files from lib directories to class loader.
     */
    private boolean loadJars() {
    	List<String> libDirectories = pluginClasspath.getLibDirectories();

    	// add each jars directory to plugin class loader
    	for (String libDirectory : libDirectories) {
	        // make 'libDirectory' absolute
	        File file = new File(pluginRepository, libDirectory).getAbsoluteFile();

	        // collect all jars from current lib directory in jars variable
	        Vector<File> jars = new Vector<File>();
	        getJars(jars, file);
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
    	}

        return true;
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

}
