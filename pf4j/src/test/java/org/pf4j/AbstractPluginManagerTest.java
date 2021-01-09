/*
 * Copyright (C) 2012-present the original author or authors.
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
package org.pf4j;

import org.junit.jupiter.api.Test;
import org.pf4j.test.TestExtension;
import org.pf4j.test.TestExtensionPoint;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Decebal Suiu
 */
public class AbstractPluginManagerTest {

    @Test
    public void getExtensionsByType() {
        AbstractPluginManager pluginManager = mock(AbstractPluginManager.class, CALLS_REAL_METHODS);

        ExtensionFinder extensionFinder = mock(ExtensionFinder.class);
        List<ExtensionWrapper<TestExtensionPoint>> extensionList = new ArrayList<>(1);
        extensionList.add(new ExtensionWrapper<>(new ExtensionDescriptor(0, TestExtension.class), new DefaultExtensionFactory()));
        when(extensionFinder.find(TestExtensionPoint.class)).thenReturn(extensionList);

        pluginManager.extensionFinder = extensionFinder;
        List<TestExtensionPoint> extensions = pluginManager.getExtensions(TestExtensionPoint.class);
        assertEquals(1, extensions.size());
    }

}
