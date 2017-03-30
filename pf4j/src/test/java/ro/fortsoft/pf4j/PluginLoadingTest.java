package ro.fortsoft.pf4j;

import org.junit.Test;

import java.nio.file.Paths;

import static junit.framework.TestCase.assertNotNull;

/**
 * Test loading of plugins
 */
public class PluginLoadingTest {
    @Test
    public void testPluginLoader() throws ClassNotFoundException {
        PluginManager manager = new DefaultPluginManager();
        assertNotNull(manager.loadPlugin(Paths.get("src/test/resources/plugin-with-jar")));
        // next line will fail if FileUtils.getJars() needs absolute path
        manager.getPluginClassLoader("welcome-plugin").loadClass("org.apache.commons.lang.ArrayUtils");
    }

}
