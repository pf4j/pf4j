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
package org.pf4j.demo;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionWrapper;
import org.pf4j.PluginManager;
import org.pf4j.demo.api.Greeting;

/**
 * A boot class that start the demo.
 *
 * @author Decebal Suiu
 */
public class Boot {

    public static void main(String[] args) {
    	// print logo
    	printLogo();
    	
        // load and start (active/resolved) plugins
        final PluginManager pluginManager = new DefaultPluginManager();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        
        List<ExtensionWrapper<Greeting>> greetings = pluginManager.getExtensions(Greeting.class);
        for (ExtensionWrapper<Greeting> greeting : greetings) {
        	System.out.println(">>> " + greeting.getInstance().getGreeting());
        }
        
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

	private static void printLogo() {
    	System.out.println(StringUtils.repeat("#", 40));
    	System.out.println(StringUtils.center("PF4J-DEMO", 40));
    	System.out.println(StringUtils.repeat("#", 40));		
	}

}
