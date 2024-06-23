/*
 * Copyright (C) 2012-present the original author or authors.
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
package org.pf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.test.TestExtension;
import org.pf4j.test.TestExtensionPoint;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * @author Decebal Suiu
 */
public class AbstractPluginManagerTest {

    private AbstractPluginManager pluginManager;

    @BeforeEach
    void setUp() {
        pluginManager = mock(AbstractPluginManagerWithDefaultVersionManager.class, withSettings()
            .useConstructor()
            .defaultAnswer(CALLS_REAL_METHODS));
    }

    @AfterEach
    void tearDown() {
        pluginManager = null;
    }

    @Test
    public void getExtensionsByType() {
        ExtensionFinder extensionFinder = mock(ExtensionFinder.class);
        List<ExtensionWrapper<TestExtensionPoint>> extensionList = new ArrayList<>(1);
        extensionList.add(new ExtensionWrapper<>(new ExtensionDescriptor(0, TestExtension.class), new DefaultExtensionFactory()));
        when(extensionFinder.find(TestExtensionPoint.class)).thenReturn(extensionList);

        pluginManager.extensionFinder = extensionFinder;
        List<TestExtensionPoint> extensions = pluginManager.getExtensions(TestExtensionPoint.class);
        assertEquals(1, extensions.size());
    }

    @Test
    public void getVersion() {
        assertNotEquals("0.0.0", pluginManager.getVersion());
    }

    @Test
    void stopStartedPlugin() throws IOException {
        String pluginId = "plugin1";
        PluginWrapper pluginWrapper = createPluginWrapper(pluginId);
        pluginWrapper.setPluginState(PluginState.STARTED);

        doReturn(pluginWrapper).when(pluginManager).getPlugin(pluginId);
        doNothing().when(pluginManager).checkPluginId(pluginId);
        doReturn(new ArrayList<>(Arrays.asList(pluginWrapper))).when(pluginManager).getStartedPlugins();

        PluginState pluginState = pluginManager.stopPlugin(pluginId, false);
        verify(pluginWrapper.getPlugin()).stop();
        assertSame(PluginState.STOPPED, pluginState);
    }

    @Test
    void stopCreatedPlugin() {
        String pluginId = "plugin1";
        PluginWrapper pluginWrapper = createPluginWrapper(pluginId);

        doReturn(pluginWrapper).when(pluginManager).getPlugin(pluginId);
        doNothing().when(pluginManager).checkPluginId(pluginId);
        doReturn(new ArrayList<>(Arrays.asList(pluginWrapper))).when(pluginManager).getStartedPlugins();

        PluginState pluginState = pluginManager.stopPlugin(pluginId, false);
        verify(pluginWrapper.getPlugin(), never()).stop();
        assertSame(PluginState.CREATED, pluginState);
    }

    @Test
    void checkExistedPluginId() {
        String pluginId = "plugin1";
        PluginWrapper pluginWrapper = createPluginWrapper(pluginId);

        pluginManager.plugins.put("plugin1", pluginWrapper);
        pluginManager.checkPluginId("plugin1");
    }

    @Test
    void checkNotExistedPluginId() {
        assertThrows(PluginNotFoundException.class, () -> pluginManager.checkPluginId("plugin1"));
    }

    @Test
    void unloadPluginCallsResolveDependenciesOnce() {
        PluginWrapper pluginWrapper1 = createPluginWrapper("plugin1", "plugin2");
        PluginWrapper pluginWrapper2 = createPluginWrapper("plugin2");

        pluginManager.addPlugin(pluginWrapper1);
        pluginManager.addPlugin(pluginWrapper2);
        pluginManager.resolveDependencies();

        // reset the mock to not count the explicit call of resolveDependencies
        reset(pluginManager);

        pluginManager.unloadPlugin("plugin2", true);

        verify(pluginManager, times(1)).resolveDependencies();
    }

    private PluginWrapper createPluginWrapper(String pluginId, String... dependencies) {
        PluginDescriptor pluginDescriptor = new DefaultPluginDescriptor()
            .setPluginId(pluginId)
            .setPluginVersion("1.2.3")
            .setDependencies(String.join(", ", dependencies));

        return createPluginWrapper(pluginDescriptor);
    }

    private PluginWrapper createPluginWrapper(PluginDescriptor pluginDescriptor) {
        PluginWrapper pluginWrapper = new PluginWrapper(pluginManager, pluginDescriptor, Paths.get("plugin1"), getClass().getClassLoader());
        Plugin plugin = mock(Plugin.class);
        pluginWrapper.setPluginFactory(wrapper -> plugin);

        return pluginWrapper;
    }

    static abstract class AbstractPluginManagerWithDefaultVersionManager extends AbstractPluginManager {

        public AbstractPluginManagerWithDefaultVersionManager() {
            super();
        }

        @Override
        protected VersionManager createVersionManager() {
            return new DefaultVersionManager();
        }

    }

}
