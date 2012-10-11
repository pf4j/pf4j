Plugin Framework for Java (PF4J)
=====================

A plugin is a way for a third party to extend the functionality of an application. A plugin implements extensions points
declared by application or another plugins. Also a plugin can defines extension points.

Components
-------------------
- **Plugin** is the base class for all plugins types. Each plugin is loaded into a separate class loader to avoid conflicts.
- **PluginManager** is used for all aspects of plugins management (loading, starting, stopping).
- **ExtensionPoint** is a point in the application where custom code can be invoked.
- **Extension** is an implementation of extension point.

Artifacts
-------------------
- PF4J `pf4j` (jar)
- PF4J Demo `pf4j-demo` (executable jar)

Using Maven
-------------------

First you must install the pf4j artifacts in your local maven repository with:

    mvn clean install

I will upload these artifacts in maven central repository as soon as possible.

In your pom.xml you must define the dependencies to PF4J artifacts with:

```xml
<dependency>
    <groupId>org.pf4j</groupId>
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

In above code, I created a DefaultPluginManager (it's the default implementation for
PluginManager interface) that load and start all active(resolved) plugins.
The plugins are stored in a folder. You can specify the plugins folder in constructor of DefaultPluginManager. If the plugins folder is not specified 
than the location is returned by System.getProperty("pf4j.pluginsDir", "plugins").

The structure of plugins folder is:
* plugin1.zip (or plugin1 folder)
* plugin2.zip (or plugin2 folder)

In plugins folder you can put a plugin as folder or archive file (zip).
A plugin folder has this structure:
* `classes` folder
* `lib` folder (optional - if the plugin used third party libraries)

The plugin manager discovers plugins metadata using a PluginDescriptorFinder. DefaultPluginDescriptorFinder lookup plugins descriptors in MANIFEST.MF file.
In this case the classes/META-INF/MANIFEST.MF looks like:

    Manifest-Version: 1.0
    Archiver-Version: Plexus Archiver
    Created-By: Apache Maven
    Built-By: decebal
    Build-Jdk: 1.6.0_17
    Plugin-Class: org.pf4j.demo.welcome.WelcomePlugin
    Plugin-Dependencies: x, y z
    Plugin-Id: welcome-plugin
    Plugin-Provider: Decebal Suiu
    Plugin-Version: 0.0.1

In above manifest I described a plugin with id `welcome-plugin`, with class `org.pf4j.demo.welcome.WelcomePlugin`, with version `0.0.1` and with dependencies 
to plugins `x, y, z`.

You can define an extension point in your application using ExtensionPoint interface marker.

    public interface Greeting extends ExtensionPoint {

        public String getGreeting();

    }

Another important internal component is ExtensionFinder that describes how plugin manager discovers extensions for extensions points. DefaultExtensionFinder look up extensions using Extension annotation.

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

In above code I supply an extension for the Greeting extension point.

You can retrieves all extensions for an extension point with:

    List<ExtensionWrapper<Greeting>> greetings = pluginManager.getExtensions(Greeting.class);
    for (ExtensionWrapper<Greeting> greeting : greetings) {
    	System.out.println(">>> " + greeting.getInstance().getGreeting());
    }


For more information please see the demo sources.

Demo
-------------------

I have a tiny demo application. In demo/api folder I declared an extension point (Greeting).
In this demo I have implemented two plugins: plugin1, plugin2 (each plugin with an extension).

The demo application is in demo folder.
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
