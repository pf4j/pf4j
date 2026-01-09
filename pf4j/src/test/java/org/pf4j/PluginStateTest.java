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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginStateTest {

    @Test
    void testIsCreated() {
        assertTrue(PluginState.CREATED.isCreated());
        assertFalse(PluginState.STARTED.isCreated());
    }

    @Test
    void testIsDisabled() {
        assertTrue(PluginState.DISABLED.isDisabled());
        assertFalse(PluginState.STARTED.isDisabled());
    }

    @Test
    void testIsResolved() {
        assertTrue(PluginState.RESOLVED.isResolved());
        assertFalse(PluginState.STARTED.isResolved());
    }

    @Test
    void testIsStarted() {
        assertTrue(PluginState.STARTED.isStarted());
        assertFalse(PluginState.STOPPED.isStarted());
    }

    @Test
    void testIsStopped() {
        assertTrue(PluginState.STOPPED.isStopped());
        assertFalse(PluginState.STARTED.isStopped());
    }

    @Test
    void testIsFailed() {
        assertTrue(PluginState.FAILED.isFailed());
        assertFalse(PluginState.STARTED.isFailed());
    }

    @Test
    void testIsUnloaded() {
        assertTrue(PluginState.UNLOADED.isUnloaded());
        assertFalse(PluginState.STARTED.isUnloaded());
    }

    @Test
    void testEqualsString() {
        assertTrue(PluginState.STARTED.equals("STARTED"));
        assertTrue(PluginState.STARTED.equals("started"));
        assertTrue(PluginState.STARTED.equals("Started"));
        assertFalse(PluginState.STARTED.equals("STOPPED"));
    }

    @Test
    void testToString() {
        assertEquals("STARTED", PluginState.STARTED.toString());
        assertEquals("STOPPED", PluginState.STOPPED.toString());
        assertEquals("CREATED", PluginState.CREATED.toString());
    }

    @Test
    void testParse() {
        assertEquals(PluginState.STARTED, PluginState.parse("STARTED"));
        assertEquals(PluginState.STARTED, PluginState.parse("started"));
        assertEquals(PluginState.STARTED, PluginState.parse("Started"));
        assertEquals(PluginState.STOPPED, PluginState.parse("STOPPED"));
        assertEquals(PluginState.CREATED, PluginState.parse("CREATED"));
        assertEquals(PluginState.DISABLED, PluginState.parse("DISABLED"));
        assertEquals(PluginState.RESOLVED, PluginState.parse("RESOLVED"));
        assertEquals(PluginState.FAILED, PluginState.parse("FAILED"));
        assertEquals(PluginState.UNLOADED, PluginState.parse("UNLOADED"));
    }

    @Test
    void testParseInvalidState() {
        assertNull(PluginState.parse("INVALID"));
        assertNull(PluginState.parse(""));
        assertNull(PluginState.parse("random"));
    }

}