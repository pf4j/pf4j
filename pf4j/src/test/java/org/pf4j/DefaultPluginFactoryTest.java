/*
 * Copyright 2015 Decebal Suiu
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
import org.pf4j.plugin.FailTestPlugin;
import org.pf4j.plugin.TestPlugin;
import org.pf4j.plugin.AnotherFailTestPlugin;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Mario Franco
 */
public class DefaultPluginFactoryTest {

    /**
     * Test of create method, of class DefaultPluginFactory.
     */
    @Test
    public void testCreate() {
        PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
        when(pluginDescriptor.getPluginClass()).thenReturn(TestPlugin.class.getName());

        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getDescriptor()).thenReturn(pluginDescriptor);
        when(pluginWrapper.getPluginClassLoader()).thenReturn(getClass().getClassLoader());

        DefaultPluginFactory instance = new DefaultPluginFactory();

        Plugin result = instance.create(pluginWrapper);
        assertNotNull(result);
        assertThat(result, instanceOf(TestPlugin.class));
    }

    /**
     * Test of create method, of class DefaultPluginFactory.
     */
    @Test
    public void testCreateFail() {
        PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
        when(pluginDescriptor.getPluginClass()).thenReturn(FailTestPlugin.class.getName());

        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getDescriptor()).thenReturn(pluginDescriptor);
        when(pluginWrapper.getPluginClassLoader()).thenReturn(getClass().getClassLoader());

        DefaultPluginFactory instance = new DefaultPluginFactory();

        Plugin result = instance.create(pluginWrapper);
        assertNull(result);
    }

    /**
     * Test of create method, of class DefaultPluginFactory.
     */
    @Test
    public void testCreateFailNotFound() {
        PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
        when(pluginDescriptor.getPluginClass()).thenReturn("org.pf4j.plugin.NotFoundTestPlugin");

        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getDescriptor()).thenReturn(pluginDescriptor);
        when(pluginWrapper.getPluginClassLoader()).thenReturn(getClass().getClassLoader());

        DefaultPluginFactory instance = new DefaultPluginFactory();

        Plugin result = instance.create(pluginWrapper);
        assertNull(result);
    }

    /**
     * Test of create method, of class DefaultPluginFactory.
     */
    @Test
    public void testCreateFailConstructor() {
        PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
        when(pluginDescriptor.getPluginClass()).thenReturn(AnotherFailTestPlugin.class.getName());

        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getDescriptor()).thenReturn(pluginDescriptor);
        when(pluginWrapper.getPluginClassLoader()).thenReturn(getClass().getClassLoader());

        DefaultPluginFactory instance = new DefaultPluginFactory();

        Plugin result = instance.create(pluginWrapper);
        assertNull(result);
    }

}
