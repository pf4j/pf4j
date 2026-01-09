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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginStateEventTest {

    private PluginManager pluginManager;
    private PluginWrapper pluginWrapper;

    @BeforeEach
    void setUp() {
        pluginManager = mock(PluginManager.class);
        pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginId()).thenReturn("testPlugin");
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);
    }

    @Test
    void testGetSource() {
        PluginStateEvent event = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);

        assertEquals(pluginManager, event.getSource());
    }

    @Test
    void testGetPlugin() {
        PluginStateEvent event = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);

        assertEquals(pluginWrapper, event.getPlugin());
    }

    @Test
    void testGetPluginState() {
        PluginStateEvent event = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);

        assertEquals(PluginState.STARTED, event.getPluginState());
    }

    @Test
    void testGetOldState() {
        PluginStateEvent event = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);

        assertEquals(PluginState.CREATED, event.getOldState());
    }

    @Test
    void testToString() {
        PluginStateEvent event = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);

        String result = event.toString();
        assertTrue(result.contains("testPlugin"));
        assertTrue(result.contains("STARTED"));
        assertTrue(result.contains("CREATED"));
    }

    @Test
    void testEquals() {
        PluginWrapper otherWrapper = mock(PluginWrapper.class);
        when(otherWrapper.getPluginId()).thenReturn("otherPlugin");
        when(otherWrapper.getPluginState()).thenReturn(PluginState.STOPPED);

        PluginStateEvent event1 = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);
        PluginStateEvent event2 = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);
        PluginStateEvent event3 = new PluginStateEvent(pluginManager, otherWrapper, PluginState.CREATED);
        PluginStateEvent event4 = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.STOPPED);

        assertEquals(event1, event2);
        assertNotEquals(event1, event3);
        assertNotEquals(event1, event4);
        assertEquals(event1, event1);
        assertNotEquals(event1, null);
        assertNotEquals(event1, "string");
    }

    @Test
    void testHashCode() {
        PluginStateEvent event1 = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);
        PluginStateEvent event2 = new PluginStateEvent(pluginManager, pluginWrapper, PluginState.CREATED);

        assertEquals(event1.hashCode(), event2.hashCode());
    }

}