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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pf4j.plugin.PluginZip;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class FileUtilsTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void expandIfZip() throws Exception {
        PluginZip pluginZip = new PluginZip.Builder(testFolder.newFile("my-plugin-1.2.3.zip"), "myPlugin")
            .pluginVersion("1.2.3")
            .build();

        Path unzipped = FileUtils.expandIfZip(pluginZip.path());
        assertEquals(pluginZip.unzippedPath(), unzipped);
        assertTrue(Files.exists(unzipped.resolve("plugin.properties")));

        // File without .suffix
        Path extra = testFolder.newFile("extra").toPath();
        assertEquals(extra, FileUtils.expandIfZip(extra));
        // Folder
        Path folder = testFolder.newFile("folder").toPath();
        assertEquals(folder, FileUtils.expandIfZip(folder));
    }

}
