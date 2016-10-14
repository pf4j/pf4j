Plugin Framework for Java (PF4J)
=====================
[![Join the chat at https://gitter.im/decebals/pf4j](https://badges.gitter.im/decebals/pf4j.svg)](https://gitter.im/decebals/pf4j?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Travis CI Build Status](https://travis-ci.org/decebals/pf4j.png)](https://travis-ci.org/decebals/pf4j)
[![Coverage Status](https://coveralls.io/repos/decebals/pf4j/badge.svg?branch=master&service=github)](https://coveralls.io/github/decebals/pf4j?branch=master)
[![Maven Central](http://img.shields.io/maven-central/v/ro.fortsoft.pf4j/pf4j.svg)](http://search.maven.org/#search|ga|1|pf4j)

A plugin is a way for a third party to extend the functionality of an application. A plugin implements extension points
declared by application or other plugins. Also a plugin can define extension points.  

**NOTE:** Starting with version 0.9 you can define an extension directly in the application jar (you're not obligated to put the extension in a plugin - you can see this extension as a default/system extension). See [WhazzupGreeting](https://github.com/decebals/pf4j/blob/master/demo/app/src/main/java/ro/fortsoft/pf4j/demo/WhazzupGreeting.java) for a real example.  

Features/Benefits
-------------------
With PF4J you can easily transform a monolithic java application in a modular application.  
PF4J is an open source (Apache license) lightweight (around 50KB) plugin framework for java, with minimal dependencies (only slf4j-api) and very extensible (see PluginDescriptorFinder and ExtensionFinder).   

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
- **PluginManager** is used for all aspects of plugins management (loading, starting, stopping).
- **ExtensionPoint** is a point in the application where custom code can be invoked. It's a java interface marker.   
Any java interface or abstract class can be marked as an extension point (implements _ExtensionPoint_ interface).
- **Extension** is an implementation of an extension point. It's a java annotation on a class.

Artifacts
-------------------
- PF4J `pf4j` (jar)
- PF4J Demo `pf4j-demo` (executable jar)

Using Maven
-------------------
In your pom.xml you must define the dependencies to PF4J artifacts with:

```xml
<dependency>
    <groupId>ro.fortsoft.pf4j</groupId>
    <artifactId>pf4j</artifactId>
    <version>${pf4j.version}</version>
</dependency>    
```

where ${pf4j.version} is the last pf4j version.

You may want to check for the latest released version using [Maven Search](http://search.maven.org/#search%7Cga%7C1%7Cpf4j)

Also you can use the latest SNAPSHOT via the Sonatype Maven Repository. For this, you must add above lines in your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>sonatype-nexus-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

How to use
-------------------
It's very simple to add pf4j in your application:

```java
public static void main(String[] args) {
    ...
    
    PluginManager pluginManager = new DefaultPluginManager();
    pluginManager.loadPlugins();
    pluginManager.startPlugins();

    ...
}
```

In above code, I created a **DefaultPluginManager** (it's the default implementation for
**PluginManager** interface) that loads and starts all active(resolved) plugins.  
Each available plugin is loaded using a different java class loader, **PluginClassLoader**.   
The **PluginClassLoader** contains only classes found in **PluginClasspath** (default _classes_ and _lib_ folders) of plugin and runtime classes and libraries of the required/dependent plugins. This class loader is a _Parent Last ClassLoader_ - it loads the classes from the plugin's jars before delegating to the parent class loader.   
The plugins are stored in a folder. You can specify the plugins folder in the constructor of DefaultPluginManager. If the plugins folder is not specified 
than the location is returned by `System.getProperty("pf4j.pluginsDir", "plugins")`.

The structure of plugins folder is:
* plugin1.zip (or plugin1 folder)
* plugin2.zip (or plugin2 folder)

In plugins folder you can put a plugin as folder or archive file (zip).
A plugin folder has this structure by default:
* `classes` folder
* `lib` folder (optional - if the plugin used third party libraries)

The plugin manager searches plugins metadata using a **PluginDescriptorFinder**.   
**DefaultPluginDescriptorFinder** is a "link" to **ManifestPluginDescriptorFinder** that lookups plugins descriptors in MANIFEST.MF file.
In this case the `classes/META-INF/MANIFEST.MF` file looks like:

```
Manifest-Version: 1.0
Archiver-Version: Plexus Archiver
Created-By: Apache Maven
Built-By: decebal
Build-Jdk: 1.6.0_17
Plugin-Class: ro.fortsoft.pf4j.demo.welcome.WelcomePlugin
Plugin-Dependencies: x, y, z
Plugin-Id: welcome-plugin
Plugin-Provider: Decebal Suiu
Plugin-Version: 0.0.1
```

In above manifest I described a plugin with id `welcome-plugin`, with class `ro.fortsoft.pf4j.demo.welcome.WelcomePlugin`, with version `0.0.1` and with dependencies 
to plugins `x, y, z`.

You can define an extension point in your application using **ExtensionPoint** interface marker.

```java
public interface Greeting extends ExtensionPoint {

    public String getGreeting();

}
```

Another important internal component is **ExtensionFinder** that describes how the plugin manager discovers extensions for the extensions points.   
**DefaultExtensionFinder** looks up extensions using **Extension** annotation.   
DefaultExtensionFinder looks up extensions in all extensions index files `META-INF/extensions.idx`. PF4J uses Java Annotation Processing to process at compile time all classes annotated with @Extension and to produce the extensions index file.

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

In above code I supply an extension for the `Greeting` extension point.

You can retrieve all extensions for an extension point with:

```java
List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
for (Greeting greeting : greetings) {
    System.out.println(">>> " + greeting.getGreeting());
}
```

The output is:

```
>>> Welcome
>>> Hello
```

You can inject your custom component (for example PluginDescriptorFinder, ExtensionFinder, PluginClasspath, ...) in DefaultPluginManager just override `create...` methods (factory method pattern).

Example:

```java
protected PluginDescriptorFinder createPluginDescriptorFinder() {
    return new PropertiesPluginDescriptorFinder();
}
```
    
and in plugin repository you must have a plugin.properties file with the below content:

```
plugin.class=ro.fortsoft.pf4j.demo.welcome.WelcomePlugin
plugin.dependencies=x, y, z
plugin.id=welcome-plugin
plugin.provider=Decebal Suiu
plugin.version=0.0.1
```
    
You can control extension instance creation overriding `createExtensionFactory` method from DefaultExtensionFinder. 
Also, you can control plugin instance creation overriding `createPluginFactory` method from DefaultExtensionFinder. 

For more information please see the demo sources.

**NOTE:** If your application didn't find extensions then make sure that you have a file with name `extensions.idx` 
generated by PF4J in the plugin jar.
It's most likely that they are some problems with the annotation processing mechanism from Java.
O possible solution to resolve your problem is to add a configuration to your maven build.
The `maven-compiler-plugin` can be configured to do this like so:
```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>2.5.1</version>
    <configuration>
        <annotationProcessors>
            <annotationProcessor>ro.fortsoft.pf4j.processor.ExtensionAnnotationProcessor</annotationProcessor>
        </annotationProcessors>
    </configuration>
</plugin> 
```

Plugin assembly
------------------------------
After you developed a plugin the next step is to deploy it in your application. For this task, one option is to create a zip file with a structure described in section [How to use](https://github.com/decebals/pf4j/blob/master/README.md#how-to-use) from the beginning of the document.  
If you use `apache maven` as build manger than your pom.xml file must looks like [this](https://github.com/decebals/pf4j/blob/master/demo/plugins/plugin1/pom.xml). This file it's very simple and it's self explanatory.  
If you use `apache ant` then your build.xml file must looks like [this](https://github.com/gitblit/gitblit-powertools-plugin/blob/master/build.xml). In this case please look at the "build" target.  

Plugin lifecycle
--------------------------
Each plugin passes through a pre-defined set of states. [PluginState](https://github.com/decebals/pf4j/blob/master/pf4j/src/main/java/ro/fortsoft/pf4j/PluginState.java) defines all possible states.   
The primary plugin states are:
* CREATED
* DISABLED
* STARTED
* STOPPED

The DefaultPluginManager contains the following logic:
* all plugins are resolved & loaded
* *DISABLED* plugins are NOT automatically *STARTED* by pf4j in `startPlugins()` BUT you may manually start (and therefore enable) a *DISABLED* plugin by calling `startPlugin(pluginId)` instead of `enablePlugin(pluginId)` + `startPlugin(pluginId)`
* only *STARTED* plugins may contribute extensions. Any other state should not be considered ready to contribute an extension to the running system.

The differences between a DISABLED plugin and a STARTED plugin are:
* a STARTED plugin has executed Plugin.start(), a DISABLED plugin has not
* a STARTED plugin may contribute extension instances, a DISABLED plugin may not

DISABLED plugins still have valid class loaders and their classes can be manually
loaded and explored, but the resource loading - which is important for inspection - 
has been handicapped by the DISABLED check.

As integrators of pf4j evolve their extension APIs it will become
a requirement to specify a minimum system version for loading plugins.
Loading & starting a newer plugin on an older system could result in
runtime failures due to method signature changes or other class
differences.  
For this reason was added a manifest attribute (in PluginDescriptor) to specify a 'requires' version
which is a minimum system version. Also DefaultPluginManager contains a method to
specify the system version of the plugin manager and the logic to disable
plugins on load if the system version is too old (if you want total control, please override `isPluginValid()`). This works for both `loadPlugins()` and `loadPlugin()`.  

__PluginStateListener__ defines the interface for an object that listens to plugin state changes. You can use `addPluginStateListener()` and `removePluginStateListener()` from PluginManager if you want to add or remove a plugin state listener.  

Your application, as a PF4J consumer, has full control over each plugin (state). So, you can load, unload, enable, disable, start, stop and delete a certain plugin using PluginManager (programmatically).

Development mode
--------------------------
PF4J can run in two modes: **DEVELOPMENT** and **DEPLOYMENT**.  
The DEPLOYMENT(default) mode is the standard workflow for plugins creation: create a new Maven module for each plugin, codding the plugin (declares new extension points and/or 
add new extensions), pack the plugin in a zip file, deploy the zip file to plugins folder. These operations are time consuming and from this reason I introduced the DEVELOPMENT runtime mode.  
The main advantage of DEVELOPMENT runtime mode for a plugin developer is that he/she is not enforced to pack and deploy the plugins. In DEVELOPMENT mode you can developing plugins in a simple and fast mode.   

Lets describe how DEVELOPMENT runtime mode works.

First, you can change the runtime mode using the "pf4j.mode" system property or overriding `DefaultPluginManager.getRuntimeMode()`.  
For example I run the pf4j demo in eclipse in DEVELOPMENT mode adding only `"-Dpf4j.mode=development"` to the pf4j demo launcher.  
You can retrieve the current runtime mode using `PluginManager.getRuntimeMode()` or in your Plugin implementation with `getWrapper().getRuntimeMode()`(see [WelcomePlugin](https://github.com/decebals/pf4j/blob/master/demo/plugins/plugin1/src/main/java/ro/fortsoft/pf4j/demo/welcome/WelcomePlugin.java)).   
The DefaultPluginManager determines automatically the correct runtime mode and for DEVELOPMENT mode overrides some components(pluginsDirectory is __"../plugins"__, __PropertiesPluginDescriptorFinder__ as PluginDescriptorFinder, __DevelopmentPluginClasspath__ as PluginClassPath).  
Another advantage of DEVELOPMENT runtime mode is that you can execute some code lines only in this mode (for example more debug messages). 

**NOTE:** If you use Eclipse than make sure annotation processing is enabled at least for any projects registering objects using annotations. In the properties for your new project go to __Java Compiler > Annotation Processing__
Check the __“Enable Project Specific Settings”__ and make sure __“Enable annotation processing”__ is checked.  
If you use Maven as build manger, after each dependency modification in your plugin (Maven module) you must run __Maven > Update Project...__   

For more details see the demo application. 

Enable/Disable plugins
-------------------
In theory, it's a relation **1:N** between an extension point and the extensions for this extension point.   
This works well, except for when you develop multiple plugins for this extension point as different options for your clients to decide on which one to use.  
In this situation you wish a possibility to disable all but one extension.   
For example I have an extension point for sending mail (EmailSender interface) with two extensions: one based on Sendgrid and another
based on Amazon Simple Email Service.   
The first extension is located in Plugin1 and the second extension is located in Plugin2.   
I want to go only with one extension ( **1:1** relation between extension point and extensions) and to achieve this I have two options:  
1) uninstall Plugin1 or Plugin2 (remove folder pluginX.zip and pluginX from plugins folder)  
2) disable Plugin1 or Plugin2  

For option two you must create a simple file **enabled.txt** or **disabled.txt** in your plugins folder.   
The content for **enabled.txt** is similar with:

```
########################################
# - load only these plugins
# - add one plugin id on each line
# - put this file in plugins folder
########################################
welcome-plugin
```

The content for **disabled.txt** is similar with:

```
########################################
# - load all plugins except these
# - add one plugin id on each line
# - put this file in plugins folder
########################################
welcome-plugin
```

All comment lines (line that start with # character) are ignored.   
If a file with enabled.txt exists than disabled.txt is ignored. See enabled.txt and disabled.txt from the demo folder. 

Default/System extension
-------------------
Starting with version 0.9 you can define an extension directly in the application jar (you're not obligated 
to put the extension in a plugin - you can see this extension as a default/system extension). 
See [WhazzupGreeting](https://github.com/decebals/pf4j/blob/master/demo/app/src/main/java/ro/fortsoft/pf4j/demo/WhazzupGreeting.java) 
for a real example.

This is great for starting application phase. In this scenario you have a minimalist plugin framework with one class loader 
(the application class loader), similar with Java [ServiceLoader](https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html) 
but with the following benefits:
- no need to write provider-configuration files in the resource directory `META-INF/services`, you using the elegant
 `@Extension` annotation from PF4J
- anytime you can switch to the multiple class loader mechanism without to change one code line in your application  

Of course the code present in the `Boot` class from the demo application it is functional but you can use a more minimalist code 
skipping `pluginManager.loadPlugins()` and `pluginManager.startPlugins()`. 

```java
public static void main(String[] args) {
    PluginManager pluginManager = new DefaultPluginManager();
    pluginManager.loadPlugins();
    pluginManager.startPlugins();
    List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
    for (Greeting greeting : greetings) {
        System.out.println(">>> " + greeting.getGreeting());
    }
}
```

The above code can be written:

```java
public static void main(String[] args) {
    PluginManager pluginManager = new DefaultPluginManager();
    List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
    for (Greeting greeting : greetings) {
        System.out.println(">>> " + greeting.getGreeting());
    }
}
```

ServiceLoader interoperability
-------------------
Starting with version 0.12 PF4J comes with a better support for `ServiceLoader`.
PF4J can read `META-INF/services` (Java Service Provider mechanism) as extensions, so, 
if you have a modular application based on `java.util.ServiceLoader` class you can replace entirely the `ServiceLoader.load()` 
calls from your application with `PluginManager.getExtensions()` and migrate smooth from ServiceLoader to PF4J.

Also you have the possibility to change the `ExtensionStorage` used in `ExtensionAnnotationProcessor`. 
By default we use the format with `META-INF/extensions.idx`
```
ro.fortsoft.pf4j.demo.HowdyGreeting
ro.fortsoft.pf4j.demo.WhazzupGreeting
```
 
but you can use a more standard location and format, `META-INF/services/<extension-point>`, used by Java Service Provider 
(see `java.util.ServiceLoader`) via `ServiceProviderExtensionStorage` implementation. 
In this case the format of `META-INF/services/ro.fortsoft.pf4j.demo.api.Greeting` is
```
# Generated by PF4J
ro.fortsoft.pf4j.demo.HowdyGreeting
ro.fortsoft.pf4j.demo.WhazzupGreeting # pf4j extension
```

where the `ro.fortsoft.pf4j.demo.HowdyGreeting` entry is legacy (it's not generated by PF4J) but it's seen as 
an extension of `Greeting` by PF4J (at runtime).

You can plug your custom `ExtensionStorage` implementation in `ExtensionAnnotationProcessor` in two possible modes:
- set the annotation procesor option with key `pf4j.storageClassName`
- set the system property with key `pf4j.storageClassName`

For example if I want to use `ServiceProviderExtensionStorage` then the value for the `pf4j.storageClassName` key must be
`ro.fortsoft.pf4j.processor.ServiceProviderExtensionStorage`

**NOTE:** `ServiceLoaderExtensionFinder`, the class that lookups for extensions stored in `META-INF/services` folder, is
not added/enabled by default. To do this please override `createExtensionFinder` from `DefaultPluginManager`:
```java
protected ExtensionFinder createExtensionFinder() {
    return ((DefaultExtensionFinder) super.createExtensionFinder()).addServiceProviderExtensionFinder();
}
```

Troubleshooting
-------------------
Below are listed some problems that may occur when attempting to use PF4J, and suggestions for solving them.

- **No Extensions Found**

See if you have a file `extensions.idx` in each plugin.  
If file `extensions.idx` doesn't exist then probably it's something wrong with the annotation processing step (enable annotation processing in your IDE or in your Maven script).   
If file `extensions.idx` exists and it's not empty then sure you have a class loader issue (you have the same extension point in two different class loader), in this situation you must remove some libraries (probably the API jar) from plugin.   

If the problem persist or you want to find more info related to the extensions discovery process (e.g what interfaces/classes are loaded by each plugin, what classes are not recognized as extensions for an extension point) then you must put on `TRACE` level the logger for `PluginClassLoader` and `AbstractExtensionFinder` (see the [log4j.properties](https://github.com/decebals/pf4j/blob/master/demo/app/src/main/resources/log4j.properties) file for demo).   

Are some resources on the internet related to this subject: [#82](https://github.com/decebals/pf4j/issues/82), [#64](https://github.com/decebals/pf4j/issues/64) and [No extensions found] (https://groups.google.com/forum/#!topic/pf4j/tEQXY_WpD3A).

Demo
-------------------
I have a tiny demo application. The demo application is in demo folder.
In demo/api folder I declared an extension point ( _Greeting_).  
In demo/plugins I implemented two plugins: plugin1, plugin2 (each plugin adds an extension for _Greeting_).  

To run the demo application use:  
 
```
./run-demo.sh (for Linux/Unix)
./run-demo.bat (for Windows)
```

How to build
-------------------
Requirements: 
- [Git](http://git-scm.com/) 
- JDK 7 (test with `java -version`)
- [Apache Maven 3](http://maven.apache.org/) (test with `mvn -version`)

Steps:
- create a local clone of this repository (with `git clone https://github.com/decebals/pf4j.git`)
- go to project's folder (with `cd pf4j`) 
- build the artifacts (with `mvn clean package` or `mvn clean install`)

After above steps a folder _pf4j/target_ is created and all goodies are in that folder.

Mailing list
--------------
Much of the conversation between developers and users is managed through [mailing list] (http://groups.google.com/group/pf4j).

Versioning
------------
PF4J will be maintained under the Semantic Versioning guidelines as much as possible.

Releases will be numbered with the follow format:

`<major>.<minor>.<patch>`

And constructed with the following guidelines:

* Breaking backward compatibility bumps the major
* New additions without breaking backward compatibility bumps the minor
* Bug fixes and misc changes bump the patch

For more information on SemVer, please visit http://semver.org/.

License
--------------
Copyright 2012 Decebal Suiu
 
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
the License. You may obtain a copy of the License in the LICENSE file, or at:
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
