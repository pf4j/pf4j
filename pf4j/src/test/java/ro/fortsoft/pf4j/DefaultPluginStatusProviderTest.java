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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 *
 * @author Mario Franco
 */
public class DefaultPluginStatusProviderTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    public DefaultPluginStatusProviderTest() {
    }

    @Before
    public void setUp() throws IOException {
        File file = testFolder.newFile("disabled.txt");
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
            writer.write("plugin-2\r\n");
        }
        file.createNewFile();
    }

    private void setUpEnabled() throws IOException {
        File file = testFolder.newFile("enabled.txt");
        file.createNewFile();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
            writer.write("plugin-1\r\n");
            writer.write("plugin-2\r\n");
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isPluginDisabled method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testIsPluginDisabled() throws IOException {
        setUpEnabled();
        DefaultPluginStatusProvider instance = new DefaultPluginStatusProvider(testFolder.getRoot());

        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-2"));
        assertTrue(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of isPluginDisabled method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testIsPluginDisabledWithEnableEmpty() {
        DefaultPluginStatusProvider instance = new DefaultPluginStatusProvider(testFolder.getRoot());

        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of disablePlugin method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testDisablePlugin() throws IOException {
        setUpEnabled();
        DefaultPluginStatusProvider instance = new DefaultPluginStatusProvider(testFolder.getRoot());

        assertTrue(instance.disablePlugin("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-2"));
        assertTrue(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of disablePlugin method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testDisablePluginWithEnableEmpty() {
        DefaultPluginStatusProvider instance = new DefaultPluginStatusProvider(testFolder.getRoot());

        assertTrue(instance.disablePlugin("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-1"));
        assertTrue(instance.isPluginDisabled("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of enablePlugin method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testEnablePlugin() throws IOException {
        setUpEnabled();
        DefaultPluginStatusProvider instance = new DefaultPluginStatusProvider(testFolder.getRoot());

        assertTrue(instance.enablePlugin("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertFalse(instance.isPluginDisabled("plugin-2"));
        assertTrue(instance.isPluginDisabled("plugin-3"));
    }

    /**
     * Test of enablePlugin method, of class DefaultPluginStatusProvider.
     */
    @Test
    public void testEnablePluginWithEnableEmpty() {
        DefaultPluginStatusProvider instance = new DefaultPluginStatusProvider(testFolder.getRoot());

        assertTrue(instance.enablePlugin("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-1"));
        assertFalse(instance.isPluginDisabled("plugin-2"));
        assertFalse(instance.isPluginDisabled("plugin-3"));
    }

}
