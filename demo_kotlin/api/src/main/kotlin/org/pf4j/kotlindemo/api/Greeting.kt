package org.pf4j.kotlindemo.api

import org.pf4j.ExtensionPoint

/**
 * Greeting is the extension point for plugins.
 */
interface Greeting : ExtensionPoint {
    // a value with default implementation
    val greeting: String
        get() = "Hello World!"

    fun greetPerson(person: String = "John Doe"): String
}
