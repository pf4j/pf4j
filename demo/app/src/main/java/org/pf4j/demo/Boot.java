/*
 * Copyright (C) 2012-present the original author or authors.
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

import org.apache.commons.lang.StringUtils;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.demo.api.Greeting;

import java.util.ArrayList;
import java.util.List;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
public class Boot {

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

        // retrieves the extensions for Greeting extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        System.out.println(String.format("Found %d extensions for extension point '%s'", greetings.size(), Greeting.class.getName()));
        for (Greeting greeting : greetings) {
            System.out.println(">>> " + greeting.getGreeting());
        }

        // print extensions from classpath (non plugin)
        System.out.println("Extensions added by classpath:");
        printIndented(pluginManager.getExtensionClassNames(null));

        System.out.println("Extension classes by classpath:");
        final List<String> classesNames = classesNamesForPrint(pluginManager.getExtensionClasses(Greeting.class));
        printIndented(classesNames);

        // print extensions ids for each started plugin
        for (PluginWrapper plugin : pluginManager.getStartedPlugins()) {
            String pluginId = plugin.getDescriptor().getPluginId();
            System.out.println(String.format("Extensions added by plugin '%s':", pluginId));
            printIndented(pluginManager.getExtensionClassNames(pluginId));
        }

        // print the extensions instances for Greeting extension point for each started plugin
        for (PluginWrapper plugin : pluginManager.getStartedPlugins()) {
            String pluginId = plugin.getDescriptor().getPluginId();
            System.out.println(String.format("Extensions instances added by plugin '%s' for extension point '%s':", pluginId, Greeting.class.getName()));
            printIndented(pluginManager.getExtensions(Greeting.class, pluginId));
        }

        // print extensions instances from classpath (non plugin)
        System.out.println("Extensions instances added by classpath:");
        printIndented(pluginManager.getExtensions((String) null));

        // print extensions instances for each started plugin
        for (PluginWrapper plugin : pluginManager.getStartedPlugins()) {
            String pluginId = plugin.getDescriptor().getPluginId();
            System.out.println(String.format("Extensions instances added by plugin '%s':", pluginId));
            printIndented(pluginManager.getExtensions(pluginId));
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

    private static List<String> classesNamesForPrint(List<Class<Greeting>> classes) {
        List<String> classesNames = new ArrayList<>();
        for (Class<Greeting> greeting : classes) {
            classesNames.add("Class: " + greeting.getCanonicalName());
        }
        return classesNames;
    }

    private static <T> void printIndented(Iterable<T> strings) {
        for (T item : strings) {
            System.out.println("   " + item);
        }
    }

    private static void printLogo() {
    	System.out.println(StringUtils.repeat("#", 40));
    	System.out.println(StringUtils.center("PF4J-DEMO", 40));
    	System.out.println(StringUtils.repeat("#", 40));
	}

}
