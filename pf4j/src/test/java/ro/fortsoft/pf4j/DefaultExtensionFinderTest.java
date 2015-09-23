/*
 * Copyright 2015 Mario Franco.
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
package ro.fortsoft.pf4j;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ro.fortsoft.pf4j.plugin.FailTestPlugin;
import ro.fortsoft.pf4j.plugin.TestExtensionInterface;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Mario Franco
 */
public class DefaultExtensionFinderTest {
    PluginManager pluginManager;
    ExtensionFactory extensionFactory;

    public DefaultExtensionFinderTest() {
    }

    @Before
    public void setUp() {
        pluginManager = mock(PluginManager.class);
        extensionFactory = new DefaultExtensionFactory();
    }

    @After
    public void tearDown() {
        pluginManager = null;
        extensionFactory = null;
    }

    /**
     * Test of find method, of class DefaultExtensionFinder.
     */
    @Test
    public void testFindFailType() {
        DefaultExtensionFinder instance = new DefaultExtensionFinder(pluginManager, extensionFactory);
        List<ExtensionWrapper<FailTestPlugin>> list = instance.find(FailTestPlugin.class);
        assertEquals(0, list.size());
    }

    /**
     * Test of find method, of class DefaultExtensionFinder.
     */
    @Test
    public void testFind() {
        DefaultExtensionFinder instance = new DefaultExtensionFinder(pluginManager, extensionFactory) {
            @Override
            protected Map<String, Set<String>> readIndexFiles() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();
                Set<String> bucket = new HashSet<>();
                bucket.add("ro.fortsoft.pf4j.plugin.TestExtension");
                bucket.add("ro.fortsoft.pf4j.plugin.FailTestExtension");
                entries.put(null, bucket);
                return entries;
            }
        };
        List<ExtensionWrapper<TestExtensionInterface>> list = instance.find(TestExtensionInterface.class);
        assertEquals(2, list.size());
    }

    /**
     * Test of findClassNames method, of class DefaultExtensionFinder.
     */
    @Test
    public void testFindClassNames() {
        DefaultExtensionFinder instance = new DefaultExtensionFinder(pluginManager, extensionFactory) {
            @Override
            protected Map<String, Set<String>> readIndexFiles() {
                Map<String, Set<String>> entries = new LinkedHashMap<>();
                Set<String> bucket = new HashSet<>();
                bucket.add("ro.fortsoft.pf4j.plugin.TestExtension");
                bucket.add("ro.fortsoft.pf4j.plugin.FailTestExtension");
                entries.put(null, bucket);
                bucket = new HashSet<>();
                bucket.add("ro.fortsoft.pf4j.plugin.TestExtension");
                entries.put("plugin1", bucket);
                return entries;
            }
        };

        Set<String> result = instance.findClassNames(null);
        assertEquals(2, result.size());

        result = instance.findClassNames("plugin1");
        assertEquals(1, result.size());

    }

}
