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

import org.junit.Test;
import org.pf4j.plugin.FailTestExtension;
import org.pf4j.plugin.TestExtension;

import static org.junit.Assert.*;

/**
 * @author Decebal Suiu
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

}
