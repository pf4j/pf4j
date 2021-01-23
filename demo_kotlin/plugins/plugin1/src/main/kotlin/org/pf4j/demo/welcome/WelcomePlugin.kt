package org.pf4j.demo.welcome

import org.apache.commons.lang3.StringUtils
import org.pf4j.Extension
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import org.pf4j.kotlindemo.api.Greeting
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class WelcomePlugin(wrapper: PluginWrapper) : Plugin(wrapper) {
    override fun start() {
        logger.info("WelcomePlugin.start()")
        logger.info(StringUtils.upperCase("WelcomePlugin"))
    }

    override fun stop() {
        logger.info("WelcomePlugin.stop()")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(WelcomePlugin::class.java)
    }

    @Extension
    class WelcomeGreeting : Greeting {
        override val greeting: String
            get() = "Welcome"

        override fun greetPerson(person: String): String {
            return "$greeting $person"
        }
    }

}


