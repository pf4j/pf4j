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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A wrapper over plugin instance.
 *
 * @author Decebal Suiu
 */
public class PluginWrapper {

	PluginDescriptor descriptor;
	String pluginPath;
	PluginClassLoader pluginClassLoader;
	Plugin plugin;
	
	public PluginWrapper(PluginDescriptor descriptor, String pluginPath, PluginClassLoader pluginClassLoader) {
		this.descriptor = descriptor;
		this.pluginPath = pluginPath;
		this.pluginClassLoader = pluginClassLoader;
		
		// TODO
		try {
			plugin = createPluginInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    /**
     * Returns the plugin descriptor.
     */
    public PluginDescriptor getDescriptor() {
    	return descriptor;
    }

    /**
     * Returns the path of this plugin relative to plugins directory.
     */
    public String getPluginPath() {
    	return pluginPath;
    }

    /**
     * Returns the plugin class loader used to load classes and resources
	 * for this plug-in. The class loader can be used to directly access
	 * plug-in resources and classes.
	 */
    public PluginClassLoader getPluginClassLoader() {
    	return pluginClassLoader;
    }

    public Plugin getPlugin() {
		return plugin;
	}

	private Plugin createPluginInstance() throws Exception {
    	String pluginClassName = descriptor.getPluginClass();
        Class<?> pluginClass = pluginClassLoader.loadClass(pluginClassName);

        // once we have the class, we can do some checks on it to ensure
        // that it is a valid implementation of a plugin.
        int modifiers = pluginClass.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)
                || (!Plugin.class.isAssignableFrom(pluginClass))) {
            throw new PluginException("The plugin class '" + pluginClassName + "' is not compatible.");
        }

        // create the plugin instance
        Constructor<?> constructor = pluginClass.getConstructor(new Class[] { PluginWrapper.class });
        Plugin plugin = (Plugin) constructor.newInstance(new Object[] { this });

        return plugin;
    }

    @Override
	public int hashCode() {
    	return new HashCodeBuilder().append(descriptor.getPluginId())
    		.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PluginWrapper)) {
			return false;
		}
		
		PluginWrapper wrapper = (PluginWrapper) obj;
		
		return new EqualsBuilder().append(descriptor.getPluginId(), wrapper.getDescriptor().getPluginId())
			.isEquals();
	}

	@Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("descriptor", descriptor)
            .append("pluginPath", pluginPath)
            .append("plugin", plugin)
            .toString();
    }

}
