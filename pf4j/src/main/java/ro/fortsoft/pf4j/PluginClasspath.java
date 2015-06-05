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
 * The classpath of the plugin after it was unpacked.
 * It contains classes directories and lib directories (directories that contains jars).
 * All directories are relative to plugin repository.
 * The default values are "classes" and "lib".
 *
 * @author Decebal Suiu
 */
public class PluginClasspath {

	private static final String DEFAULT_CLASSES_DIRECTORY = "classes";
	private static final String DEFAULT_LIB_DIRECTORY = "lib";

	protected List<String> classesDirectories;
	protected List<String> libDirectories;

	public PluginClasspath() {
		classesDirectories = new ArrayList<>();
		libDirectories = new ArrayList<>();

		addResources();
	}

	public List<String> getClassesDirectories() {
		return classesDirectories;
	}

	public void setClassesDirectories(List<String> classesDirectories) {
		this.classesDirectories = classesDirectories;
	}

	public List<String> getLibDirectories() {
		return libDirectories;
	}

	public void setLibDirectories(List<String> libDirectories) {
		this.libDirectories = libDirectories;
	}

	protected void addResources() {
		classesDirectories.add(DEFAULT_CLASSES_DIRECTORY);
		libDirectories.add(DEFAULT_LIB_DIRECTORY);
	}

}
