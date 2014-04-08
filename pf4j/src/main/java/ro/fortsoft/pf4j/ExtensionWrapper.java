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

/**
 * @author Decebal Suiu
 */
public class ExtensionWrapper<T> implements Comparable<ExtensionWrapper<T>> {

	private final String pluginId;
	private final T instance;
	private final int ordinal;

	public ExtensionWrapper(String pluginId, T instance, int ordinal) {
		this.pluginId = pluginId;
		this.instance = instance;
		this.ordinal = ordinal;
	}

	public String getPluginId() {
		return pluginId;
	}

	public T getInstance() {
		return instance;
	}

	public int getOrdinal() {
		return ordinal;
	}

	@Override
	public int compareTo(ExtensionWrapper<T> o) {
		return (ordinal - o.getOrdinal());
	}

}