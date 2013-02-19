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

	private static final Logger log = LoggerFactory.getLogger(DependencyResolver.class);
	
    private List<PluginWrapper> plugins;

	public DependencyResolver(List<PluginWrapper> plugins) {
		this.plugins = plugins;
	}

	/**
	 * Get the list of plugins in dependency sorted order.
	 */
	public List<PluginWrapper> getSortedPlugins() throws PluginException {
		DirectedGraph<String> graph = new DirectedGraph<String>();
		for (PluginWrapper pluginWrapper : plugins) {
			PluginDescriptor descriptor = pluginWrapper.getDescriptor();
			String pluginId = descriptor.getPluginId();
			List<PluginDependency> dependencies = descriptor.getDependencies();
			if (!dependencies.isEmpty()) {
				for (PluginDependency dependency : dependencies) {
					graph.addEdge(pluginId, dependency.getPluginId());
				}
			} else {
				graph.addVertex(pluginId);
			}
		}

		log.debug("Graph: " + graph);
		List<String> pluginsId = graph.reverseTopologicalSort();

		if (pluginsId == null) {
			throw new CyclicDependencyException("Cyclic dependences !!!" + graph.toString());
		}

		log.debug("Plugins order: " + pluginsId);
		List<PluginWrapper> sortedPlugins = new ArrayList<PluginWrapper>();
		for (String pluginId : pluginsId) {
			sortedPlugins.add(getPlugin(pluginId));
		}

		return sortedPlugins;
	}

	private PluginWrapper getPlugin(String pluginId) throws PluginNotFoundException {
		for (PluginWrapper pluginWrapper : plugins) {
			if (pluginId.equals(pluginWrapper.getDescriptor().getPluginId())) {
				return pluginWrapper;
			}
		}

		throw new PluginNotFoundException(pluginId);
	}

}
