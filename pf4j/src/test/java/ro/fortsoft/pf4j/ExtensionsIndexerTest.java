/*
 * Copyright 2015 Mario Franco.
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
package ro.fortsoft.pf4j;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Mario Franco
 */
public class ExtensionsIndexerTest {

    public ExtensionsIndexerTest() {
    }

    /**
     * Test of getSupportedAnnotationTypes method, of class ExtensionsIndexer.
     */
    @Test
    public void testGetSupportedAnnotationTypes() {
        ExtensionsIndexer instance = new ExtensionsIndexer();
        Set<String> result = instance.getSupportedAnnotationTypes();
        assertEquals(1, result.size());
        assertTrue(result.contains(Extension.class.getName()));
    }

    /**
     * Test of readIndex method, of class ExtensionsIndexer.
     */
    @Test
    public void testReadIndex() throws Exception {
        Reader reader = new StringReader(
                "ro.fortsoft.pf4j.demo.hello.HelloPlugin$HelloGreeting\n"
                + "ro.fortsoft.pf4j.demo.welcome.WelcomePlugin$WelcomeGreeting\n"
                + "ro.fortsoft.pf4j.demo.welcome.OtherGreeting\n");
        Set<String> entries = new HashSet<>();
        ExtensionsIndexer.readIndex(reader, entries);
        assertEquals(3, entries.size());
    }

}
