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
package org.pf4j.demo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.demo.api.Greeting;

import java.util.List;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
public class Boot {
    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) {
        // print logo
        printLogo();

        // create the plugin manager
        final PluginManager pluginManager = new DefaultPluginManager();

        // load the plugins
        pluginManager.loadPlugins();

        // enable a disabled plugin
//        pluginManager.enablePlugin("welcome-plugin");

        // start (active/resolved) the plugins
        pluginManager.startPlugins();

        logger.info("Plugindirectory: ");
        logger.info("\t" + System.getProperty("pf4j.pluginsDir", "plugins") + "\n");

        // retrieves the extensions for Greeting extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        logger.info(String.format("Found %d extensions for extension point '%s'", greetings.size(), Greeting.class.getName()));
        for (Greeting greeting : greetings) {
            logger.info(">>> " + greeting.getGreeting());
        }

        // // print extensions from classpath (non plugin)
        // logger.info(String.format("Extensions added by classpath:"));
        // Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
        // for (String extension : extensionClassNames) {
        //     logger.info("   " + extension);
        // }

        // print extensions for each started plugin
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        for (PluginWrapper plugin : startedPlugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            logger.info(String.format("Extensions added by plugin '%s':", pluginId));
            // extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
            // for (String extension : extensionClassNames) {
            //     logger.info("   " + extension);
            // }
        }

        // stop the plugins
        pluginManager.stopPlugins();
        /*
        Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				pluginManager.stopPlugins();
			}

        });
        */
    }

	private static void printLogo() {
    	logger.info(StringUtils.repeat("#", 40));
    	logger.info(StringUtils.center("PF4J-DEMO", 40));
    	logger.info(StringUtils.repeat("#", 40));
	}

}
