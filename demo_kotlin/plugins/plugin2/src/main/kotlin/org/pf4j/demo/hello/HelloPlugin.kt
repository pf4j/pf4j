package org.pf4j.demo.hello

import org.apache.commons.lang3.StringUtils
import org.pf4j.Extension
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import org.pf4j.kotlindemo.api.Greeting
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HelloPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun start() {
        logger.info("HelloPlugin.start()")
        logger.info(StringUtils.upperCase("HelloPlugin"))
    }

    override fun stop() {
        logger.info("HelloPlugin.stop()")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(HelloPlugin::class.java)
    }

    @Extension
    class WelcomeGreeting : Greeting {
        override val greeting: String
            get() = "Hello"

        override fun greetPerson(person: String): String {
            return "$greeting $person"
        }
    }

}


