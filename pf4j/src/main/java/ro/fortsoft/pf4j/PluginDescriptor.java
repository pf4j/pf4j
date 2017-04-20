/*
 * Copyright 2012 Decebal Suiu
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

import com.github.zafarkhaja.semver.Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A plugin descriptor contains information about a plug-in obtained
 * from the manifest (META-INF) file.
 *
 * @author Decebal Suiu
 */
public class PluginDescriptor {

	private String pluginId;
	private String pluginDescription;
    private String pluginClass;
    private Version version;
    private String requires = "*"; // SemVer format
    private String provider;
    private List<PluginDependency> dependencies;
    private String license;

    public PluginDescriptor() {
        dependencies = new ArrayList<>();
    }

    /**
     * Returns the unique identifier of this plugin.
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Returns the description of this plugin.
     */
    public String getPluginDescription() {
        return pluginDescription;
    }

    /**
     * Returns the name of the class that implements Plugin interface.
     */
    public String getPluginClass() {
        return pluginClass;
    }

    /**
     * Returns the version of this plugin.
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Returns string version of requires
     * @return String with requires expression on SemVer format
     */
    public String getRequires() {
        return requires;
    }

    /**
     * Returns the provider name of this plugin.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the legal license of this plugin, e.g. "Apache-2.0", "MIT" etc
     */
    public String getLicense() {
        return license;
    }

    /**
     * Returns all dependencies declared by this plugin.
     * Returns an empty array if this plugin does not declare any require.
     */
    public List<PluginDependency> getDependencies() {
        return dependencies;
    }

    @Override
	public String toString() {
		return "PluginDescriptor [pluginId=" + pluginId + ", pluginClass="
				+ pluginClass + ", version=" + version + ", provider="
				+ provider + ", dependencies=" + dependencies + ", description="
                + pluginDescription + ", requires=" + requires + ", license="
				+ license + "]";
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

    void setRequires(String requires) {
        this.requires = requires;
    }

    void setDependencies(String dependencies) {
    	if (dependencies != null) {
    		dependencies = dependencies.trim();
    		if (dependencies.isEmpty()) {
    			this.dependencies = Collections.emptyList();
    		} else {
	    		this.dependencies = new ArrayList<>();
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

    public void setLicense(String license) {
        this.license = license;
    }

}
