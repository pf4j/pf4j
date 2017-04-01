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
package ro.fortsoft.pf4j;

import com.github.zafarkhaja.semver.Version;
import org.junit.Before;
import org.junit.Test;

public class DefaultPluginManagerTest {
    private PluginDescriptor pd1 = null;
    private DefaultPluginManager pluginManager = new DefaultPluginManager();

    @Before
    public void init() {
        pd1 = new PluginDescriptor();
        pd1.setPluginId("myPlugin");
        pd1.setPluginVersion(Version.valueOf("1.2.3"));
        pd1.setPluginClass("foo");
        pd1.setPluginDescription("My plugin");
        pd1.setDependencies("bar, baz");
        pd1.setProvider("Me");
        pd1.setRequires("5.0.0");
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
}
