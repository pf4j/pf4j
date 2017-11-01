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
package org.pf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A plugin descriptor contains information about a plug-in obtained
 * from the manifest (META-INF) file.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginDescriptor implements PluginDescriptor {

	private String pluginId;
	private String pluginDescription;
    private String pluginClass;
    private String version;
    private String requires = "*"; // SemVer format
    private String provider;
    private List<PluginDependency> dependencies;
    private String license;

    public DefaultPluginDescriptor() {
        dependencies = new ArrayList<>();
    }

    /**
     * Returns the unique identifier of this plugin.
     */
    @Override
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Returns the description of this plugin.
     */
    @Override
    public String getPluginDescription() {
        return pluginDescription;
    }

    /**
     * Returns the name of the class that implements Plugin interface.
     */
    @Override
    public String getPluginClass() {
        return pluginClass;
    }

    /**
     * Returns the version of this plugin.
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Returns string version of requires
     * @return String with requires expression on SemVer format
     */
    @Override
    public String getRequires() {
        return requires;
    }

    /**
     * Returns the provider name of this plugin.
     */
    @Override
    public String getProvider() {
        return provider;
    }

    /**
     * Returns the legal license of this plugin, e.g. "Apache-2.0", "MIT" etc
     */
    @Override
    public String getLicense() {
        return license;
    }

    /**
     * Returns all dependencies declared by this plugin.
     * Returns an empty array if this plugin does not declare any require.
     */
    @Override
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

	DefaultPluginDescriptor setPluginId(String pluginId) {
        this.pluginId = pluginId;

        return this;
    }

    PluginDescriptor setPluginDescription(String pluginDescription) {
        this.pluginDescription = pluginDescription;

        return this;
    }

    PluginDescriptor setPluginClass(String pluginClassName) {
        this.pluginClass = pluginClassName;

        return this;
    }

    DefaultPluginDescriptor setPluginVersion(String version) {
        this.version = version;

        return this;
    }

    PluginDescriptor setProvider(String provider) {
        this.provider = provider;

        return this;
    }

    PluginDescriptor setRequires(String requires) {
        this.requires = requires;

        return this;
    }

    PluginDescriptor setDependencies(String dependencies) {
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

    	return this;
    }

    public PluginDescriptor setLicense(String license) {
        this.license = license;

        return this;
    }

}
