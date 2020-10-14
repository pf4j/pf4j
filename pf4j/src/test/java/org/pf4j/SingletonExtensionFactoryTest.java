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
import org.pf4j.plugin.FailTestExtension;
import org.pf4j.plugin.TestExtension;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Decebal Suiu
 * @author Ajith Kumar
 */
public class SingletonExtensionFactoryTest {

    @Test
    public void create() {
        ExtensionFactory extensionFactory = new SingletonExtensionFactory();
        Object extensionOne = extensionFactory.create(TestExtension.class);
        Object extensionTwo = extensionFactory.create(TestExtension.class);
        assertSame(extensionOne, extensionTwo);
    }

    @Test
    public void createNewEachTime() {
        ExtensionFactory extensionFactory = new SingletonExtensionFactory(FailTestExtension.class.getName());
        Object extensionOne = extensionFactory.create(TestExtension.class);
        Object extensionTwo = extensionFactory.create(TestExtension.class);
        assertNotSame(extensionOne, extensionTwo);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void createNewEachTimeFromDifferentClassLoaders() throws Exception {

        ExtensionFactory extensionFactory = new SingletonExtensionFactory();

        // Get classpath locations
        URL[] classpathReferences = getClasspathReferences();

        // Create different classloaders for the classpath references and load classes respectively
        ClassLoader klassLoaderOne = new URLClassLoader(classpathReferences, null);
        Class klassOne = klassLoaderOne.loadClass(TestExtension.class.getName());
        ClassLoader klassLoaderTwo = new URLClassLoader(classpathReferences, null);
        Class klassTwo = klassLoaderTwo.loadClass(TestExtension.class.getName());

        // create instances
        Object instanceOne = extensionFactory.create(klassOne);
        Object instanceTwo = extensionFactory.create(klassTwo);

        // assert that instances not same
        assertNotSame(instanceOne, instanceTwo);

    }

    private URL[] getClasspathReferences() throws MalformedURLException {

        String classpathProperty = System.getProperty("java.class.path");

        String[] classpaths = classpathProperty.split(":");
        URL[] uris = new URL[classpaths.length];

        for (int index = 0; index < classpaths.length; index++) {
            uris[index] = new File(classpaths[index]).toURI().toURL();
        }
        return uris;
    }

}
