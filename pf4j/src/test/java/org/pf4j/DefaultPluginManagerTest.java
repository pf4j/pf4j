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
import org.pf4j.test.JavaFileObjectUtils;
import org.pf4j.test.JavaSources;
import org.pf4j.test.PluginJar;
import org.pf4j.test.PluginZip;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultPluginManagerTest {

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
    void validateOK() {
        pluginManager.validatePluginDescriptor(pluginDescriptor);
    }

    @Test
    void validateFailsOnId() {
        pluginDescriptor.setPluginId("");
        assertThrows(PluginRuntimeException.class, () -> pluginManager.validatePluginDescriptor(pluginDescriptor));
    }

    @Test
    void validateFailsOnVersion() {
        pluginDescriptor.setPluginVersion(null);
        assertThrows(PluginRuntimeException.class, () -> pluginManager.validatePluginDescriptor(pluginDescriptor));
    }

    @Test
    void validateNoPluginClass() {
        pluginManager.validatePluginDescriptor(pluginDescriptor);
        assertEquals(Plugin.class.getName(), pluginDescriptor.getPluginClass());
    }

    @Test
    void isPluginValid() {
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
    void isPluginValidAllowExact() {
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
    void testDefaultExactVersionAllowed() {
        assertFalse(pluginManager.isExactVersionAllowed());
    }

    /**
     * Test that a disabled plugin doesn't start.
     * See <a href="https://github.com/pf4j/pf4j/issues/223">#223</a>.
     */
    @Test
    void testPluginDisabledNoStart() throws IOException {
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
    void deleteZipPlugin() throws Exception {
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
            .filter(event -> event.getPluginState().isUnloaded())
            .findFirst();

        assertTrue(unloadedEvent.isPresent());
    }

    @Test
    void deleteJarPlugin() throws Exception {
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
            .filter(event -> event.getPluginState().isUnloaded())
            .findFirst();

        assertTrue(unloadedEvent.isPresent());
    }

    @Test
    void loadedPluginWithMissingDependencyCanBeUnloaded() throws IOException {
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
    void deleteZipPluginForPluginThatHasNotBeenStartedPostsUnloadedEvent() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        pluginManager.loadPlugin(pluginZip.path());

        assertEquals(1, pluginManager.getPlugins().size());

        boolean deleted = pluginManager.deletePlugin(pluginZip.pluginId());
        assertTrue(deleted);

        assertFalse(pluginZip.file().exists());

        Optional<PluginStateEvent> unloadedEvent = receivedEvents.stream()
            .filter(event -> event.getPluginState().isUnloaded())
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

    @Test
    void deletePluginWithDependency() throws IOException {
        PluginZip pluginZip1 = new PluginZip.Builder(pluginsPath.resolve("my-first-plugin-1.1.1.zip"), "myPlugin1")
            .pluginVersion("1.1.1")
            .build();

        PluginZip pluginZip2 = new PluginZip.Builder(pluginsPath.resolve("my-second-plugin-2.2.2.zip"), "myPlugin2")
            .pluginVersion("2.2.2")
            .pluginDependencies("myPlugin1")
            .build();

        PluginZip pluginZip3 = new PluginZip.Builder(pluginsPath.resolve("my-third-plugin-3.3.3.zip"), "myPlugin3")
            .pluginVersion("3.3.3")
            .pluginDependencies("myPlugin2")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZip1.pluginId()).getPluginState());
        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZip2.pluginId()).getPluginState());
        assertEquals(PluginState.STARTED, pluginManager.getPlugin(pluginZip3.pluginId()).getPluginState());

        System.out.println("Stopping " + pluginZip2.pluginId());
        pluginManager.stopPlugin(pluginZip2.pluginId());
        assertEquals(PluginState.STOPPED, pluginManager.getPlugin(pluginZip2.pluginId()).getPluginState());

        boolean deleted = pluginManager.deletePlugin(pluginZip2.pluginId());
        assertTrue(deleted);

        assertEquals(1, pluginManager.getPlugins().size()); // myPlugin1 should still be there, myPlugin2 and myPlugin3 should be gone

        pluginManager.stopPlugin(pluginZip1.pluginId());
        assertEquals(PluginState.STOPPED, pluginManager.getPlugin(pluginZip1.pluginId()).getPluginState());
    }

    @Test
    void unloadPlugin() throws IOException, ClassNotFoundException {
        JavaFileObject object = JavaSources.compile(JavaSources.GREETING);
        String pluginClassName = JavaFileObjectUtils.getClassName(object);
        byte[] pluginBytes = JavaFileObjectUtils.getAllBytes(object);

        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .addFile(Paths.get("classes", "test", "Greeting.class"), pluginBytes)
            .build();

        pluginManager.loadPlugin(pluginZip.path());
        pluginManager.startPlugin(pluginZip.pluginId());

        assertEquals(1, pluginManager.getPlugins().size());

        PluginWrapper plugin = pluginManager.getPlugin(pluginZip.pluginId());
        ClassLoader classLoader = plugin.getPluginClassLoader();
        assertNotNull(classLoader);
        assertTrue(classLoader instanceof PluginClassLoader);
        PluginClassLoader pluginClassLoader = (PluginClassLoader) classLoader;

        Class<?> clazz = classLoader.loadClass(pluginClassName);
        assertNotNull(clazz);

        boolean unloaded = pluginManager.unloadPlugin(pluginZip.pluginId());
        assertTrue(unloaded);
        assertTrue(pluginManager.getPlugins().isEmpty());
        assertNull(pluginManager.getPluginClassLoader(pluginZip.pluginId()));
        assertTrue(pluginClassLoader.isClosed());
    }

    @Test
    void startPluginWithExceptionSetsStateToFailed() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("failing-plugin-1.0.0.zip"), "failingPlugin")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingPlugin")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugin("failingPlugin");

        PluginWrapper plugin = pluginManager.getPlugin("failingPlugin");
        assertEquals(PluginState.FAILED, plugin.getPluginState());
        assertNotNull(plugin.getFailedException());
        assertEquals("Intentional failure for testing", plugin.getFailedException().getMessage());
        assertFalse(pluginManager.getStartedPlugins().contains(plugin));
    }

    @Test
    void startPluginWithExceptionFiresStateEvent() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("failing-plugin-1.0.0.zip"), "failingPlugin")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingPlugin")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugin("failingPlugin");

        boolean failedEventFired = receivedEvents.stream()
            .anyMatch(event -> event.getPluginState() == PluginState.FAILED
                && event.getPlugin().getPluginId().equals("failingPlugin"));

        assertTrue(failedEventFired);
    }

    @Test
    void startPluginsWithExceptionSetsStateToFailed() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("failing-plugin-1.0.0.zip"), "failingPlugin")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingPlugin")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        PluginWrapper plugin = pluginManager.getPlugin("failingPlugin");
        assertEquals(PluginState.FAILED, plugin.getPluginState());
        assertNotNull(plugin.getFailedException());
        assertFalse(pluginManager.getStartedPlugins().contains(plugin));
    }

    @Test
    void shouldFailToStartPluginWhenRequiredDependencyFailsToStart() throws IOException {
        // Create a plugin that fails on start
        PluginZip dependency = new PluginZip.Builder(pluginsPath.resolve("failing-dep-1.0.0.zip"), "failingDep")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingPlugin")
            .build();

        // Create a plugin that depends on the failing plugin
        PluginZip dependent = new PluginZip.Builder(pluginsPath.resolve("dependent-1.0.0.zip"), "dependent")
            .pluginVersion("1.0.0")
            .pluginDependencies("failingDep@1.0.0")
            .build();

        pluginManager.loadPlugins();
        PluginState result = pluginManager.startPlugin("dependent");

        // Both plugins should be FAILED
        assertEquals(PluginState.FAILED, pluginManager.getPlugin("failingDep").getPluginState());
        assertEquals(PluginState.FAILED, pluginManager.getPlugin("dependent").getPluginState());
        assertEquals(PluginState.FAILED, result);

        // Verify the dependent plugin has the correct exception
        PluginWrapper dependentPlugin = pluginManager.getPlugin("dependent");
        assertNotNull(dependentPlugin.getFailedException());
        assertEquals("Required dependency 'failingDep' failed to start", dependentPlugin.getFailedException().getMessage());

        // Neither plugin should be in started plugins list
        assertFalse(pluginManager.getStartedPlugins().contains(pluginManager.getPlugin("failingDep")));
        assertFalse(pluginManager.getStartedPlugins().contains(dependentPlugin));
    }

    @Test
    void shouldStartPluginWhenOptionalDependencyFailsToStart() throws IOException {
        // Create a plugin that fails on start
        PluginZip dependency = new PluginZip.Builder(pluginsPath.resolve("failing-optional-dep-1.0.0.zip"), "failingOptionalDep")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingPlugin")
            .build();

        // Create a plugin with optional dependency on the failing plugin
        PluginZip dependent = new PluginZip.Builder(pluginsPath.resolve("dependent-optional-1.0.0.zip"), "dependentOptional")
            .pluginVersion("1.0.0")
            .pluginDependencies("failingOptionalDep?@1.0.0")
            .build();

        pluginManager.loadPlugins();
        PluginState result = pluginManager.startPlugin("dependentOptional");

        // Dependency should be FAILED, but dependent should be STARTED
        assertEquals(PluginState.FAILED, pluginManager.getPlugin("failingOptionalDep").getPluginState());
        assertEquals(PluginState.STARTED, pluginManager.getPlugin("dependentOptional").getPluginState());
        assertEquals(PluginState.STARTED, result);

        // Only the dependent plugin should be in started plugins list
        assertFalse(pluginManager.getStartedPlugins().contains(pluginManager.getPlugin("failingOptionalDep")));
        assertTrue(pluginManager.getStartedPlugins().contains(pluginManager.getPlugin("dependentOptional")));
    }

    @Test
    void shouldHandleCascadingDependencyFailures() throws IOException {
        // Create a chain: pluginC -> pluginB -> pluginA
        // pluginA will fail to start
        PluginZip pluginA = new PluginZip.Builder(pluginsPath.resolve("plugin-a-1.0.0.zip"), "pluginA")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingPlugin")
            .build();

        PluginZip pluginB = new PluginZip.Builder(pluginsPath.resolve("plugin-b-1.0.0.zip"), "pluginB")
            .pluginVersion("1.0.0")
            .pluginDependencies("pluginA@1.0.0")
            .build();

        PluginZip pluginC = new PluginZip.Builder(pluginsPath.resolve("plugin-c-1.0.0.zip"), "pluginC")
            .pluginVersion("1.0.0")
            .pluginDependencies("pluginB@1.0.0")
            .build();

        pluginManager.loadPlugins();
        PluginState result = pluginManager.startPlugin("pluginC");

        // All plugins should be FAILED due to cascading failure
        assertEquals(PluginState.FAILED, pluginManager.getPlugin("pluginA").getPluginState());
        assertEquals(PluginState.FAILED, pluginManager.getPlugin("pluginB").getPluginState());
        assertEquals(PluginState.FAILED, pluginManager.getPlugin("pluginC").getPluginState());
        assertEquals(PluginState.FAILED, result);

        // Verify error messages
        assertEquals("Intentional failure for testing", pluginManager.getPlugin("pluginA").getFailedException().getMessage());
        assertEquals("Required dependency 'pluginA' failed to start", pluginManager.getPlugin("pluginB").getFailedException().getMessage());
        assertEquals("Required dependency 'pluginB' failed to start", pluginManager.getPlugin("pluginC").getFailedException().getMessage());

        // No plugins should be started
        assertTrue(pluginManager.getStartedPlugins().isEmpty());
    }

    @Test
    void invalidPluginSetsFailedException() throws IOException {
        // Set system version to 2.0.0
        pluginManager.setSystemVersion("2.0.0");

        // Create plugin that requires version 3.0.0
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("invalid-plugin-1.0.0.zip"), "invalidPlugin")
            .pluginVersion("1.0.0")
            .pluginRequires("3.0.0")
            .build();

        pluginManager.loadPlugins();

        PluginWrapper plugin = pluginManager.getPlugin("invalidPlugin");
        assertEquals(PluginState.DISABLED, plugin.getPluginState());
        assertNotNull(plugin.getFailedException());
        assertTrue(plugin.getFailedException().getMessage().contains("validation failed"));
    }

    @Test
    void stopPluginsWithExceptionSetsFailedException() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("failing-stop-plugin-1.0.0.zip"), "failingStopPlugin")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingStopPlugin")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugin("failingStopPlugin");

        PluginWrapper plugin = pluginManager.getPlugin("failingStopPlugin");
        assertEquals(PluginState.STARTED, plugin.getPluginState());

        // Stop all plugins - this will trigger exception in stop()
        pluginManager.stopPlugins();

        assertEquals(PluginState.FAILED, plugin.getPluginState());
        assertNotNull(plugin.getFailedException());
        assertTrue(plugin.getFailedException().getMessage().contains("stop"));
    }

    @Test
    void enableInvalidPluginSetsFailedException() throws IOException {
        // Set system version to 2.0.0
        pluginManager.setSystemVersion("2.0.0");

        // Create plugin that requires version 3.0.0
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("invalid-enable-plugin-1.0.0.zip"), "invalidEnablePlugin")
            .pluginVersion("1.0.0")
            .pluginRequires("3.0.0")
            .build();

        pluginManager.loadPlugins();

        PluginWrapper plugin = pluginManager.getPlugin("invalidEnablePlugin");
        assertEquals(PluginState.DISABLED, plugin.getPluginState());

        // Try to enable the invalid plugin
        boolean result = pluginManager.enablePlugin("invalidEnablePlugin");

        assertFalse(result);
        assertNotNull(plugin.getFailedException());
        assertTrue(plugin.getFailedException().getMessage().contains("validation failed"));
    }

    @Test
    void disablePluginWithStopFailureSetsFailedException() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("failing-disable-plugin-1.0.0.zip"), "failingDisablePlugin")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingStopPlugin")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugin("failingDisablePlugin");

        PluginWrapper plugin = pluginManager.getPlugin("failingDisablePlugin");
        assertEquals(PluginState.STARTED, plugin.getPluginState());

        // Try to disable - stop will fail
        boolean result = pluginManager.disablePlugin("failingDisablePlugin");

        assertFalse(result);
        assertNotNull(plugin.getFailedException());
        assertTrue(plugin.getFailedException().getMessage().contains("stop"));
    }

    @Test
    void deletePluginWithStopFailureSetsFailedException() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("failing-delete-plugin-1.0.0.zip"), "failingDeletePlugin")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingStopPlugin")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugin("failingDeletePlugin");

        PluginWrapper plugin = pluginManager.getPlugin("failingDeletePlugin");
        assertEquals(PluginState.STARTED, plugin.getPluginState());

        // Try to delete - stop will fail
        boolean result = pluginManager.deletePlugin("failingDeletePlugin");

        assertFalse(result);
        assertNotNull(plugin.getFailedException());
        assertTrue(plugin.getFailedException().getMessage().contains("stop"));
    }

    @Test
    void unloadPluginWithStopFailureSetsFailedException() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("failing-unload-plugin-1.0.0.zip"), "failingUnloadPlugin")
            .pluginVersion("1.0.0")
            .pluginClass("org.pf4j.test.FailingStopPlugin")
            .build();

        pluginManager.loadPlugins();
        pluginManager.startPlugin("failingUnloadPlugin");

        PluginWrapper plugin = pluginManager.getPlugin("failingUnloadPlugin");
        assertEquals(PluginState.STARTED, plugin.getPluginState());

        // Unload - stop will fail
        boolean result = pluginManager.unloadPlugin("failingUnloadPlugin");

        // unloadPlugin continues even if stop fails, so result is true
        assertTrue(result);
        // Plugin should be in UNLOADED state now (set after the exception)
        assertEquals(PluginState.UNLOADED, plugin.getPluginState());
        // But failedException should be set from the stop failure
        assertNotNull(plugin.getFailedException());
        assertTrue(plugin.getFailedException().getMessage().contains("stop"));
    }

}
