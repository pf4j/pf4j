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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.test.FailTestExtension;
import org.pf4j.test.TestExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mario Franco
 */
class DefaultExtensionFactoryTest {

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
    void create() {
        assertNotNull(extensionFactory.create(TestExtension.class));
    }

    /**
     * Test of create method, of class DefaultExtensionFactory.
     */
    @Test
    void createFailConstructor() {
        assertThrows(PluginRuntimeException.class, () -> extensionFactory.create(FailTestExtension.class));
    }

}
