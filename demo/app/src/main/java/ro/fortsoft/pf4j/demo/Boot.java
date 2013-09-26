/*
 * Copyright 2012 Decebal Suiu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pf4j.demo;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginClasspath;
import ro.fortsoft.pf4j.PluginDescriptorFinder;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PropertiesPluginDescriptorFinder;
import ro.fortsoft.pf4j.demo.api.Greeting;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
public class Boot {
	
    public static void main(String[] args) {
    	// print logo
    	printLogo();
    	
    	// create the plugin manager
        final PluginManager pluginManager = createPluginManager();
        
        // load and start (active/resolved) the plugins
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        
        // retrieves the extensions for Greeting extension point 
        List<Greeting> greetings = pluginManager.getExtensions(Greeting.class);
        for (Greeting greeting : greetings) {
        	System.out.println(">>> " + greeting.getGreeting());
        }
        
        // stop the plugins
        pluginManager.stopPlugins();
        /*
        Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				pluginManager.stopPlugins();
			}
        	
        });
        */
    }
    
    private static PluginManager createPluginManager() {
    	// retrieves the pf4j runtime mode
    	String modeAsString = System.getProperty("pf4j.mode", RuntimeMode.PROD.toString());
    	RuntimeMode mode = RuntimeMode.byName(modeAsString);

    	System.out.println("PF4J runtime mode: '" + mode + "'");
    	
    	// create the plugin manager depending on runtime mode
    	PluginManager pluginManager = null;
    	if (mode == RuntimeMode.PROD) {
    		pluginManager = new DefaultPluginManager();
    	} else if (mode == RuntimeMode.DEV) {
    		// run from eclipse IDE (for example)
    		pluginManager = new DefaultPluginManager(new File("../plugins")) {

				@Override
				protected PluginClasspath createPluginClasspath() {
					PluginClasspath pluginClasspath = super.createPluginClasspath();
					// modify plugin classes
					List<String> pluginClasses = pluginClasspath.getClassesDirectories();
					pluginClasses.clear();
					pluginClasses.add("target/classes");
					
					return pluginClasspath;
				}
	
				@Override
				protected PluginDescriptorFinder createPluginDescriptorFinder() {
					return new PropertiesPluginDescriptorFinder();
				}
				
    		};
        }
    	    	
    	return pluginManager;
    }

	private static void printLogo() {
    	System.out.println(StringUtils.repeat("#", 40));
    	System.out.println(StringUtils.center("PF4J-DEMO", 40));
    	System.out.println(StringUtils.repeat("#", 40));		
	}
	
	public enum RuntimeMode {
		
		DEV("dev"), // development
	    PROD("prod"); // production		

	    private final String name;
	    
		private static final Map<String, RuntimeMode> map = new HashMap<String, RuntimeMode>();
		
		static {
			for (RuntimeMode mode : RuntimeMode.values()) {
				map.put(mode.name, mode);
			}
		}
		
		private RuntimeMode(final String name) {
	        this.name = name;
	    }

	    @Override
	    public String toString() {
	        return name;
	    }
		
	    public static RuntimeMode byName(String name) {
	    	if (map.containsKey(name)) {
	    		return map.get(name);
	    	}

	    	throw new NoSuchElementException("Cannot found PF4J runtime mode with name '" + name + "'");
	    }
	    
	}

}
