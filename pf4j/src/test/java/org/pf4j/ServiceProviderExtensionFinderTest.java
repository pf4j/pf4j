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
package org.pf4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceProviderExtensionFinderTest {

    private static final String GREETER_EXTENSION_POINT = "org.pf4j.demo.api.Greeting";
    private static final String HELLO_GREETER_EXTENSION = "org.pf4j.demo.hello.HelloGreeting";
    private static final String WELCOME_GREETER_EXTENSION = "org.pf4j.demo.welcome.WelcomeGreeting";

    @TempDir
    public Path tempDir;

    @Test
    void readClasspathStorages() {
        PluginManager pluginManager = mock(PluginManager.class);
        ServiceProviderExtensionFinder finder = new ServiceProviderExtensionFinder(pluginManager) {

            @Override
            Enumeration<URL> getExtensionResource(ClassLoader classLoader) throws IOException {
                return getExtensionEnumeration();
            }

        };

        Map<String, Set<String>> storages = finder.readClasspathStorages();
        assertNotNull(storages);
        assertTrue(storages.containsKey(null));
        Set<String> extensions = storages.get(null);
        assertEquals(2, extensions.size());
        assertThat(extensions, containsInAnyOrder(HELLO_GREETER_EXTENSION, WELCOME_GREETER_EXTENSION));
    }


    @Test
    void readPluginsStorages() {
        String pluginId = "testPlugin";
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        when(pluginWrapper.getPluginClassLoader()).thenReturn(null); // not needed for this test

        PluginManager pluginManager = mock(PluginManager.class);
        when(pluginManager.getPlugins()).thenReturn(Collections.singletonList(pluginWrapper));
        ServiceProviderExtensionFinder finder = new ServiceProviderExtensionFinder(pluginManager) {

            @Override
            Enumeration<URL> findExtensionResource(PluginClassLoader classLoader) throws IOException {
                return getExtensionEnumeration();
            }

        };

        Map<String, Set<String>> storages = finder.readPluginsStorages();
        assertNotNull(storages);
        assertTrue(storages.containsKey(pluginId));
        Set<String> extensions = storages.get(pluginId);
        assertEquals(2, extensions.size());
        assertThat(extensions, containsInAnyOrder(HELLO_GREETER_EXTENSION, WELCOME_GREETER_EXTENSION));
    }

    private Enumeration<URL> getExtensionEnumeration() throws IOException {
        Path servicesPath = tempDir.resolve("META-INF/services");
        servicesPath.toFile().mkdirs();

        Path greetingPath = servicesPath.resolve(GREETER_EXTENSION_POINT);
        List<String> extensions = new ArrayList<>();
        extensions.add(HELLO_GREETER_EXTENSION);
        extensions.add(WELCOME_GREETER_EXTENSION);
        Files.write(greetingPath, extensions, StandardCharsets.UTF_8);

        return Collections.enumeration(Collections.singletonList(servicesPath.toUri().toURL()));
    }

}
