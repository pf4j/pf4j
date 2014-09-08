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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A plug-in descriptor contains information about a plug-in obtained
 * from the manifest (META-INF) file.
 * 
 * @author Decebal Suiu
 */
public class PluginDescriptor {

	private String pluginId;
	private String pluginDescription;
	private String pluginClass;
	private Version version;
	private Version requires;
	private String provider;
	private List<PluginDependency> dependencies;

	/**
	 * Constructor that sets default required version,
	 * and creates an empty list of plug-in dependency.
	 */
	public PluginDescriptor() {
		requires = Version.ZERO;
		dependencies = new ArrayList<PluginDependency>();
	}

	/**
	 * Get the unique identifier of this plug-in.
	 * 
	 * @return The unique identifier of this plug-in.
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * Get the description of this plug-in.
	 * 
	 * @return The description of this plug-in.
	 */
	public String getPluginDescription() {
		return pluginDescription;
	}

	/**
	 * Get the name of a class that extends Plugin abstract class.
	 * 
	 * @return The name of a class that extends Plugin abstract class.
	 */
	public String getPluginClass() {
		return pluginClass;
	}

	/**
	 * Get the version of this plug-in.
	 * 
	 * @return The version of this plug-in.
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Get the required version of this plug-in
	 * 
	 * @return The required version of this plug-in.
	 */
	public Version getRequires() {
		return requires;
	}

	/**
	 * Get the provider name of this plug-in.
	 * 
	 * @return The provider name of this plug-in.
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * Get a list of all dependencies declared by this plug-in.
	 * 
	 * @return A list of all dependencies declared by this plug-in or an empty list if the plug-in does not declar any
	 *         requirement.
	 */
	public List<PluginDependency> getDependencies() {
		return dependencies;
	}

	@Override
	public String toString() {
		return "PluginDescriptor [pluginId=" + pluginId + ", pluginClass=" + pluginClass + ", version=" + version
				+ ", provider=" + provider + ", dependencies=" + dependencies + "]";
	}

	void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	void setPluginDescription(String pluginDescription) {
		this.pluginDescription = pluginDescription;
	}

	void setPluginClass(String pluginClassName) {
		this.pluginClass = pluginClassName;
	}

	void setPluginVersion(Version version) {
		this.version = version;
	}

	void setProvider(String provider) {
		this.provider = provider;
	}

	void setRequires(Version requires) {
		this.requires = requires;
	}

	void setDependencies(String dependencies) {
		if (dependencies != null) {
			dependencies = dependencies.trim();
			if (dependencies.isEmpty()) {
				this.dependencies = Collections.emptyList();
			} else {
				this.dependencies = new ArrayList<PluginDependency>();
				String[] tokens = dependencies.split(",");
				for (String dependency : tokens) {
					dependency = dependency.trim();
					if (!dependency.isEmpty()) {
						this.dependencies.add(new PluginDependency(dependency));
					}
				}
				if (this.dependencies.isEmpty()) {
					this.dependencies = Collections.emptyList();
				}
			}
		} else {
			this.dependencies = Collections.emptyList();
		}
	}

}
