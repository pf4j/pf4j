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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(HelloPlugin.class);

    public HelloPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        logger.info("HelloPlugin.start()");
    }

    @Override
    public void stop() {
        logger.info("HelloPlugin.stop()");
    }

    @Extension(ordinal=1)
    public static class HelloGreeting implements Greeting {

    	@Override
        public String getGreeting() {
            return "Hello";
        }

    }

}
