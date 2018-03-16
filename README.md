<img src="pf4j-logo.svg" width="250"/>

Plugin Framework for Java (PF4J)
=====================
[![Join the chat at https://gitter.im/decebals/pf4j](https://badges.gitter.im/decebals/pf4j.svg)](https://gitter.im/decebals/pf4j?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Travis CI Build Status](https://travis-ci.org/decebals/pf4j.png)](https://travis-ci.org/decebals/pf4j)
[![Coverage Status](https://coveralls.io/repos/decebals/pf4j/badge.svg?branch=master&service=github)](https://coveralls.io/github/decebals/pf4j?branch=master)
[![Maven Central](http://img.shields.io/maven-central/v/org.pf4j/pf4j.svg)](http://search.maven.org/#search|ga|1|pf4j)

A plugin is a way for a third party to extend the functionality of an application. A plugin implements extension points
declared by application or other plugins. Also a plugin can define extension points.  

**NOTE:** Starting with version 0.9 you can define an extension directly in the application jar (you're not obligated to put the extension in a plugin - you can see this extension as a default/system extension). See [WhazzupGreeting](https://github.com/decebals/pf4j/blob/master/demo/app/src/main/java/org/pf4j/demo/WhazzupGreeting.java) for a real example.  

Features/Benefits
-------------------
With PF4J you can easily transform a monolithic java application in a modular application.  
PF4J is an open source (Apache license) lightweight (around __50 KB__) plugin framework for java, with minimal dependencies (only slf4j-api) and very extensible (see PluginDescriptorFinder and ExtensionFinder).   

Practically PF4J is a microframework and the aim is to keep the core simple but extensible. I try to create a little ecosystem (extensions) based on this core with the help of the comunity.  
For now are available these extensions:
- [pf4j-update](https://github.com/decebals/pf4j-update) (update mechanism for PF4J)
- [pf4j-spring](https://github.com/decebals/pf4j-spring) (PF4J - Spring Framework integration)
- [pf4j-web](https://github.com/rmrodrigues/pf4j-web) (PF4J in web applications)
- [wicket-plugin](https://github.com/decebals/wicket-plugin) (Wicket Plugin Framework based on PF4J)

No XML, only Java.

You can mark any interface or abstract class as an extension point (with marker interface ExtensionPoint) and you specified that an class is an extension with @Extension annotation.

Also, PF4J can be used in web applications. For my web applications when I want modularity I use [Wicket Plugin](https://github.com/decebals/wicket-plugin).

Components
-------------------
- **Plugin** is the base class for all plugins types. Each plugin is loaded into a separate class loader to avoid conflicts.
- **PluginManager** is used for all aspects of plugins management (loading, starting, stopping). You can use a built-in implementation as `DefaultPluginManager`, `JarPluginManager` or you can implement a custom plugin manager starting from `AbstractPluginManager` (implement only factory methods).
- **PluginLoader** loads all information (classes) needed by a plugin.
- **ExtensionPoint** is a point in the application where custom code can be invoked. It's a java interface marker.   
Any java interface or abstract class can be marked as an extension point (implements `ExtensionPoint` interface).
- **Extension** is an implementation of an extension point. It's a java annotation on a class.

How to use
-------------------
It's very simple to add pf4j in your application.

Define an extension point in your application using **ExtensionPoint** interface marker:

```java
public interface Greeting extends ExtensionPoint {

    String getGreeting();

}
```

Create a plugin that contribute with an extension:
 
```java
public class WelcomePlugin extends Plugin {

    public WelcomePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class WelcomeGreeting implements Greeting {

        public String getGreeting() {
            return "Welcome";
        }

    }

}
```

In above code I created a plugin that comes with one extension for the `Greeting` extension point.

You can distribute you plugin as a jar file (the simple solution). In this case add the plugin's metadata in `MANIFEST.MF` file of jar:

```
Manifest-Version: 1.0
Archiver-Version: Plexus Archiver
Created-By: Apache Maven
Built-By: decebal
Build-Jdk: 1.6.0_17
Plugin-Class: org.pf4j.demo.welcome.WelcomePlugin
Plugin-Dependencies: x, y, z
Plugin-Id: welcome-plugin
Plugin-Provider: Decebal Suiu
Plugin-Version: 0.0.1
```

In above manifest I described a plugin with id `welcome-plugin`, with class `org.pf4j.demo.welcome.WelcomePlugin`, with version `0.0.1` and with dependencies
to plugins `x, y, z`.

Now you can play with plugins and extensions in your code:

```java
public static void main(String[] args) {
    ...

    // create the plugin manager
    PluginManager pluginManager = new DefaultPluginManager();
    
    // start and load all plugins of application
    pluginManager.loadPlugins();
    pluginManager.startPlugins();

    // retrieve all extensions for "Greeting" extension point
    List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
    for (Greeting greeting : greetings) {
        System.out.println(">>> " + greeting.getGreeting());
    }
    
    // stop and unload all plugins
    pluginManager.stopPlugins();
    pluginManager.unloadPlugins();
    
    ...
}
```

The output is:

```
>>> Welcome
```

PF4J is very customizable and comes with a lot of goodies. Please read the documentation to discover yourself the power of this library.

Documentation
---------------
Documentation is available on [pf4j.org](http://www.pf4j.org)

Demo
---------------
Demo applications are available in [demo](https://github.com/decebals/pf4j/tree/master/demo) folder
