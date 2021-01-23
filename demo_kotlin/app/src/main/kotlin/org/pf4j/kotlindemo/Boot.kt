package org.pf4j.kotlindemo

import org.apache.commons.lang3.StringUtils
import org.pf4j.CompoundPluginDescriptorFinder
import org.pf4j.DefaultPluginManager
import org.pf4j.ManifestPluginDescriptorFinder
import org.pf4j.PluginWrapper
import org.pf4j.kotlindemo.api.Greeting
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
    Boot.runDemo()
}

/**
 * A boot class that start the demo.
 *
 * @author Tobias Watzl
 */
object Boot {
    // we wrap the demo method in a object so we have something the logger can refer to
    private val logger: Logger = LoggerFactory.getLogger(Boot::class.java)

    private class PluginManager(importPaths: List<Path>) : DefaultPluginManager(importPaths) {
        override fun createPluginDescriptorFinder(): CompoundPluginDescriptorFinder {
            return CompoundPluginDescriptorFinder() // Demo is using the Manifest file
                // PropertiesPluginDescriptorFinder is commented out just to avoid error log
                //.add(PropertiesPluginDescriptorFinder())
                .add(ManifestPluginDescriptorFinder());
        }
    }

    fun runDemo() {
        val greetedPersons = listOf("Alice", "Bob", "Trudy")

        // print logo
        printLogo()

        val pluginsDir = System.getProperty("pf4j.pluginsDir", "./plugins")
        logger.info("Plugin directory: $pluginsDir")
        // create the plugin manager
        val pluginManager = PluginManager(listOf(Paths.get(pluginsDir)))

        // load the plugins
        pluginManager.loadPlugins()

        // enable a disabled plugin
        // pluginManager.enablePlugin("welcome-plugin")

        // start (active/resolved) the plugins
        pluginManager.startPlugins()

        // retrieves the extensions for Greeting extension point
        val greetings: List<Greeting> = pluginManager.getExtensions(Greeting::class.java)
        logger.info(
            String.format(
                "Found %d extensions for extension point '%s'",
                greetings.size,
                Greeting::class.java.name
            )
        )
        greetings.forEach { greeting ->
            logger.info(">>> ${greeting.greeting}")
            greetedPersons.forEach { person ->
                logger.info("\t>>> ${greeting.greetPerson(person)}")
            }
        }

        // // print extensions from classpath (non plugin)
        // logger.info(String.format("Extensions added by classpath:"))
        // val extensionClassNames = pluginManager.getExtensionClassNames(null)
        // for (extension in extensionClassNames) {
        //     logger.info("   $extension")
        // }

        // print extensions for each started plugin
        val startedPlugins: List<PluginWrapper> = pluginManager.startedPlugins
        startedPlugins.forEach { plugin ->
            val pluginId: String = plugin.descriptor.pluginId
            logger.info(String.format("Extensions added by plugin '%s':", pluginId))
            // val extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
            //     extensionClassNames.forEach { extension ->
            //     logger.info("   $extension");
            // }
        }

        // stop the plugins
        pluginManager.stopPlugins()

        // Runtime.getRuntime().addShutdownHook(object : Thread() {
        //     override fun run() {
        //         pluginManager.stopPlugins()
        //     }
        // })
    }

    private fun printLogo() {
        logger.info(StringUtils.repeat("#", 40))
        logger.info(StringUtils.center("PF4J-DEMO", 40))
        logger.info(StringUtils.repeat("#", 40))
    }
}
