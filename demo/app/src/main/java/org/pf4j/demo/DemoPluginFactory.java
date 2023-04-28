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
package org.pf4j.demo;

import java.lang.reflect.Constructor;
import org.pf4j.DefaultPluginFactory;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.pf4j.demo.api.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DemoPluginFactory extends DefaultPluginFactory {

    private static final Logger log = LoggerFactory.getLogger(DemoPluginFactory.class);

    @Override
    protected Plugin createInstance(Class<?> pluginClass, PluginWrapper pluginWrapper) {
        PluginContext context = new PluginContext(pluginWrapper.getRuntimeMode());
        try {
            Constructor<?> constructor = pluginClass.getConstructor(PluginContext.class);
            return (Plugin) constructor.newInstance(context);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

}
