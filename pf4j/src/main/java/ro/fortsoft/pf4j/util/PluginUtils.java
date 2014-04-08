/*
 * Copyright 2014 Decebal Suiu
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
package ro.fortsoft.pf4j.util;

import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginState;
import ro.fortsoft.pf4j.PluginWrapper;

import java.util.List;

/**
 * @author Decebal Suiu
 */
public class PluginUtils {

    public static boolean isPluginStarted(PluginManager pluginManager, String pluginId) {
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        for (PluginWrapper plugin: startedPlugins) {
            if (pluginId.equals(plugin.getPluginId())) {
                return PluginState.STARTED == plugin.getPluginState();
            }
        }

        return false;
    }

}
