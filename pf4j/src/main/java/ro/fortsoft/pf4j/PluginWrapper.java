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
	PluginState pluginState;
	RuntimeMode runtimeMode;

	public PluginWrapper(PluginDescriptor descriptor, String pluginPath, PluginClassLoader pluginClassLoader) {
		this.descriptor = descriptor;
		this.pluginPath = pluginPath;
		this.pluginClassLoader = pluginClassLoader;

		// TODO
		try {
			plugin = createPluginInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}

		pluginState = PluginState.CREATED;
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

	public PluginState getPluginState() {
		return pluginState;
	}

	public RuntimeMode getRuntimeMode() {
		return runtimeMode;
	}

    /**
     * Shortcut
     */
    public String getPluginId() {
        return getDescriptor().getPluginId();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + descriptor.getPluginId().hashCode();

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		PluginWrapper other = (PluginWrapper) obj;
		if (!descriptor.getPluginId().equals(other.descriptor.getPluginId())) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "PluginWrapper [descriptor=" + descriptor + ", pluginPath="
				+ pluginPath + "]";
	}

	void setPluginState(PluginState pluginState) {
		this.pluginState = pluginState;
	}

	void setRuntimeMode(RuntimeMode runtimeMode) {
		this.runtimeMode = runtimeMode;
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

}
