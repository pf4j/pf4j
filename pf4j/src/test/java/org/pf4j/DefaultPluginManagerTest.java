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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.test.PluginJar;
import org.pf4j.test.PluginZip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultPluginManagerTest {

    private DefaultPluginManager pluginManager;
    private DefaultPluginDescriptor pluginDescriptor;
    private PluginWrapper pluginWrapper;

    private List<PluginStateEvent> receivedEvents;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    public void setUp() throws IOException {
        receivedEvents = new ArrayList<>();

        pluginManager = new DefaultPluginManager(pluginsPath);
        pluginManager.addPluginStateListener(event -> receivedEvents.add(event));

        pluginDescriptor = new DefaultPluginDescriptor();
        pluginDescriptor.setPluginId("myPlugin");
        pluginDescriptor.setPluginVersion("1.2.3");
        pluginDescriptor.setPluginDescription("My plugin");
        pluginDescriptor.setDependencies("bar, baz");
        pluginDescriptor.setProvider("Me");
        pluginDescriptor.setRequires("5.0.0");

        pluginWrapper = new PluginWrapper(pluginManager, pluginDescriptor, Files.createTempDirectory("test"), getClass().getClassLoader());
    }

    @AfterEach
    public void tearDown() {
        pluginManager = null;
        pluginDescriptor = null;
        pluginWrapper = null;
        receivedEvents = null;
    }

    @Test
    public void validateOK() {
        pluginManager.validatePluginDescriptor(pluginDescriptor);
    }

    @Test
    public void validateFailsOnId() {
        pluginDescriptor.setPluginId("");
        assertThrows(PluginRuntimeException.class, () -> pluginManager.validatePluginDescriptor(pluginDescriptor));
    }

    @Test
    public void validateFailsOnVersion() {
        pluginDescriptor.setPluginVersion(null);
        assertThrows(PluginRuntimeException.class, () -> pluginManager.validatePluginDescriptor(pluginDescriptor));
    }

    @Test
    public void validateNoPluginClass() {
        pluginManager.validatePluginDescriptor(pluginDescriptor);
        assertEquals(Plugin.class.getName(), pluginDescriptor.getPluginClass());
    }

    @Test
    public void isPluginValid() {
        // By default, accept all since system version not given
        assertTrue(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("1.0.0");
        assertFalse(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("5.0.0");
        assertTrue(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("6.0.0");
        assertTrue(pluginManager.isPluginValid(pluginWrapper));
    }

    @Test
    public void isPluginValidAllowExact() {
        pluginManager.setExactVersionAllowed(true);

        // By default, accept all since system version not given
        assertTrue(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("1.0.0");
        assertFalse(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("5.0.0");
        assertTrue(pluginManager.isPluginValid(pluginWrapper));

        pluginManager.setSystemVersion("6.0.0");
        assertFalse(pluginManager.isPluginValid(pluginWrapper));
    }

    @Test
    public void testDefaultExactVersionAllowed() {
        assertFalse(pluginManager.isExactVersionAllowed());
    }

    /**
     * Test that a disabled plugin doesn't start.
     * See <a href="https://github.com/pf4j/pf4j/issues/223">#223</a>.
     */
    @Test
    public void testPluginDisabledNoStart() throws IOException {
        new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        final PluginStatusProvider statusProvider = mock(PluginStatusProvider.class);
        when(statusProvider.isPluginDisabled("myPlugin")).thenReturn(true);

        PluginManager pluginManager = new DefaultPluginManager(pluginsPath) {

            @Override
            protected PluginStatusProvider createPluginStatusProvider() {
                return statusProvider;
            }

        };

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals(0, pluginManager.getStartedPlugins().size());

        PluginWrapper plugin = pluginManager.getPlugin("myPlugin");
        assertSame(PluginState.DISABLED, plugin.getPluginState());
    }

    @Test
    void shouldDisablePluginAfterStarting() throws IOException {
        new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertEquals(PluginState.STARTED, pluginManager.getPlugin("myPlugin").getPluginState());
        pluginManager.disablePlugin("myPlugin");
        assertEquals(PluginState.DISABLED, pluginManager.getPlugin("myPlugin").getPluginState());
    }

    @Test
    public void deleteZipPlugin() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugin(pluginZip.path());
        pluginManager.startPlugin(pluginZip.pluginId());

        assertEquals(1, pluginManager.getPlugins().size());

        boolean deleted = pluginManager.deletePlugin(pluginZip.pluginId());
        assertTrue(deleted);

        assertFalse(pluginZip.file().exists());

        Optional<PluginStateEvent> unloadedEvent = receivedEvents.stream()
            .filter(event -> event.getPluginState() == PluginState.UNLOADED)
            .findFirst();

        assertTrue(unloadedEvent.isPresent());
    }

    @Test
    public void deleteJarPlugin() throws Exception {
        PluginJar pluginJar = new PluginJar.Builder(pluginsPath.resolve("my-plugin-1.2.3.jar"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugin(pluginJar.path());
        pluginManager.startPlugin(pluginJar.pluginId());

        assertEquals(1, pluginManager.getPlugins().size());

        boolean deleted = pluginManager.deletePlugin(pluginJar.pluginId());
        assertTrue(deleted);

        assertFalse(pluginJar.file().exists());

        Optional<PluginStateEvent> unloadedEvent = receivedEvents.stream()
            .filter(event -> event.getPluginState() == PluginState.UNLOADED)
            .findFirst();

        assertTrue(unloadedEvent.isPresent());
    }

    @Test
    public void loadedPluginWithMissingDependencyCanBeUnloaded() throws IOException {
        pluginManager.setResolveRecoveryStrategy(AbstractPluginManager.ResolveRecoveryStrategy.IGNORE_PLUGIN_AND_CONTINUE);

        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.1.1.zip"), "myPlugin")
            .pluginVersion("1.1.1")
            .pluginDependencies("myBasePlugin")
            .build();

        // try to load the plugin with a missing dependency
        pluginManager.loadPlugin(pluginZip.path());

        // the plugin is unloaded automatically
        assertTrue(pluginManager.getPlugins().isEmpty());

        // start plugins
        pluginManager.startPlugins();

        assertTrue(pluginManager.getStartedPlugins().isEmpty());
        assertTrue(pluginManager.getUnresolvedPlugins().isEmpty());
        assertTrue(pluginManager.getResolvedPlugins().isEmpty());
        assertTrue(pluginManager.getPlugins().isEmpty());
    }

    @Test
    void loadingPluginWithMissingDependencyDoesNotBreakOtherPlugins() throws IOException {
        pluginManager.setResolveRecoveryStrategy(AbstractPluginManager.ResolveRecoveryStrategy.IGNORE_PLUGIN_AND_CONTINUE);

        // Load 2 plugins, one with a dependency that is missing and one without any dependencies.
        PluginZip pluginZip1 = new PluginZip.Builder(pluginsPath.resolve("my-first-plugin-1.1.1.zip"), "myPlugin1")
            .pluginVersion("1.1.1")
            .pluginDependencies("myBasePlugin")
            .build();

        PluginZip pluginZip2 = new PluginZip.Builder(pluginsPath.resolve("my-second-plugin-2.2.2.zip"), "myPlugin2")
            .pluginVersion("2.2.2")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        // myPlugin2 should have been started at this point.
        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZip2.pluginId()).getPluginState());

        pluginManager.unloadPlugin(pluginZip1.pluginId());

        // No traces should remain of myPlugin1.
        assertTrue(
            pluginManager.getUnresolvedPlugins().stream()
                .noneMatch(pluginWrapper -> pluginWrapper.getPluginId().equals(pluginZip1.pluginId()))
        );

        pluginManager.unloadPlugin(pluginZip2.pluginId());

        // Load the missing dependency, everything should start working.
        PluginZip pluginZipBase = new PluginZip.Builder(pluginsPath.resolve("my-base-plugin-3.0.0.zip"), "myBasePlugin")
            .pluginVersion("3.0.0")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZip1.pluginId()).getPluginState());
        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZip2.pluginId()).getPluginState());
        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZipBase.pluginId()).getPluginState());

        assertTrue(pluginManager.getUnresolvedPlugins().isEmpty());
    }

    @Test
    void loadingPluginWithMissingDependencyFails() throws IOException {
        pluginManager.setResolveRecoveryStrategy(AbstractPluginManager.ResolveRecoveryStrategy.THROW_EXCEPTION);

        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.1.1.zip"), "myPlugin")
            .pluginVersion("1.1.1")
            .pluginDependencies("myBasePlugin")
            .build();

        // try to load the plugin with a missing dependency
        Path pluginPath = pluginZip.path();
        assertThrows(DependencyResolver.DependenciesNotFoundException.class, () -> pluginManager.loadPlugin(pluginPath));
    }

    @Test
    void loadingPluginWithWrongDependencyVersionFails() throws IOException {
        pluginManager.setResolveRecoveryStrategy(AbstractPluginManager.ResolveRecoveryStrategy.THROW_EXCEPTION);

        PluginZip pluginZip1 = new PluginZip.Builder(pluginsPath.resolve("my-first-plugin-1.1.1.zip"), "myPlugin1")
            .pluginVersion("1.1.1")
            .pluginDependencies("myPlugin2@3.0.0")
            .build();

        PluginZip pluginZip2 = new PluginZip.Builder(pluginsPath.resolve("my-second-plugin-2.2.2.zip"), "myPlugin2")
            .pluginVersion("2.2.2")
            .build();

        // try to load the plugins with cyclic dependencies
        Path pluginPath1 = pluginZip1.path();
        Path pluginPath2 = pluginZip2.path();
        assertThrows(DependencyResolver.DependenciesWrongVersionException.class, () -> pluginManager.loadPlugins());
    }

    @Test
    void loadingPluginsWithCyclicDependenciesFails() throws IOException {
        PluginZip pluginZip1 = new PluginZip.Builder(pluginsPath.resolve("my-first-plugin-1.1.1.zip"), "myPlugin1")
            .pluginVersion("1.1.1")
            .pluginDependencies("myPlugin2")
            .build();

        PluginZip pluginZip2 = new PluginZip.Builder(pluginsPath.resolve("my-second-plugin-2.2.2.zip"), "myPlugin2")
            .pluginVersion("2.2.2")
            .pluginDependencies("myPlugin1")
            .build();

        // try to load the plugins with cyclic dependencies
        Path pluginPath1 = pluginZip1.path();
        Path pluginPath2 = pluginZip2.path();
        assertThrows(DependencyResolver.CyclicDependencyException.class, () -> pluginManager.loadPlugins());
    }

    @Test
    public void deleteZipPluginForPluginThatHasNotBeenStartedPostsUnloadedEvent() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugin(pluginZip.path());

        assertEquals(1, pluginManager.getPlugins().size());

        boolean deleted = pluginManager.deletePlugin(pluginZip.pluginId());
        assertTrue(deleted);

        assertFalse(pluginZip.file().exists());

        Optional<PluginStateEvent> unloadedEvent = receivedEvents.stream()
            .filter(event -> event.getPluginState() == PluginState.UNLOADED)
            .findFirst();

        assertTrue(unloadedEvent.isPresent());
    }

    @Test
    void stopPluginWithDeletedDependency() throws IOException {
        PluginZip pluginZip1 = new PluginZip.Builder(pluginsPath.resolve("my-first-plugin-1.1.1.zip"), "myPlugin1")
            .pluginVersion("1.1.1")
            .build();

        PluginZip pluginZip2 = new PluginZip.Builder(pluginsPath.resolve("my-second-plugin-2.2.2.zip"), "myPlugin2")
            .pluginVersion("2.2.2")
            .pluginDependencies("myPlugin1")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZip1.pluginId()).getPluginState());
        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZip2.pluginId()).getPluginState());

        System.out.println("Stopping " + pluginZip1.pluginId());
        pluginManager.stopPlugin(pluginZip1.pluginId());
        assertEquals(PluginState.STOPPED, pluginManager.getPlugin(pluginZip1.pluginId()).getPluginState());

        boolean deleted = pluginManager.deletePlugin(pluginZip1.pluginId());
        assertTrue(deleted);

        assertEquals(0, pluginManager.getPlugins().size());

        assertThrows(PluginNotFoundException.class, () -> pluginManager.stopPlugin(pluginZip2.pluginId()));
    }

}
