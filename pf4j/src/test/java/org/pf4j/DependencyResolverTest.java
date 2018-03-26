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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Decebal Suiu
 */
public class DependencyResolverTest {

    private DependencyResolver resolver;

    @Before
    public void init() {
        VersionManager versionManager = new DefaultVersionManager();
        resolver = new DependencyResolver(versionManager);
    }

    @Test
    public void sortedPlugins() {
        // create incomplete plugin descriptor (ignore some attributes)
        PluginDescriptor pd1 = new DefaultPluginDescriptor()
            .setPluginId("p1")
            .setDependencies("p2");

        PluginDescriptor pd2 = new DefaultPluginDescriptor()
            .setPluginId("p2")
            .setPluginVersion("0.0.0"); // needed in "checkDependencyVersion" method

        List<PluginDescriptor> plugins = new ArrayList<>();
        plugins.add(pd1);
        plugins.add(pd2);

        DependencyResolver.Result result = resolver.resolve(plugins);

        assertTrue(result.getNotFoundDependencies().isEmpty());
        assertEquals(result.getSortedPlugins(), Arrays.asList("p2", "p1"));
    }

    @Test
    public void notFoundDependencies() throws Exception {
        PluginDescriptor pd1 = new DefaultPluginDescriptor()
            .setPluginId("p1")
            .setDependencies("p2, p3");

        List<PluginDescriptor> plugins = new ArrayList<>();
        plugins.add(pd1);

        DependencyResolver.Result result = resolver.resolve(plugins);

        assertFalse(result.getNotFoundDependencies().isEmpty());
        assertEquals(result.getNotFoundDependencies(), Arrays.asList("p2", "p3"));
    }

    @Test
    public void cyclicDependencies() {
        PluginDescriptor pd1 = new DefaultPluginDescriptor()
            .setPluginId("p1")
            .setPluginVersion("0.0.0")
            .setDependencies("p2");

        PluginDescriptor pd2 = new DefaultPluginDescriptor()
            .setPluginId("p2")
            .setPluginVersion("0.0.0")
            .setDependencies("p3");

        PluginDescriptor pd3 = new DefaultPluginDescriptor()
            .setPluginId("p3")
            .setPluginVersion("0.0.0")
            .setDependencies("p1");

        List<PluginDescriptor> plugins = new ArrayList<>();
        plugins.add(pd1);
        plugins.add(pd2);
        plugins.add(pd3);

        DependencyResolver.Result result = resolver.resolve(plugins);

        assertTrue(result.hasCyclicDependency());
    }

    @Test
    public void wrongDependencyVersion() {
        PluginDescriptor pd1 = new DefaultPluginDescriptor()
            .setPluginId("p1")
//            .setDependencies("p2@2.0.0"); // simple version
            .setDependencies("p2@>=1.5.0 & <1.6.0"); // range version

        PluginDescriptor pd2 = new DefaultPluginDescriptor()
            .setPluginId("p2")
            .setPluginVersion("1.4.0");

        List<PluginDescriptor> plugins = new ArrayList<>();
        plugins.add(pd1);
        plugins.add(pd2);

        DependencyResolver.Result result = resolver.resolve(plugins);

        assertFalse(result.getWrongVersionDependencies().isEmpty());
    }

    @Test
    public void goodDependencyVersion() {
        PluginDescriptor pd1 = new DefaultPluginDescriptor()
            .setPluginId("p1")
            .setDependencies("p2@2.0.0");

        PluginDescriptor pd2 = new DefaultPluginDescriptor()
            .setPluginId("p2")
            .setPluginVersion("2.0.0");

        List<PluginDescriptor> plugins = new ArrayList<>();
        plugins.add(pd1);
        plugins.add(pd2);

        DependencyResolver.Result result = resolver.resolve(plugins);

        assertTrue(result.getWrongVersionDependencies().isEmpty());
    }

}
