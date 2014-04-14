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
public class PluginDependency {

	private String pluginId;
	private Version pluginVersion;

	public PluginDependency(String dependency) {
		/*
		 int index = dependency.indexOf(':');
		 if (index == -1) {
			 throw new IllegalArgumentException("Illegal dependency specifier "+ dependency);
		 }

		 this.pluginId = dependency.substring(0, index);
		 this.pluginVersion = Version.createVersion(dependency.substring(index + 1));
		 */
		this.pluginId = dependency;
	}

	public String getPluginId() {
		return pluginId;
	}

	public Version getPluginVersion() {
		return pluginVersion;
	}

	@Override
	public String toString() {
		return "PluginDependency [pluginId=" + pluginId + ", pluginVersion=" + pluginVersion + "]";
	}

}
