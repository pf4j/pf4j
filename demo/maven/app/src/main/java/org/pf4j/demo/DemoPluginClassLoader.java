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

import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

/**
 * Custom PluginClassLoader that demonstrates how to exclude specific packages
 * from parent delegation.
 * <p>
 * This allows demo plugins to load their own classes even if they're in the
 * org.pf4j.demo package, which would normally be delegated to the parent classloader.
 * <p>
 * This pattern is useful for framework extensions (like PF4J-Plus) that need to
 * customize class loading behavior for their own demo/test packages.
 *
 * @author Decebal Suiu
 */
class DemoPluginClassLoader extends PluginClassLoader {

    public DemoPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
        super(pluginManager, pluginDescriptor, parent);
    }

    @Override
    protected boolean shouldDelegateToParent(String className) {
        // Call parent implementation but exclude org.pf4j.demo.* classes
        // This demonstrates how to customize class loading for framework extensions
        return super.shouldDelegateToParent(className)
            && !className.startsWith("org.pf4j.demo");
    }

}