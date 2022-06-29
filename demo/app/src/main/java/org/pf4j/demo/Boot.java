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
import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.demo.api.Greeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
public class Boot {

    private static final Logger log = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) {
        // print logo
        printLogo();

        // create the plugin manager
        PluginManager pluginManager = createPluginManager();

        // load the plugins
        pluginManager.loadPlugins();

        // enable a disabled plugin
//        pluginManager.enablePlugin("welcome-plugin");

        // start (active/resolved) the plugins
        pluginManager.startPlugins();

        // retrieves the extensions for Greeting extension point
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        log.info("Found {} extensions for extension point '{}'", greetings.size(), Greeting.class.getName());
        for (Greeting greeting : greetings) {
            log.info(">>> {}", greeting.getGreeting());
        }

        // print extensions from classpath (non plugin)
        log.info("Extensions added by classpath:");
        Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
        for (String extension : extensionClassNames) {
            log.info("   {}", extension);
        }

        log.info("Extension classes by classpath:");
        List<Class<? extends Greeting>> greetingsClasses = pluginManager.getExtensionClasses(Greeting.class);
        for (Class<? extends Greeting> greeting : greetingsClasses) {
            log.info("   Class: {}", greeting.getCanonicalName());
        }

        // print extensions ids for each started plugin
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        for (PluginWrapper plugin : startedPlugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            log.info("Extensions added by plugin '{}}':", pluginId);
            extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
            for (String extension : extensionClassNames) {
                log.info("   {}", extension);
            }
        }

        // print the extensions instances for Greeting extension point for each started plugin
        for (PluginWrapper plugin : startedPlugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            log.info("Extensions instances added by plugin '{}' for extension point '{}':", pluginId, Greeting.class.getName());
            List<Greeting> extensions = pluginManager.getExtensions(Greeting.class, pluginId);
            for (Object extension : extensions) {
                log.info("   {}", extension);
            }
        }

        // print extensions instances from classpath (non plugin)
        log.info("Extensions instances added by classpath:");
        List<?> extensions = pluginManager.getExtensions((String) null);
        for (Object extension : extensions) {
            log.info("   {}", extension);
        }

        // print extensions instances for each started plugin
        for (PluginWrapper plugin : startedPlugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            log.info("Extensions instances added by plugin '{}':", pluginId);
            extensions = pluginManager.getExtensions(pluginId);
            for (Object extension : extensions) {
                log.info("   {}", extension);
            }
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
        log.info(StringUtils.repeat("#", 40));
        log.info(StringUtils.center("PF4J-DEMO", 40));
        log.info(StringUtils.repeat("#", 40));
    }

    private static PluginManager createPluginManager() {
        return new DefaultPluginManager() {

            @Override
            protected ExtensionFinder createExtensionFinder() {
                DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
                extensionFinder.addServiceProviderExtensionFinder(); // to activate "HowdyGreeting" extension

                return extensionFinder;
            }

        };
    }

}
