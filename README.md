Plugin Framework for Java (PF4J)
=====================

A plugin is a way for a third party to extend the functionality of an application. A plugin implements extension points
declared by application or other plugins. Also a plugin can define extension points.

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

How to use
-------------------
It's very simple to add pf4j in your application:

    public static void main(String[] args) {
        ...
        
        PluginManager pluginManager = new DefaultPluginManager();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        ...
    }

In above code, I created a **DefaultPluginManager** (it's the default implementation for
**PluginManager** interface) that loads and starts all active(resolved) plugins.  
The available plugins are loaded using a **PluginClassLoader**.   
The **PluginClassLoader** contains only classes found in _classes_ and _lib_ folders of plugin and runtime classes and libraries of the required plugins. 
The plugins are stored in a folder. You can specify the plugins folder in the constructor of DefaultPluginManager. If the plugins folder is not specified 
than the location is returned by `System.getProperty("pf4j.pluginsDir", "plugins")`.

The structure of plugins folder is:
* plugin1.zip (or plugin1 folder)
* plugin2.zip (or plugin2 folder)

In plugins folder you can put a plugin as folder or archive file (zip).
A plugin folder has this structure:
* `classes` folder
* `lib` folder (optional - if the plugin used third party libraries)

The plugin manager searches plugins metadata using a **PluginDescriptorFinder**.   
**DefaultPluginDescriptorFinder** lookups plugins descriptors in MANIFEST.MF file.
In this case the `classes/META-INF/MANIFEST.MF` file looks like:

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

In above manifest I described a plugin with id `welcome-plugin`, with class `ro.fortsoft.pf4j.demo.welcome.WelcomePlugin`, with version `0.0.1` and with dependencies 
to plugins `x, y, z`.

You can define an extension point in your application using **ExtensionPoint** interface marker.

    public interface Greeting extends ExtensionPoint {

        public String getGreeting();

    }

Another important internal component is **ExtensionFinder** that describes how plugin manager discovers extensions for extensions points.   
**DefaultExtensionFinder** looks up extensions using **Extension** annotation.

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

In above code I supply an extension for the `Greeting` extension point.

You can retrieve all extensions for an extension point with:

    List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
    for (Greeting greeting : greetings) {
        System.out.println(">>> " + greeting.getGreeting());
    }

The output is:

    >>> Welcome
    >>> Hello

For more information please see the demo sources.

Demo
-------------------

I have a tiny demo application. The demo application is in demo folder.
In demo/api folder I declared an extension point (_Greeting_).  
In demo/plugin* I implemented two plugins: plugin1, plugin2 (each plugin adds an extension for _Greeting_).  

To run the demo application use:  
 
    ./run-demo.sh

License
--------------
  
Copyright 2012 Decebal Suiu
 
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
the License. You may obtain a copy of the License in the LICENSE file, or at:
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
