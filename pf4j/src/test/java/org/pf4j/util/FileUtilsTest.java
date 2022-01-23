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
import org.pf4j.util.io.ZipPathFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {

    @TempDir
    Path pluginsPath;

    @Test
    void expandIfZipForZipWithOnlyModuleDescriptor() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
                .pluginVersion("1.2.3")
                .build();

        Path unzipped = FileUtils.expandIfZip(pluginZip.path());
        assertEquals(pluginZip.unzippedPath(), unzipped);
        assertTrue(Files.exists(unzipped.resolve("plugin.properties")));
    }

    @Test
    void expandIfZipForZipWithResourceFile() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-second-plugin-1.2.3.zip"), "myPlugin")
                .pluginVersion("1.2.3")
                .addFile(Paths.get("classes/META-INF/plugin-file"), "plugin")
                .build();

        Path unzipped = FileUtils.expandIfZip(pluginZip.path());
        assertEquals(pluginZip.unzippedPath(), unzipped);
        assertTrue(Files.exists(unzipped.resolve("classes/META-INF/plugin-file")));
    }

    @Test
    void expandIfZipNonZipFiles() throws IOException {
        // File without .suffix
        Path extra = pluginsPath.resolve("extra");
        assertEquals(extra, FileUtils.expandIfZip(extra));

        // Folder
        Path folder = pluginsPath.resolve("folder");
        assertEquals(folder, FileUtils.expandIfZip(folder));
    }

    @Test
    void findPaths() throws IOException {
        PluginZip pluginZip = new PluginZip.Builder(pluginsPath.resolve("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        Path unzipped = FileUtils.expandIfZip(pluginZip.path());
        assertEquals(pluginZip.unzippedPath(), unzipped);

        List<Path> zipPaths = FileUtils.findPaths(pluginsPath, new ZipPathFilter()).collect(Collectors.toList());
        System.out.println("zipPaths = " + zipPaths);
        assertEquals(1, zipPaths.size());
    }

}
