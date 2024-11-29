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

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompoundPluginLoaderTest {

    @Test
    void addLoaderSuccessfully() {
        CompoundPluginLoader compoundLoader = new CompoundPluginLoader();
        PluginLoader mockLoader = mock(PluginLoader.class);
        compoundLoader.add(mockLoader);
        assertTrue(compoundLoader.getLoaders().contains(mockLoader));
    }

    @Test
    void addLoaderWithConditionTrue() {
        CompoundPluginLoader compoundLoader = new CompoundPluginLoader();
        PluginLoader mockLoader = mock(PluginLoader.class);
        compoundLoader.add(mockLoader, () -> true);
        assertTrue(compoundLoader.getLoaders().contains(mockLoader));
    }

    @Test
    void addLoaderWithConditionFalse() {
        CompoundPluginLoader compoundLoader = new CompoundPluginLoader();
        PluginLoader mockLoader = mock(PluginLoader.class);
        compoundLoader.add(mockLoader, () -> false);
        assertFalse(compoundLoader.getLoaders().contains(mockLoader));
    }

    @Test
    void applicableLoaderFound() {
        CompoundPluginLoader compoundLoader = new CompoundPluginLoader();
        PluginLoader mockLoader = mock(PluginLoader.class);
        Path mockPath = mock(Path.class);
        when(mockLoader.isApplicable(mockPath)).thenReturn(true);
        compoundLoader.add(mockLoader);
        assertTrue(compoundLoader.isApplicable(mockPath));
    }

    @Test
    void noApplicableLoaderFound() {
        CompoundPluginLoader compoundLoader = new CompoundPluginLoader();
        PluginLoader mockLoader = mock(PluginLoader.class);
        Path mockPath = mock(Path.class);
        when(mockLoader.isApplicable(mockPath)).thenReturn(false);
        compoundLoader.add(mockLoader);
        assertFalse(compoundLoader.isApplicable(mockPath));
    }

    @Test
    void loadPluginSuccessfully() {
        CompoundPluginLoader compoundLoader = new CompoundPluginLoader();
        PluginLoader mockLoader = mock(PluginLoader.class);
        Path mockPath = mock(Path.class);
        PluginDescriptor mockDescriptor = mock(PluginDescriptor.class);
        ClassLoader mockClassLoader = mock(ClassLoader.class);
        when(mockLoader.isApplicable(mockPath)).thenReturn(true);
        when(mockLoader.loadPlugin(mockPath, mockDescriptor)).thenReturn(mockClassLoader);
        compoundLoader.add(mockLoader);
        assertEquals(mockClassLoader, compoundLoader.loadPlugin(mockPath, mockDescriptor));
    }

    @Test
    void loadPluginThrowsExceptionWhenNoLoaderApplicable() {
        CompoundPluginLoader compoundLoader = new CompoundPluginLoader();
        PluginLoader mockLoader = mock(PluginLoader.class);
        Path mockPath = mock(Path.class);
        PluginDescriptor mockDescriptor = mock(PluginDescriptor.class);
        when(mockLoader.isApplicable(mockPath)).thenReturn(false);
        compoundLoader.add(mockLoader);
        assertThrows(RuntimeException.class, () -> compoundLoader.loadPlugin(mockPath, mockDescriptor));
    }

}
