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
 * A plugin descriptor contains information about a plug-in obtained
 * from the manifest (META-INF) file.
 *
 * @author Decebal Suiu
 */
public class PluginDescriptor {

	private String pluginId;
    private String pluginClass;
    private PluginVersion version;
    private String provider;
    private List<PluginDependency> dependencies;
    private PluginClassLoader pluginClassLoader;

    public PluginDescriptor() {
        dependencies = new ArrayList<PluginDependency>();
    }

    /**
     * Returns the unique identifier of this plugin.
     */
    public String getPluginId() {
        return pluginId;
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
    public PluginVersion getVersion() {
        return version;
    }

    /**
     * Returns the provider name of this plugin.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Returns all dependencies declared by this plugin.
     * Returns an empty array if this plugin does not declare any require.
     */
    public List<PluginDependency> getDependencies() {
        return dependencies;
    }

    /**
     * Returns the plugin class loader used to load classes and resources
	 * for this plug-in. The class loader can be used to directly access
	 * plug-in resources and classes.
	 */
    public PluginClassLoader getPluginClassLoader() {
    	return pluginClassLoader;
    }

    @Override
	public String toString() {
		return "PluginDescriptor [pluginId=" + pluginId + ", pluginClass="
				+ pluginClass + ", version=" + version + ", provider="
				+ provider + ", dependencies=" + dependencies
				+ ", pluginClassLoader=" + pluginClassLoader + "]";
	}

	void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    void setPluginClass(String pluginClassName) {
        this.pluginClass = pluginClassName;
    }

    void setPluginVersion(PluginVersion version) {
        this.version = version;
    }

    void setProvider(String provider) {
        this.provider = provider;
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

	void setPluginClassLoader(PluginClassLoader pluginClassLoader) {
		this.pluginClassLoader = pluginClassLoader;
	}

}
