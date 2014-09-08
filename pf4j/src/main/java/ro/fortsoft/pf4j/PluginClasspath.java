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

import java.util.ArrayList;
import java.util.List;

/**
 * The classpath of a plug-in after it is unpacked.
 * It contains classes directories and lib directories (directories that contains jars).
 * All directories are relative to plug-in repository.
 * The default values are "classes" and "lib".
 * 
 * @author Decebal Suiu
 */
public class PluginClasspath {
	// Default values of classpath.
	private static final String DEFAULT_CLASSES_DIRECTORY = "classes";
	private static final String DEFAULT_LIB_DIRECTORY = "lib";
	// Lists of class and lib directories.
	protected List<String> classesDirectories;
	protected List<String> libDirectories;

	/**
	 * Sets classpath of a plug-in.
	 */
	public PluginClasspath() {
		classesDirectories = new ArrayList<String>();
		libDirectories = new ArrayList<String>();
		// Add the default values "class" and "lib" to classpath.
		addResources();
	}

	/**
	 * Get a list of classes directories.
	 * 
	 * @return A list of classes directories. The first element is default value "classes".
	 */
	public List<String> getClassesDirectories() {
		return classesDirectories;
	}

	/**
	 * Store a list of classes directories.
	 * 
	 * @param classesDirectories
	 *          A list of classes directories to be stored.
	 */
	public void setClassesDirectories(List<String> classesDirectories) {
		this.classesDirectories = classesDirectories;
	}

	/**
	 * Get a list of lib directories.
	 * 
	 * @return A list of lib directories. The first element is default value "lib".
	 */
	public List<String> getLibDirectories() {
		return libDirectories;
	}

	/**
	 * Store a list of lib directories.
	 * 
	 * @param libDirectories
	 *          A list of lib directories to be stored.
	 */
	public void setLibDirectories(List<String> libDirectories) {
		this.libDirectories = libDirectories;
	}

	/**
	 * Add the default values "class" and "lib" to Lists.
	 */
	protected void addResources() {
		classesDirectories.add(DEFAULT_CLASSES_DIRECTORY);
		libDirectories.add(DEFAULT_LIB_DIRECTORY);
	}

}
