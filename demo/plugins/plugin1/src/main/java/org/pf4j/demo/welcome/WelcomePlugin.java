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
package org.pf4j.demo.welcome;

import org.apache.commons.lang.StringUtils;
import org.pf4j.Extension;
import org.pf4j.RuntimeMode;
import org.pf4j.demo.api.DemoPlugin;
import org.pf4j.demo.api.Greeting;
import org.pf4j.demo.api.PluginContext;

/**
 * @author Decebal Suiu
 */
public class WelcomePlugin extends DemoPlugin {

    public WelcomePlugin(PluginContext context) {
        super(context);
    }

    @Override
    public void start() {
        log.info("WelcomePlugin.start()");
        // for testing the development mode
        if (RuntimeMode.DEVELOPMENT.equals(context.getRuntimeMode())) {
            log.info(StringUtils.upperCase("WelcomePlugin"));
        }
    }

    @Override
    public void stop() {
        log.info("WelcomePlugin.stop()");
    }

    @Extension
    public static class WelcomeGreeting implements Greeting {

        @Override
        public String getGreeting() {
            return "Welcome";
        }

    }

}
