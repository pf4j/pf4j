/*
 * Copyright 2012 Decebal Suiu
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
package org.pf4j.demo.hello;

import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.demo.api.Greeting;

/**
 * A very simple plugin.
 *
 * @author Decebal Suiu
 */
public class HelloPlugin extends Plugin {

    public HelloPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        System.out.println("HelloPlugin.start()");
    }

    @Override
    public void stop() {
        System.out.println("HelloPlugin.stop()");
    }

    @Extension(ordinal=1)
    public static class HelloGreeting implements Greeting {

    	@Override
        public String getGreeting() {
            return "Hello";
        }

    }

}
