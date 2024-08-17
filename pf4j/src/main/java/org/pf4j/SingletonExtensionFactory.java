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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link ExtensionFactory} that always returns a specific instance.
 * Optional, you can specify the extension classes for which you want singletons.
 *
 * @author Decebal Suiu
 * @author Ajith Kumar
 */
public class SingletonExtensionFactory extends DefaultExtensionFactory {

    private final List<String> extensionClassNames;

    private final Map<ClassLoader, Map<String, Object>> cache;

    public SingletonExtensionFactory(PluginManager pluginManager, String... extensionClassNames) {
        this.extensionClassNames = Arrays.asList(extensionClassNames);

        cache = new HashMap<>();

        pluginManager.addPluginStateListener(event -> {
            if (!event.getPluginState().isStarted()) {
                cache.remove(event.getPlugin().getPluginClassLoader());
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> extensionClass) {
        String extensionClassName = extensionClass.getName();
        ClassLoader extensionClassLoader = extensionClass.getClassLoader();

        if (!cache.containsKey(extensionClassLoader)) {
            cache.put(extensionClassLoader, new HashMap<>());
        }

        Map<String, Object> classLoaderBucket = cache.get(extensionClassLoader);

        if (classLoaderBucket.containsKey(extensionClassName)) {
            return (T) classLoaderBucket.get(extensionClassName);
        }

        T extension = super.create(extensionClass);
        if (extensionClassNames.isEmpty() || extensionClassNames.contains(extensionClassName)) {
            classLoaderBucket.put(extensionClassName, extension);
        }

        return extension;
    }

}
