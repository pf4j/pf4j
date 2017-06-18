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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.util.DirectedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DependencyResolver {

	private static final Logger log = LoggerFactory.getLogger(DependencyResolver.class);

    private List<PluginWrapper> plugins;
    private DirectedGraph<String> dependenciesGraph; // the value is 'pluginId'
    private DirectedGraph<String> dependentsGraph; // the value is 'pluginId'
    private boolean resolved;

	public void resolve(List<PluginWrapper> plugins) {
		this.plugins = plugins;

        initGraph();

        resolved = true;
	}

    /**
     * Retrieves the plugins ids that the given plugin id directly depends on.
     *
     * @param pluginId
     * @return
     */
    public List<String> getDependecies(String pluginId) {
	    checkResolved();

        return dependenciesGraph.getNeighbors(pluginId);
    }

    /**
     * Retrieves the plugins ids that the given content is a direct dependency of.
     *
     * @param pluginId
     * @return
     */
    public List<String> getDependents(String pluginId) {
	    checkResolved();

        return dependentsGraph.getNeighbors(pluginId);
    }

	/**
	 * Get the list of plugins in dependency sorted order.
	 */
	public List<PluginWrapper> getSortedPlugins() throws PluginException {
	    checkResolved();

		log.debug("Graph: {}", dependenciesGraph);
		List<String> pluginsIds = dependenciesGraph.reverseTopologicalSort();

		if (pluginsIds == null) {
			throw new PluginException("Cyclic dependencies !!! {}", dependenciesGraph.toString());
		}

		log.debug("Plugins order: {}", pluginsIds);
		List<PluginWrapper> sortedPlugins = new ArrayList<>();
		for (String pluginId : pluginsIds) {
			sortedPlugins.add(getPlugin(pluginId));
		}

		return sortedPlugins;
	}

    private void initGraph() {
        // create graphs
        dependenciesGraph = new DirectedGraph<>();
        dependentsGraph = new DirectedGraph<>();

        // populate graphs
        for (PluginWrapper pluginWrapper : plugins) {
            PluginDescriptor descriptor = pluginWrapper.getDescriptor();
            String pluginId = descriptor.getPluginId();
            List<PluginDependency> dependencies = descriptor.getDependencies();
            if (dependencies.isEmpty()) {
                dependenciesGraph.addVertex(pluginId);
                dependentsGraph.addVertex(pluginId);
            } else {
                for (PluginDependency dependency : dependencies) {
                    dependenciesGraph.addEdge(pluginId, dependency.getPluginId());
                    dependentsGraph.addEdge(dependency.getPluginId(), pluginId);
                }
            }
        }
    }

    private PluginWrapper getPlugin(String pluginId) throws PluginNotFoundException {
		for (PluginWrapper pluginWrapper : plugins) {
			if (pluginId.equals(pluginWrapper.getDescriptor().getPluginId())) {
				return pluginWrapper;
			}
		}

		throw new PluginNotFoundException(pluginId);
	}

	private void checkResolved() {
	    if (!resolved) {
	        throw new IllegalStateException("Call 'resolve' method first");
        }
    }

}
