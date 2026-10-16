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
package org.pf4j.asm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionInfoTest {

    private static final String PROBE_CLASS_NAME = "ExtensionProbe";

    @TempDir
    Path tempDir;

    @Test
    void loadShouldReturnExtensionInfoWhenClassExists() {
        ExtensionInfo info = ExtensionInfo.load("org.pf4j.asm.ExtensionInfo", this.getClass().getClassLoader());
        assertNotNull(info);
        assertEquals("org.pf4j.asm.ExtensionInfo", info.getClassName());

        // the class has no Extension annotation, which gives an info with the default values
        assertEquals(0, info.getOrdinal());
        assertTrue(info.getPlugins().isEmpty());
        assertTrue(info.getPoints().isEmpty());
    }

    @Test
    void loadShouldReturnNullWhenClassDoesNotExist() {
        ExtensionInfo info = ExtensionInfo.load("non.existent.Class", this.getClass().getClassLoader());
        assertNull(info);
    }

    @Test
    void getClassNameShouldReturnCorrectName() {
        ExtensionInfo info = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        assertEquals("org.pf4j.asm.ExtensionInfo", info.getClassName());
    }

    @Test
    void getOrdinalShouldReturnZeroWhenNotSet() {
        ExtensionInfo info = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        assertEquals(0, info.getOrdinal());
    }

    @Test
    void getPluginsShouldReturnEmptyListWhenNotSet() {
        ExtensionInfo info = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        assertTrue(info.getPlugins().isEmpty());
    }

    @Test
    void getPointsShouldReturnEmptyListWhenNotSet() {
        ExtensionInfo info = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        assertTrue(info.getPoints().isEmpty());
    }

    /**
     * The bundled ASM version must be able to read class files produced by recent Java releases.
     * See <a href="https://github.com/pf4j/pf4j/issues/669">#669</a>.
     */
    @Test
    void loadShouldReadAnnotationFromRecentClassFileVersion() throws Exception {
        ExtensionInfo info = loadProbe(probe(Opcodes.V21, false, false));

        assertNotNull(info);
        assertTrue(info.getPlugins().contains("alpha"));
    }

    /**
     * A class file that cannot be parsed must not abort the whole extension discovery.
     * See <a href="https://github.com/pf4j/pf4j/issues/669">#669</a>.
     */
    @Test
    void loadShouldReturnNullForUnsupportedClassFileVersion() throws Exception {
        byte[] bytes = probe(Opcodes.V21, false, false);
        // pretend the class was compiled by a Java release that ASM does not know yet
        bytes[6] = 0;
        bytes[7] = (byte) 99;

        assertNull(loadProbe(bytes));
    }

    /**
     * A record annotated with {@code @Extension} carries a {@code Record} attribute, which requires
     * at least the ASM8 API level. See <a href="https://github.com/pf4j/pf4j/issues/669">#669</a>.
     */
    @Test
    void loadShouldReadAnnotationFromRecord() throws Exception {
        ExtensionInfo info = loadProbe(probe(Opcodes.V21, true, false));

        assertNotNull(info);
        assertTrue(info.getPlugins().contains("alpha"));
    }

    /**
     * A sealed class annotated with {@code @Extension} carries a {@code PermittedSubclasses}
     * attribute, which requires at least the ASM9 API level.
     * See <a href="https://github.com/pf4j/pf4j/issues/669">#669</a>.
     */
    @Test
    void loadShouldReadAnnotationFromSealedClass() throws Exception {
        ExtensionInfo info = loadProbe(probe(Opcodes.V21, false, true));

        assertNotNull(info);
        assertTrue(info.getPlugins().contains("alpha"));
    }

    /**
     * {@code Extension} is {@code @Inherited}, so a class that only extends an annotated one must get
     * the attributes of its superclass. See <a href="https://github.com/pf4j/pf4j/issues/701">#701</a>.
     */
    @Test
    void loadShouldReadAnnotationInheritedFromSuperclass() throws Exception {
        Path classes = Files.createTempDirectory(tempDir, "inherited");
        writeClass(classes, "Base", "java/lang/Object", 7, "foo");
        writeClass(classes, "Child", "Base", null);
        writeClass(classes, "GrandChild", "Child", null);

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classes.toUri().toURL()}, null)) {
            for (String className : new String[]{"Child", "GrandChild"}) {
                ExtensionInfo info = ExtensionInfo.load(className, classLoader);

                assertNotNull(info);
                assertEquals(className, info.getClassName());
                assertEquals(7, info.getOrdinal());
                assertEquals(Collections.singletonList("foo"), info.getPlugins());
            }
        }
    }

    /**
     * An {@code Extension} annotation on the class itself hides the one of its superclass, as
     * {@code @Inherited} does for core reflection.
     */
    @Test
    void loadShouldPreferOwnAnnotationOverSuperclass() throws Exception {
        Path classes = Files.createTempDirectory(tempDir, "own");
        writeClass(classes, "Base", "java/lang/Object", 7, "foo");
        writeClass(classes, "Child", "Base", 3);

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classes.toUri().toURL()}, null)) {
            ExtensionInfo info = ExtensionInfo.load("Child", classLoader);

            assertNotNull(info);
            assertEquals(3, info.getOrdinal());
            assertTrue(info.getPlugins().isEmpty());
        }
    }

    /**
     * A superclass whose class file cannot be found leaves the defaults instead of failing the class.
     */
    @Test
    void loadShouldKeepDefaultsWhenSuperclassIsMissing() throws Exception {
        Path classes = Files.createTempDirectory(tempDir, "missing");
        writeClass(classes, "Child", "MissingBase", null);

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classes.toUri().toURL()}, null)) {
            ExtensionInfo info = ExtensionInfo.load("Child", classLoader);

            assertNotNull(info);
            assertEquals(0, info.getOrdinal());
            assertTrue(info.getPlugins().isEmpty());
        }
    }

    /**
     * Writes a class extending {@code superName}, annotated with {@code @Extension(ordinal, plugins)}
     * when {@code ordinal} is not null.
     */
    private static void writeClass(Path dir, String name, String superName, Integer ordinal, String... plugins) throws Exception {
        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, name, null, superName, null);

        if (ordinal != null) {
            AnnotationVisitor annotationVisitor = classWriter.visitAnnotation("Lorg/pf4j/Extension;", true);
            annotationVisitor.visit("ordinal", ordinal);
            AnnotationVisitor pluginsVisitor = annotationVisitor.visitArray("plugins");
            for (String plugin : plugins) {
                pluginsVisitor.visit(null, plugin);
            }
            pluginsVisitor.visitEnd();
            annotationVisitor.visitEnd();
        }

        classWriter.visitEnd();

        Files.write(dir.resolve(name + ".class"), classWriter.toByteArray());
    }

    /**
     * Generates a class annotated with {@code @Extension(plugins = "alpha")}. The class is built with
     * ASM rather than compiled, so that class file features newer than the compiler release used for
     * the tests can be covered as well.
     */
    private static byte[] probe(int classFileVersion, boolean record, boolean sealed) {
        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(classFileVersion, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, PROBE_CLASS_NAME,
            null, record ? "java/lang/Record" : "java/lang/Object", null);

        AnnotationVisitor annotationVisitor = classWriter.visitAnnotation("Lorg/pf4j/Extension;", true);
        AnnotationVisitor pluginsVisitor = annotationVisitor.visitArray("plugins");
        pluginsVisitor.visit(null, "alpha");
        pluginsVisitor.visitEnd();
        annotationVisitor.visitEnd();

        if (record) {
            RecordComponentVisitor recordComponentVisitor = classWriter.visitRecordComponent("value", "I", null);
            recordComponentVisitor.visitEnd();
        }
        if (sealed) {
            classWriter.visitPermittedSubclass(PROBE_CLASS_NAME + "Subclass");
        }

        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    private ExtensionInfo loadProbe(byte[] bytes) throws Exception {
        Path classFile = Files.createTempDirectory(tempDir, "probe").resolve(PROBE_CLASS_NAME + ".class");
        Files.write(classFile, bytes);

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classFile.getParent().toUri().toURL()}, null)) {
            return ExtensionInfo.load(PROBE_CLASS_NAME, classLoader);
        }
    }

}
