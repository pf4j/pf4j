/*
 * Copyright 2015 Decebal Suiu
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

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultPluginManagerTest {

    private PluginDescriptor pd1 = null;
    private DefaultPluginManager pluginManager = new DefaultPluginManager();
    private PluginWrapper pw1;

    @Before
    public void init() throws IOException {
        pd1 = new PluginDescriptor();
        pd1.setPluginId("myPlugin");
        pd1.setPluginVersion("1.2.3");
        pd1.setPluginClass("foo");
        pd1.setPluginDescription("My plugin");
        pd1.setDependencies("bar, baz");
        pd1.setProvider("Me");
        pd1.setRequires("5.0.0");

        pw1 = new PluginWrapper(pluginManager, pd1, Files.createTempDirectory("test"), getClass().getClassLoader());
    }

    @Test
    public void validateOK() throws PluginException {
        pluginManager.validatePluginDescriptor(pd1);
    }

    @Test(expected = PluginException.class)
    public void validateFailsOnId() throws PluginException {
        pd1.setPluginId("");
        pluginManager.validatePluginDescriptor(pd1);
    }

    @Test(expected = PluginException.class)
    public void validateFailsOnVersion() throws PluginException {
        pd1.setPluginVersion(null);
        pluginManager.validatePluginDescriptor(pd1);
    }

    @Test(expected = PluginException.class)
    public void validateFailsOnClass() throws PluginException {
        pd1.setPluginClass(null);
        pluginManager.validatePluginDescriptor(pd1);
    }

    @Test
    public void isPluginValid() throws Exception {
        // By default accept all since system version not given
        assertTrue(pluginManager.isPluginValid(pw1));

        pluginManager.setSystemVersion("1.0.0");
        assertFalse(pluginManager.isPluginValid(pw1));

        pluginManager.setSystemVersion("5.0.0");
        assertTrue(pluginManager.isPluginValid(pw1));

        pluginManager.setSystemVersion("6.0.0");
        assertTrue(pluginManager.isPluginValid(pw1));
    }

    @Test
    public void isPluginValidAllowExact() throws Exception {
        pluginManager.setExactVersionAllowed(true);

        // By default accept all since system version not given
        assertTrue(pluginManager.isPluginValid(pw1));

        pluginManager.setSystemVersion("1.0.0");
        assertFalse(pluginManager.isPluginValid(pw1));

        pluginManager.setSystemVersion("5.0.0");
        assertTrue(pluginManager.isPluginValid(pw1));

        pluginManager.setSystemVersion("6.0.0");
        assertFalse(pluginManager.isPluginValid(pw1));
    }

    @Test
    public void testDefaultExactVersionAllowed() throws Exception {
        assertEquals(false, pluginManager.isExactVersionAllowed());
    }

}
