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

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks that the packaged jar keeps the layout that JPMS needs, so that consumers can write
 * {@code requires org.pf4j;}.
 * <p>
 * This runs against the built artifact and not against {@code target/classes}, because that is what
 * gets published. Both conditions below are needed. A jar that carries the module descriptor without
 * the {@code Multi-Release} attribute is not read as a multi-release jar, and the module stays invisible.
 * <p>
 * Release 3.15.0 shipped without the descriptor, see #648. It was not caught by the build, because
 * {@code module-info.java} had been deleted and the compiler execution that handles it silently
 * compiled nothing.
 *
 * @author Decebal Suiu
 */
class MultiReleaseJarIT {

    private static final String MODULE_INFO = "META-INF/versions/9/module-info.class";

    @Test
    void jarContainsModuleDescriptor() throws Exception {
        try (JarFile jar = new JarFile(getJar())) {
            JarEntry entry = jar.getJarEntry(MODULE_INFO);
            assertNotNull(entry, MODULE_INFO + " is missing from the jar");
            assertTrue(entry.getSize() > 0, MODULE_INFO + " is empty");
        }
    }

    @Test
    void jarIsMarkedAsMultiRelease() throws Exception {
        try (JarFile jar = new JarFile(getJar())) {
            Attributes attributes = jar.getManifest().getMainAttributes();
            assertEquals("true", attributes.getValue("Multi-Release"),
                "the Multi-Release manifest attribute is missing or not true");
        }
    }

    private File getJar() {
        String path = System.getProperty("pf4j.jar");
        assertNotNull(path, "the pf4j.jar system property is not set");

        File jar = new File(path);
        assertTrue(jar.isFile(), "the jar was not found at " + path);

        return jar;
    }

}
