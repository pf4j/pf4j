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
import org.pf4j.processor.LegacyExtensionStorage;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Decebal Suiu
 */
public class LegacyExtensionStorageTest {

    /**
     * Test of {@link LegacyExtensionStorage#read(Reader, Set)}.
     */
    @Test
    public void testRead() throws IOException {
        Reader reader = new StringReader(
            "# comment\n"
                + "org.pf4j.demo.hello.HelloPlugin$HelloGreeting\n"
                + "org.pf4j.demo.welcome.WelcomePlugin$WelcomeGreeting\n"
                + "org.pf4j.demo.welcome.OtherGreeting\n");

        Set<String> entries = new HashSet<>();
        LegacyExtensionStorage.read(reader, entries);
        assertEquals(3, entries.size());
    }

}
