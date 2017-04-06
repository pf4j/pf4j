/*
 * Copyright 2017 Decebal Suiu
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

import org.junit.Test;
import ro.fortsoft.pf4j.processor.ExtensionAnnotationProcessor;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mario Franco
 */
public class ExtensionAnnotationProcessorTest {

    /**
     * Test of {@link ExtensionAnnotationProcessor#getSupportedAnnotationTypes()}.
     */
    @Test
    public void testGetSupportedAnnotationTypes() {
        ExtensionAnnotationProcessor instance = new ExtensionAnnotationProcessor();
        Set<String> result = instance.getSupportedAnnotationTypes();
        assertEquals(1, result.size());
        assertTrue(result.contains(Extension.class.getName()));
    }

}
