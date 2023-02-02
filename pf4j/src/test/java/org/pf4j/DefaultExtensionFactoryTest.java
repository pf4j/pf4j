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

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.test.JavaFileObjectClassLoader;
import org.pf4j.test.JavaSources;
import org.pf4j.test.TestExtension;

import javax.tools.JavaFileObject;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mario Franco
 */
public class DefaultExtensionFactoryTest {

    public static final JavaFileObject FailTestExtension = JavaFileObjects.forSourceLines("FailTestExtension",
        "package test;",
        "import org.pf4j.test.TestExtensionPoint;",
        "import org.pf4j.Extension;",
        "",
        "@Extension",
        "public class FailTestExtension implements TestExtensionPoint {",
        "    public FailTestExtension(String name) {}",
        "",
        "    @Override",
        "    public String saySomething() { return \"I am a fail test extension\";}",
        "}");

    private ExtensionFactory extensionFactory;

    @BeforeEach
    public void setUp() {
        extensionFactory = new DefaultExtensionFactory();
    }

    @AfterEach
    public void tearDown() {
        extensionFactory = null;
    }

    /**
     * Test of create method, of class DefaultExtensionFactory.
     */
    @Test
    public void testCreate() {
        assertNotNull(extensionFactory.create(TestExtension.class));
    }

    /**
     * Test of create method, of class DefaultExtensionFactory.
     */
    @Test
    public void testCreateFailConstructor() {
        JavaFileObject object = JavaSources.compile(FailTestExtension);
        JavaFileObjectClassLoader classLoader = new JavaFileObjectClassLoader();
        Class<?> extensionClass = (Class<?>) classLoader.load(object).values().toArray()[0];
        assertThrows(PluginRuntimeException.class, () -> extensionFactory.create(extensionClass));
    }

}
