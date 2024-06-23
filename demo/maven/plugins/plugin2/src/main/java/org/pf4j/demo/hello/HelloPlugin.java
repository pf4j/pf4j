/*
 * Copyright (C) 2012-present the original author or authors.
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
import org.pf4j.demo.api.DemoPlugin;
import org.pf4j.demo.api.Greeting;
import org.pf4j.demo.api.PluginContext;

/**
 * A very simple plugin.
 *
 * @author Decebal Suiu
 */
public class HelloPlugin extends DemoPlugin {

    public HelloPlugin(PluginContext context) {
        super(context);
    }

    @Override
    public void start() {
        log.info("HelloPlugin.start()");
    }

    @Override
    public void stop() {
        log.info("HelloPlugin.stop()");
    }

    @Extension(ordinal=1)
    public static class HelloGreeting implements Greeting {

        @Override
        public String getGreeting() {
            return "Hello";
        }

    }

}
