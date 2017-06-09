/*
 * Copyright 2017 Anindya Chatterjee
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
