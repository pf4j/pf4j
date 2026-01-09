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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginClasspathTest {

    @Test
    void testAddClassesDirectoriesVarargs() {
        PluginClasspath classpath = new PluginClasspath();
        classpath.addClassesDirectories("classes1", "classes2");

        assertEquals(2, classpath.getClassesDirectories().size());
        assertTrue(classpath.getClassesDirectories().contains("classes1"));
        assertTrue(classpath.getClassesDirectories().contains("classes2"));
    }

    @Test
    void testAddClassesDirectoriesCollection() {
        PluginClasspath classpath = new PluginClasspath();
        classpath.addClassesDirectories(Arrays.asList("classes1", "classes2"));

        assertEquals(2, classpath.getClassesDirectories().size());
        assertTrue(classpath.getClassesDirectories().contains("classes1"));
        assertTrue(classpath.getClassesDirectories().contains("classes2"));
    }

    @Test
    void testAddJarsDirectoriesVarargs() {
        PluginClasspath classpath = new PluginClasspath();
        classpath.addJarsDirectories("lib1", "lib2");

        assertEquals(2, classpath.getJarsDirectories().size());
        assertTrue(classpath.getJarsDirectories().contains("lib1"));
        assertTrue(classpath.getJarsDirectories().contains("lib2"));
    }

    @Test
    void testAddJarsDirectoriesCollection() {
        PluginClasspath classpath = new PluginClasspath();
        classpath.addJarsDirectories(Arrays.asList("lib1", "lib2"));

        assertEquals(2, classpath.getJarsDirectories().size());
        assertTrue(classpath.getJarsDirectories().contains("lib1"));
        assertTrue(classpath.getJarsDirectories().contains("lib2"));
    }

    @Test
    void testChaining() {
        PluginClasspath classpath = new PluginClasspath()
                .addClassesDirectories("classes")
                .addJarsDirectories("lib");

        assertEquals(1, classpath.getClassesDirectories().size());
        assertEquals(1, classpath.getJarsDirectories().size());
    }

    @Test
    void testEquals() {
        PluginClasspath classpath1 = new PluginClasspath()
                .addClassesDirectories("classes")
                .addJarsDirectories("lib");

        PluginClasspath classpath2 = new PluginClasspath()
                .addClassesDirectories("classes")
                .addJarsDirectories("lib");

        PluginClasspath classpath3 = new PluginClasspath()
                .addClassesDirectories("other");

        assertEquals(classpath1, classpath2);
        assertNotEquals(classpath1, classpath3);
        assertEquals(classpath1, classpath1);
        assertNotEquals(classpath1, null);
        assertNotEquals(classpath1, "string");
    }

    @Test
    void testHashCode() {
        PluginClasspath classpath1 = new PluginClasspath()
                .addClassesDirectories("classes")
                .addJarsDirectories("lib");

        PluginClasspath classpath2 = new PluginClasspath()
                .addClassesDirectories("classes")
                .addJarsDirectories("lib");

        assertEquals(classpath1.hashCode(), classpath2.hashCode());
    }

}