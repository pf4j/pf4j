/*
 * Copyright 2013 Decebal Suiu
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

/**
 * Overwrite classes directories to "target/classes" and lib directories to "target/lib".
 *
 * @author Decebal Suiu
 */
public class DevelopmentPluginClasspath extends PluginClasspath {

	private static final String DEVELOPMENT_CLASSES_DIRECTORY = "target/classes";
	private static final String DEVELOPMENT_LIB_DIRECTORY = "target/lib";

	public DevelopmentPluginClasspath() {
		super();
	}

	@Override
	protected void addResources() {
		classesDirectories.add(DEVELOPMENT_CLASSES_DIRECTORY);
		libDirectories.add(DEVELOPMENT_LIB_DIRECTORY);
	}


}
