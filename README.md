<img src="pf4j-logo.svg" width="250"/>

Plugin Framework for Java (PF4J)
=====================
[![Join the chat at https://gitter.im/decebals/pf4j](https://badges.gitter.im/decebals/pf4j.svg)](https://gitter.im/decebals/pf4j?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![GitHub Actions Status](https://github.com/pf4j/pf4j/actions/workflows/build.yml/badge.svg)](https://github.com/pf4j/pf4j/actions/workflows/build.yml)
[![Coverage Status](https://coveralls.io/repos/pf4j/pf4j/badge.svg?branch=master&service=github)](https://coveralls.io/github/pf4j/pf4j?branch=master)
[![Maven Central](http://img.shields.io/maven-central/v/org.pf4j/pf4j.svg)](http://search.maven.org/#search|ga|1|pf4j)

A plugin is a way for a third party to extend the functionality of an application. A plugin implements extension points
declared by application or other plugins. Also, a plugin can define extension points.  

**NOTE:** Starting with version 0.9 you can define an extension directly in the application jar (you're not obligated to put the extension in a plugin - you can see this extension as a default/system extension). See [WhazzupGreeting](https://github.com/pf4j/pf4j/blob/master/demo/app/src/main/java/org/pf4j/demo/WhazzupGreeting.java) for a real example.  

Features/Benefits
-------------------
With PF4J you can easily transform a monolithic java application in a modular application.  
PF4J is an open source (Apache license) lightweight (around __100 KB__) plugin framework for java, with minimal dependencies (only slf4j-api) and very extensible (see `PluginDescriptorFinder` and `ExtensionFinder`).   

Practically, PF4J is a microframework that aims to keep the core simple but extensible. We also have a community-driven ecosystem of extensions.
For now are available these extensions:
- [pf4j-update](https://github.com/pf4j/pf4j-update) (update mechanism for PF4J)
- [pf4j-spring](https://github.com/pf4j/pf4j-spring) (PF4J - Spring Framework integration)
- [pf4j-wicket](https://github.com/pf4j/pf4j-wicket) (PF4J - Wicket integration)
- [pf4j-web](https://github.com/pf4j/pf4j-web) (PF4J in web applications)

No XML, only Java.

You can mark any interface or abstract class as an extension point (with marker interface ExtensionPoint) and you specified that an class is an extension with @Extension annotation.

Also, PF4J can be used in web applications. For example web applications that require want modularity can use [pf4j-wicket](https://github.com/pf4j/pf4j-wicket).

Components
-------------------
- **Plugin** is the base class for all plugins types. Each plugin is loaded into a separate class loader to avoid conflicts.
- **PluginManager** is used for all aspects of plugins management (loading, starting, stopping). You can use a built-in implementation as `JarPluginManager`, `ZipPluginManager`, `DefaultPluginManager` (it's a `JarPluginManager` + `ZipPluginManager`) or you can implement a custom plugin manager starting from `AbstractPluginManager` (implement only factory methods).
- **PluginLoader** loads all information (classes) needed by a plugin.
- **ExtensionPoint** is a point in the application where custom code can be invoked. It's a java interface marker.   
Any java interface or abstract class can be marked as an extension point (implements `ExtensionPoint` interface).
- **Extension** is an implementation of an extension point. It's a java annotation on a class.

**PLUGIN** = a container for **EXTENSION POINTS** and **EXTENSIONS** + lifecycle methods (start, stop, delete)

A **PLUGIN** is similar with a **MODULE** from other systems. If you don't need lifecycle methods (hook methods for start, stop, delete) you are not forced to supply a plugin class (the `PluginClass` property from the plugin descriptor is optional). You only need to supply some description of plugin (id, version, author, ...) for a good tracking (your application wants to know who supplied the extensions or extensions points).

How to use
-------------------
It's very simple to add pf4j in your application.

Define an extension point in your application/plugin using **ExtensionPoint** interface marker:

```java
public interface Greeting extends ExtensionPoint {

    String getGreeting();

}
```

Create an extension using `@Extension` annotation:
 
```java
@Extension
public class WelcomeGreeting implements Greeting {

    public String getGreeting() {
        return "Welcome";
    }

}
```

Create (it's __optional__) a `Plugin` class if you are interested in plugin's lifecycle events (start, stop, ...):

```java
public class WelcomePlugin extends Plugin {

    @Override
    public void start() {
        System.out.println("WelcomePlugin.start()");
    }

    @Override
    public void stop() {
        System.out.println("WelcomePlugin.stop()");
    }
    
    @Override
    public void delete() {
        System.out.println("WelcomePlugin.delete()");
    }
    
}
```

In above code we've been created a plugin (welcome) that comes with one extension for the `Greeting` extension point.

You can distribute your plugin as a jar file (the simple solution). In this case add the plugin's metadata in `MANIFEST.MF` file of jar:

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

In the manifest above, we've described a plugin with the id of `welcome-plugin` (mandatory attribute). We've also defined a class `org.pf4j.demo.welcome.WelcomePlugin` (optional attribute), with version `0.0.1` (mandatory attribute) and with dependencies to plugins `x, y, z` (optional attribute).

Now you can play with plugins and extensions in your code:

```java
public static void main(String[] args) {
    ...

    // create the plugin manager
    PluginManager pluginManager = new JarPluginManager(); // or "new ZipPluginManager() / new DefaultPluginManager()"
    
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
Documentation is available on [pf4j.org](http://pf4j.org)

Demo
---------------
Demo applications are available in [demo](https://github.com/pf4j/pf4j/tree/master/demo) folder

Quickstart (call to action)
---------------
1. Read this file to have an overview about what this project does
2. Read [Getting started](https://pf4j.org/doc/getting-started.html) section of documentation to understand the basic concepts
3. Read [Quickstart](https://pf4j.org/dev/quickstart.html) section of documentation to create your first PF4J-based modular application
