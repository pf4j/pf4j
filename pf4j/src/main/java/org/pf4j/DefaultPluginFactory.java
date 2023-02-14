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
package org.pf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * The default implementation for {@link PluginFactory}.
 * It uses {@link Constructor#newInstance(Object...)} method.
 *
 * @author Decebal Suiu
 */
public class DefaultPluginFactory implements PluginFactory {

    protected static final Logger log = LoggerFactory.getLogger(DefaultPluginFactory.class);

    /**
     * Creates a plugin instance. If an error occurs than that error is logged and the method returns {@code null}.
     */
    @Override
    public Plugin create(final PluginWrapper pluginWrapper) {
        String pluginClassName = pluginWrapper.getDescriptor().getPluginClass();
        log.debug("Create instance for plugin '{}'", pluginClassName);

        Class<?> pluginClass;
        try {
            pluginClass = pluginWrapper.getPluginClassLoader().loadClass(pluginClassName);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        // once we have the class, we can do some checks on it to ensure
        // that it is a valid implementation of a plugin.
        int modifiers = pluginClass.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)
                || (!Plugin.class.isAssignableFrom(pluginClass))) {
            log.error("The plugin class '{}' is not valid", pluginClassName);
            return null;
        }

        return createInstance(pluginClass, pluginWrapper);
    }

    protected Plugin createInstance(Class<?> pluginClass, PluginWrapper pluginWrapper) {
        try {
            Constructor<?> constructor = pluginClass.getConstructor(PluginWrapper.class);
            return (Plugin) constructor.newInstance(pluginWrapper);
        } catch (NoSuchMethodException e) {
            return createUsingNoParametersConstructor(pluginClass);
       } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    protected Plugin createUsingNoParametersConstructor(Class<?> pluginClass) {
        try {
            Constructor<?> constructor = pluginClass.getConstructor();
            return (Plugin) constructor.newInstance();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

}
