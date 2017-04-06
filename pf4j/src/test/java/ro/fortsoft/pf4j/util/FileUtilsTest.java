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
package ro.fortsoft.pf4j.util;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;

import static org.junit.Assert.*;

public class FileUtilsTest {
    private Path zipFile;
    private Path tmpDir;
    private Path propsFile;

    @Before
    public void setup() throws IOException {
        tmpDir = Files.createTempDirectory("pf4j-test");
        tmpDir.toFile().deleteOnExit();
        zipFile = tmpDir.resolve("my.zip").toAbsolutePath();
        propsFile = tmpDir.resolve("plugin.properties");
        URI file = URI.create("jar:file:"+zipFile.toString());
        try (FileSystem zipfs = FileSystems.newFileSystem(file, Collections.singletonMap("create", "true"))) {
            Path propsInZip = zipfs.getPath("/plugin.properties");
            BufferedWriter br = new BufferedWriter(new FileWriter(propsFile.toString()));
            br.write("plugin.id=test");
            br.newLine();
            br.write("plugin.version=1.2.3");
            br.newLine();
            br.write("plugin.class=foo.bar");
            br.close();
            Files.move(propsFile, propsInZip);
        }
    }

    @Test
    public void expandIfZip() throws Exception {
        Path unzipped = FileUtils.expandIfZip(zipFile);
        assertEquals(tmpDir.resolve("my"), unzipped);
        assertTrue(Files.exists(tmpDir.resolve("my/plugin.properties")));

        // Non-zip file remains unchanged
        assertEquals(propsFile, FileUtils.expandIfZip(propsFile));
    }

}
