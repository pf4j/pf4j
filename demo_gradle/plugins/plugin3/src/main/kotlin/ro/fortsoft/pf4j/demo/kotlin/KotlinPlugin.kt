package ro.fortsoft.pf4j.demo.kotlin

import org.apache.commons.lang3.StringUtils
import ro.fortsoft.pf4j.Extension
import ro.fortsoft.pf4j.Plugin
import ro.fortsoft.pf4j.PluginWrapper
import ro.fortsoft.pf4j.demo.api.Greeting

/**
 * A sample plugin written in Kotlin
 *
 * @author Anindya Chatterjee (anidotnet)
 */
class KotlinPlugin(wrapper: PluginWrapper) : Plugin(wrapper) {

    override fun start() {
        println("KotlinPlugin.start()")
        println(StringUtils.upperCase("KotlinPlugin"))
    }

    override fun stop() {
        println("KotlinPlugin.stop()")
    }
}

@Extension
class KotlinGreeting : Greeting {
    override fun getGreeting(): String {
        return "KotlinGreetings"
    }
}
