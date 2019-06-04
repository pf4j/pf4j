package org.pf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.plugin.PluginJar;

public class LegacyExtensionFinderTest {

    @TempDir
    public Path pluginsPath;

    public static class TestPlugin extends Plugin {

        public TestPlugin(PluginWrapper wrapper) {
            super(wrapper);
        }
    }

    @Extension
    public static class TestExtension implements ExtensionPoint {

    }

    @Test
    public void shouldUnlockFileAfterReadingExtensionsFromPlugin() throws IOException, PluginException {

        final Path pluginJarPath = pluginsPath.resolve("test-plugin.jar");
        PluginJar pluginJar = new PluginJar.Builder(pluginJarPath, "test-plugin")
                .pluginClass(TestPlugin.class.getName())
                .pluginVersion("1.2.3")
                .extension(TestExtension.class.getName())
                .build();

        JarPluginManager pluginManager = new JarPluginManager(pluginsPath);

        pluginManager.loadPlugins();
        LegacyExtensionFinder extensionFinder = new LegacyExtensionFinder(pluginManager);
        Map<String, Set<String>> pluginStorages = extensionFinder.readPluginsStorages();
        pluginManager.unloadPlugin(pluginJar.pluginId());
        boolean fileDeleted = pluginJarPath.toFile().delete();

        assertThat(pluginStorages, is(notNullValue()));
        assertThat(pluginStorages.get(pluginJar.pluginId()), is(notNullValue()));
        assertThat(pluginStorages.get(pluginJar.pluginId()).size(), is(equalTo(1)));
        assertThat(pluginStorages.get(pluginJar.pluginId()),
                contains("org.pf4j.LegacyExtensionFinderTest$TestExtension"));
        assertThat(fileDeleted, is(equalTo(true)));
    }

}
