/*
 * Copyright 2017 Decebal Suiu
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

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.Assert.*;

public class LoadPluginsTest {

    private Path tmpDir;
    private DefaultPluginManager pluginManager;
    private MockZipPlugin p1;
    private MockZipPlugin p2;
    private MockZipPlugin p3;

    @Before
    public void setup() throws IOException {
        tmpDir = Files.createTempDirectory("pf4j-test");
        tmpDir.toFile().deleteOnExit();
        p1 = new MockZipPlugin("myPlugin", "1.2.3", "my-plugin-1.2.3", "my-plugin-1.2.3.zip");
        p2 = new MockZipPlugin("myPlugin", "2.0.0", "my-plugin-2.0.0", "my-plugin-2.0.0.ZIP");
        p3 = new MockZipPlugin("other", "3.0.0", "other-3.0.0", "other-3.0.0.Zip");
        pluginManager = new DefaultPluginManager(tmpDir) {

            @Override
            protected CompoundPluginDescriptorFinder createPluginDescriptorFinder() {
                return new CompoundPluginDescriptorFinder()
                    .add(new PropertiesPluginDescriptorFinder("my.properties"));
            }

        };
    }

    @Test
    public void load() throws Exception {
        p1.create();
        assertTrue(Files.exists(p1.zipFile));
        assertEquals(0, pluginManager.getPlugins().size());
        pluginManager.loadPlugins();
        assertTrue(Files.exists(p1.zipFile));
        assertTrue(Files.exists(p1.unzipped));
        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals(p1.id, pluginManager.idForPath(p1.unzipped));
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadNonExisting() throws Exception {
        pluginManager.loadPlugin(Paths.get("nonexisting"));
    }

    @Test
    public void loadTwiceFails() throws Exception {
        p1.create();
        assertNotNull(pluginManager.loadPluginFromPath(p1.zipFile));
        assertNull(pluginManager.loadPluginFromPath(p1.zipFile));
    }

    @Test
    public void loadUnloadLoad() throws Exception {
        p1.create();
        pluginManager.loadPlugins();
        assertEquals(1, pluginManager.getPlugins().size());
        assertTrue(pluginManager.unloadPlugin(pluginManager.idForPath(p1.unzipped)));
        // duplicate check
        assertNull(pluginManager.idForPath(p1.unzipped));
        // Double unload ok
        assertFalse(pluginManager.unloadPlugin(pluginManager.idForPath(p1.unzipped)));
        assertNotNull(pluginManager.loadPlugin(p1.unzipped));
    }

    @Test
    public void upgrade() throws Exception {
        p1.create();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals("1.2.3", pluginManager.getPlugin(p2.id).getDescriptor().getVersion());
        assertEquals(1, pluginManager.getStartedPlugins().size());
        p2.create();
        pluginManager.loadPlugins();
        pluginManager.startPlugin(p2.id);
        assertEquals(1, pluginManager.getPlugins().size());
        assertEquals("2.0.0", pluginManager.getPlugin(p2.id).getDescriptor().getVersion());
        assertEquals("2.0.0", pluginManager.getStartedPlugins().get(1).getDescriptor().getVersion());
    }

    @Test
    public void getRoot() throws Exception {
        assertEquals(tmpDir, pluginManager.getPluginsRoot());
    }

    @Test
    public void notAPlugin() throws Exception {
        Path notAPlugin = tmpDir.resolve("not-a-zip");
        Files.createFile(notAPlugin);
        pluginManager.loadPlugins();
        assertEquals(0, pluginManager.getPlugins().size());
    }

    @Test
    public void deletePlugin() throws Exception {
        p1.create();
        p3.create();
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        assertEquals(2, pluginManager.getPlugins().size());
        pluginManager.deletePlugin(p1.id);
        assertEquals(1, pluginManager.getPlugins().size());
        assertFalse(Files.exists(p1.zipFile));
        assertFalse(Files.exists(p1.unzipped));
        assertTrue(Files.exists(p3.zipFile));
        assertTrue(Files.exists(p3.unzipped));
    }

    private class MockZipPlugin {

        public final String id;
        public final String version;
        public final String filename;
        public final Path zipFile;
        public final Path unzipped;
        public final Path propsFile;
        public final URI fileURI;
        public String zipname;

        public MockZipPlugin(String id, String version, String filename, String zipname) throws IOException {
            this.id = id;
            this.version = version;
            this.filename = filename;
            this.zipname = zipname;

            zipFile = tmpDir.resolve(zipname).toAbsolutePath();
            unzipped = tmpDir.resolve(filename);
            propsFile = tmpDir.resolve("my.properties");
            fileURI = URI.create("jar:file:"+zipFile.toString());
        }

        public void create() throws IOException {
            try (FileSystem zipfs = FileSystems.newFileSystem(fileURI, Collections.singletonMap("create", "true"))) {
                Path propsInZip = zipfs.getPath("/" + propsFile.getFileName().toString());
                BufferedWriter br = new BufferedWriter(new FileWriter(propsFile.toString()));
                br.write("plugin.id=" + id);
                br.newLine();
                br.write("plugin.version=" + version);
                br.newLine();
                br.write("plugin.class=org.pf4j.plugin.TestPlugin");
                br.close();
                Files.move(propsFile, propsInZip);
            }
        }

    }

}
