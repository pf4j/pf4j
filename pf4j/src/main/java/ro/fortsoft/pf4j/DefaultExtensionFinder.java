/*
 * Copyright 2013 Decebal Suiu
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
package ro.fortsoft.pf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The default implementation for ExtensionFinder.
 * It's a compound ExtensionFinder.
 *
 * @author Decebal Suiu
 */
public class DefaultExtensionFinder implements ExtensionFinder, PluginStateListener {

    protected List<ExtensionFinder> finders = new ArrayList<>();

	public DefaultExtensionFinder(PluginManager pluginManager) {
        addDefaults(pluginManager);
    }

    @Override
    public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
        List<ExtensionWrapper<T>> extensions = new ArrayList<>();
        for (ExtensionFinder finder : finders) {
            extensions.addAll(finder.find(type));
        }

        return extensions;
    }

    @Override
    public Set<String> findClassNames(String pluginId) {
        Set<String> classNames = new HashSet<>();
        for (ExtensionFinder finder : finders) {
            classNames.addAll(finder.findClassNames(pluginId));
        }

        return classNames;
    }

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        for (ExtensionFinder finder : finders) {
            if (finder instanceof PluginStateListener) {
                ((PluginStateListener) finder).pluginStateChanged(event);
            }
        }
    }

    protected void addDefaults(PluginManager pluginManager) {
        finders.add(new ServiceProviderExtensionFinder(pluginManager));
        finders.add(new LegacyExtensionFinder(pluginManager));
    }

}
