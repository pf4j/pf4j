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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.fortsoft.pf4j.util.DirectedGraph;


/**
 * @author Decebal Suiu
 */
class DependencyResolver {

	private static final Logger LOG = LoggerFactory.getLogger(DependencyResolver.class);
	
    private List<Plugin> plugins;

	public DependencyResolver(List<Plugin> plugins) {
		this.plugins = plugins;
	}

	/**
	 * Get the list of plugins in dependency sorted order.
	 */
	public List<Plugin> getSortedDependencies() {
		DirectedGraph<String> graph = new DirectedGraph<String>();
		for (Plugin plugin : plugins) {
			PluginDescriptor descriptor = plugin.getWrapper().getDescriptor();
			String pluginId = descriptor.getPluginId();
			List<String> dependencies = descriptor.getDependencies();
			if (!dependencies.isEmpty()) {
				for (String dependency : dependencies) {
					graph.addEdge(pluginId, dependency);
				}
			} else {
				graph.addVertex(pluginId);
			}
		}

		LOG.debug("Graph: " + graph);
		List<String> pluginsId = graph.reverseTopologicalSort();

		if (pluginsId == null) {
			LOG.error("Cyclic dependences !!!");
			return null;
		}

		LOG.debug("Plugins order: " + pluginsId);
		List<Plugin> sortedPlugins = new ArrayList<Plugin>();
		for (String pluginId : pluginsId) {
			sortedPlugins.add(getPlugin(pluginId));
		}

		return sortedPlugins;
	}

	private Plugin getPlugin(String pluginId) {
		for (Plugin plugin : plugins) {
			if (pluginId.equals(plugin.getWrapper().getDescriptor().getPluginId())) {
				return plugin;
			}
		}

		return null;
	}

}
