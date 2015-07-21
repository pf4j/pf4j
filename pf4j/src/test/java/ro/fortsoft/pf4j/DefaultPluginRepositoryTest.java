/*
 * Copyright 2015 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.fortsoft.pf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ro.fortsoft.pf4j.util.ZipFileFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 *
 * @author Mario Franco
 */
public class DefaultPluginRepositoryTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        testFolder.newFile("plugin-1.zip");
        testFolder.newFile("plugin-2.zip");
        testFolder.newFile("plugin-3.zi_");
    }

    /**
     * Test of getPluginArchives method, of class DefaultPluginRepository.
     */
    @Test
    public void testGetPluginArchives() {

        DefaultPluginRepository instance = new DefaultPluginRepository(testFolder.getRoot(), new ZipFileFilter());

        List<File> result = instance.getPluginArchives();

        assertEquals(2, result.size());
        assertEquals(result.get(0).getName(), "plugin-1.zip");
        assertEquals(result.get(1).getName(), "plugin-2.zip");
    }

    /**
     * Test of deletePluginArchive method, of class DefaultPluginRepository.
     */
    @Test
    public void testDeletePluginArchive() {
        DefaultPluginRepository instance = new DefaultPluginRepository(testFolder.getRoot(), new ZipFileFilter());

        assertTrue(instance.deletePluginArchive("/plugin-1"));

        assertFalse(instance.deletePluginArchive("/plugin-3"));

        List<File> result = instance.getPluginArchives();

        assertEquals(1, result.size());
        assertEquals(result.get(0).getName(), "plugin-2.zip");

    }

}
