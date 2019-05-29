package org.pf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LegacyExtensionFinderTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void shouldUnlockFileAfterReadingExtensionsFromPlugin() throws IOException, URISyntaxException {
        File pluginsFolder = testFolder.newFolder("pluginsFolder");
        Path dummyPluginJarResource = Paths.get(getClass().getClassLoader().getResource("DummyPlugin.jar").toURI());
        Path pluginJar = pluginsFolder.toPath().resolve(dummyPluginJarResource.getFileName());
        Files.copy(dummyPluginJarResource, pluginJar);

        PluginManager pluginManager = new DefaultPluginManager(pluginsFolder.toPath());
        pluginManager.loadPlugins();
        LegacyExtensionFinder extensionFinder = new LegacyExtensionFinder(pluginManager);
        Map<String, Set<String>> pluginStorages = extensionFinder.readPluginsStorages();
        pluginManager.unloadPlugin("dummy-plugin");
        boolean fileDeleted = pluginJar.toFile().delete();

        assertThat(pluginStorages, is(notNullValue()));
        assertThat(pluginStorages.get("dummy-plugin"), is(notNullValue()));
        assertThat(pluginStorages.get("dummy-plugin").size(), is(equalTo(1)));
        assertThat(pluginStorages.get("dummy-plugin"), contains("org.pf4jtest.DummyPlugin$DummyExtension"));
        assertThat(fileDeleted, is(equalTo(true)));
    }

}
