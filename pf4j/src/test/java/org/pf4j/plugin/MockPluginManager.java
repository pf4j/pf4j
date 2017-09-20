/*
 * Copyright 2017 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.plugin;

import org.pf4j.DefaultPluginClasspath;
import org.pf4j.DefaultPluginDescriptorFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginDescriptorFinder;

import java.nio.file.Path;

/**
 * Manager for testing
 */
public class MockPluginManager extends DefaultPluginManager {

    private PluginDescriptorFinder finder = new DefaultPluginDescriptorFinder(new DefaultPluginClasspath());

    public MockPluginManager() {
        super();
    }

    public MockPluginManager(Path root, PluginDescriptorFinder finder) {
        super(root);
        this.finder = finder;
    }

    @Override
    protected PluginDescriptorFinder getPluginDescriptorFinder() {
        return finder;
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return finder;
    }

}
