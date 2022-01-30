package org.pf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.test.PluginJar;
import org.pf4j.test.PluginManifest;
import org.pf4j.test.TestExtension;
import org.pf4j.test.TestExtensionPoint;
import org.pf4j.test.TestPlugin;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurePluginManagerWrapperTest {

    private static final String OTHER_PLUGIN_ID = "test-plugin-2";
    private static final String THIS_PLUGIN_ID = "test-plugin-1";

    private PluginJar thisPlugin;
    private PluginJar otherPlugin;
    private PluginManager pluginManager;
    private PluginManager wrappedPluginManager;
    private int pluginManagerEvents = 0;
    private int wrappedPluginManagerEvents = 0;

    @TempDir
    Path pluginsPath;

    @BeforeEach
    void setUp() throws IOException {
        pluginManagerEvents = 0;
        wrappedPluginManagerEvents = 0;
        PluginManifest plugin1Manifest = new PluginManifest.Builder(THIS_PLUGIN_ID)
            .pluginClass(TestPlugin.class.getName())
            .pluginVersion("1.2.3")
            .build();

        PluginManifest plugin2Manifest = new PluginManifest.Builder(OTHER_PLUGIN_ID)
            .pluginClass(TestPlugin.class.getName())
            .pluginVersion("1.2.3")
            .build();
        thisPlugin = new PluginJar.Builder(pluginsPath.resolve("test-plugin1.jar"), plugin1Manifest)
            .extension(TestExtension.class.getName())
            .build();
        otherPlugin = new PluginJar.Builder(pluginsPath.resolve("test-plugin2.jar"), plugin2Manifest)
            .extension(TestExtension.class.getName())
            .build();

        pluginManager = new JarPluginManager(pluginsPath);
        wrappedPluginManager = new SecurePluginManagerWrapper(pluginManager, THIS_PLUGIN_ID);
    }

    @AfterEach
    void tearDown() {
        pluginManager.unloadPlugins();

        thisPlugin = null;
        otherPlugin = null;
        pluginManager = null;
    }

    @Test
    void pluginStateListeners() {
        pluginManager.addPluginStateListener(event -> pluginManagerEvents++);
        wrappedPluginManager.addPluginStateListener(event -> wrappedPluginManagerEvents++);
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertEquals(4, pluginManagerEvents);
        assertEquals(2, wrappedPluginManagerEvents);
    }

    @Test
    void deletePlugin() {
        pluginManager.loadPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.deletePlugin(OTHER_PLUGIN_ID));
        assertTrue(wrappedPluginManager.deletePlugin(THIS_PLUGIN_ID));
    }

    @Test
    void disablePlugin() {
        pluginManager.loadPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.disablePlugin(OTHER_PLUGIN_ID));
        assertTrue(wrappedPluginManager.disablePlugin(THIS_PLUGIN_ID));
    }

    @Test
    void enablePlugin() {
        pluginManager.loadPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.enablePlugin(OTHER_PLUGIN_ID));
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.enablePlugin(THIS_PLUGIN_ID));
    }

    @Test
    void getExtensionClasses() {
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertEquals(1, wrappedPluginManager.getExtensionClasses(TestExtensionPoint.class).size());

        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getExtensionClasses(TestExtensionPoint.class, OTHER_PLUGIN_ID));
        assertEquals(1, wrappedPluginManager.getExtensionClasses(TestExtensionPoint.class, THIS_PLUGIN_ID).size());

        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getExtensionClasses(OTHER_PLUGIN_ID));
        assertEquals(1, wrappedPluginManager.getExtensionClasses(THIS_PLUGIN_ID).size());
    }

    @Test
    void getExtensionClassNames() {
        pluginManager.loadPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getExtensionClassNames(OTHER_PLUGIN_ID));
        assertEquals(1, wrappedPluginManager.getExtensionClassNames(THIS_PLUGIN_ID).size());
    }

    @Test
    void getExtensionFactory() {
        pluginManager.loadPlugins();
        assertEquals(pluginManager.getExtensionFactory(), wrappedPluginManager.getExtensionFactory());
    }

    @Test
    void getExtensions() {
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertEquals(1, wrappedPluginManager.getExtensions(TestExtensionPoint.class).size());

        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getExtensions(TestExtensionPoint.class, OTHER_PLUGIN_ID));
        assertEquals(1, wrappedPluginManager.getExtensions(TestExtensionPoint.class, THIS_PLUGIN_ID).size());

        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getExtensions(OTHER_PLUGIN_ID));
        assertEquals(1, wrappedPluginManager.getExtensions(THIS_PLUGIN_ID).size());
    }

    @Test
    void getPlugin() {
        pluginManager.loadPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getPlugin(OTHER_PLUGIN_ID));
        assertEquals(THIS_PLUGIN_ID, wrappedPluginManager.getPlugin(THIS_PLUGIN_ID).getPluginId());
    }

    @Test
    void getPluginClassLoader() {
        pluginManager.loadPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getPluginClassLoader(OTHER_PLUGIN_ID));
        assertNotNull(wrappedPluginManager.getPluginClassLoader(THIS_PLUGIN_ID));
    }

    @Test
    void getPlugins() {
        pluginManager.loadPlugins();
        assertEquals(2, pluginManager.getPlugins().size());
        assertEquals(1, wrappedPluginManager.getPlugins().size());
    }

    @Test
    void getPluginsRoot() {
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getPluginsRoot());
    }

    @Test
    void getPluginsRoots() {
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.getPluginsRoots());
    }

    @Test
    void getResolvedPlugins() {
        pluginManager.loadPlugins();
        assertEquals(2, pluginManager.getResolvedPlugins().size());
        assertEquals(1, wrappedPluginManager.getResolvedPlugins().size());
    }

    @Test
    void getRuntimeMode() {
        assertEquals(pluginManager.getRuntimeMode(), wrappedPluginManager.getRuntimeMode());
    }

    @Test
    void getStartedPlugins() {
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertEquals(2, pluginManager.getStartedPlugins().size());
        assertEquals(1, wrappedPluginManager.getStartedPlugins().size());
    }

    @Test
    void getSystemVersion() {
        assertEquals(pluginManager.getSystemVersion(), wrappedPluginManager.getSystemVersion());
    }

    @Test
    void getUnresolvedPlugins() {
        assertNotNull(wrappedPluginManager);
        assertNotNull(wrappedPluginManager.getUnresolvedPlugins());
        assertTrue(wrappedPluginManager.getUnresolvedPlugins().isEmpty());
    }

    @Test
    void getVersionManager() {
        assertEquals(pluginManager.getVersionManager(), wrappedPluginManager.getVersionManager());
    }

    @Test
    void isDevelopment() {
        assertEquals(pluginManager.isDevelopment(), wrappedPluginManager.isDevelopment());
    }

    @Test
    void isNotDevelopment() {
        assertEquals(pluginManager.isNotDevelopment(), wrappedPluginManager.isNotDevelopment());
    }

    @Test
    void loadPlugin() {
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.loadPlugin(thisPlugin.path()));
    }

    @Test
    void loadPlugins() {
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.loadPlugins());
    }

    @Test
    void setSystemVersion() {
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.setSystemVersion("1.0.0"));
    }

    @Test
    void startPlugin() {
        pluginManager.loadPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.startPlugin(OTHER_PLUGIN_ID));
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.startPlugin(THIS_PLUGIN_ID));
    }

    @Test
    void startPlugins() {
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.startPlugins());
    }

    @Test
    void stopPlugin() {
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.stopPlugin(OTHER_PLUGIN_ID));
        assertEquals(PluginState.STOPPED, wrappedPluginManager.stopPlugin(THIS_PLUGIN_ID));
    }

    @Test
    void stopPlugins() {
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.stopPlugins());
    }

    @Test
    void unloadPlugin() {
        pluginManager.loadPlugins();
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.unloadPlugin(OTHER_PLUGIN_ID));
        assertTrue(wrappedPluginManager.unloadPlugin(THIS_PLUGIN_ID));
    }

    @Test
    void unloadPlugins() {
        assertThrows(IllegalAccessError.class, () -> wrappedPluginManager.unloadPlugins());
    }

    @Test
    void whichPlugin() {
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertEquals(null, wrappedPluginManager.whichPlugin(pluginManager.getExtensionClasses(OTHER_PLUGIN_ID).get(0)));
        assertEquals(THIS_PLUGIN_ID, wrappedPluginManager.whichPlugin(pluginManager.getExtensionClasses(THIS_PLUGIN_ID).get(0)).getPluginId());
    }

}
