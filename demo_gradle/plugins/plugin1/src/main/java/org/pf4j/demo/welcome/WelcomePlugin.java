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
package org.pf4j.demo.welcome;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.demo.api.Greeting;

/**
 * @author Decebal Suiu
 */
public class WelcomePlugin extends Plugin {
    private static final Logger logger = LoggerFactory.getLogger(WelcomePlugin.class);

    public WelcomePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        logger.info("WelcomePlugin.start()");
        	logger.info(StringUtils.upperCase("WelcomePlugin"));
    }

    @Override
    public void stop() {
        logger.info("WelcomePlugin.stop()");
    }

    @Extension
    public static class WelcomeGreeting implements Greeting {

    	@Override
        public String getGreeting() {
            return "Welcome";
        }

    }

}
