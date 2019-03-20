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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author Mario Franco
 */
public class DefaultExtensionFactoryTest {

    /**
     * Test of create method, of class DefaultExtensionFactory.
     */
    @Test
    public void testCreate() {
        DefaultExtensionFactory instance = new DefaultExtensionFactory();
        Object result = instance.create(TestExtension.class);
        assertNotNull(result);
    }

    /**
     * Test of create method, of class DefaultExtensionFactory.
     */
    @Test
    public void testCreateFailConstructor() {
        DefaultExtensionFactory instance = new DefaultExtensionFactory();
        Object result = instance.create(FailTestExtension.class);
        assertNull(result);
    }

}
