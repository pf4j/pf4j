/*
 * Copyright 2015 Mario Franco.
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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mario Franco
 */
public class PluginDependencyTest {

    /**
     * Test of getPluginId method, of class PluginDependency.
     */
    @Test
    public void testPluginDependecy() {
        PluginDependency instance = new PluginDependency("test");
        assertEquals("test", instance.getPluginId());
        assertEquals("*", instance.getPluginVersionSupport());

        instance = new PluginDependency("test@");
        assertEquals("test", instance.getPluginId());
        assertEquals("*", instance.getPluginVersionSupport());

        instance = new PluginDependency("test@1.0");
        assertEquals("test", instance.getPluginId());
        assertEquals("1.0", instance.getPluginVersionSupport());
        assertEquals("PluginDependency [pluginId=test, pluginVersionSupport=1.0]", instance.toString());
    }

}
