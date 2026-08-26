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

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.pf4j.test.JavaFileObjectUtils;
import org.pf4j.test.JavaSources;

import javax.tools.JavaFileObject;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionVisitorTest {

    @TempDir
    Path tempDir;

    @Test
    void visitAnnotationShouldReturnExtensionAnnotationVisitor() {
        ExtensionInfo extensionInfo = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        ClassVisitor extensionVisitor = new ExtensionVisitor(extensionInfo);

        AnnotationVisitor returnedVisitor = extensionVisitor.visitAnnotation("Lorg/pf4j/Extension;", true);

        assertNotNull(returnedVisitor);
    }

    @Test
    void visitAnnotationShouldReturnSuperVisitorForNonExtensionAnnotation() {
        ExtensionInfo extensionInfo = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        ClassVisitor extensionVisitor = new ExtensionVisitor(extensionInfo);

        AnnotationVisitor returnedVisitor = extensionVisitor.visitAnnotation("Lorg/pf4j/NonExtension;", true);

        assertNull(returnedVisitor);
    }

    /**
     * {@code ordinal} is a scalar attribute, so ASM reports it through {@code visit} and never
     * through {@code visitArray}. See <a href="https://github.com/pf4j/pf4j/issues/670">#670</a>.
     */
    @Test
    void visitShouldHandleOrdinalAttribute() {
        ExtensionInfo extensionInfo = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        ClassVisitor extensionVisitor = new ExtensionVisitor(extensionInfo);

        AnnotationVisitor annotationVisitor = extensionVisitor.visitAnnotation("Lorg/pf4j/Extension;", true);

        annotationVisitor.visit("ordinal", 5);

        assertEquals(5, extensionInfo.getOrdinal());
    }

    @Test
    void visitArrayShouldHandlePluginsAttribute() {
        ExtensionInfo extensionInfo = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        ClassVisitor extensionVisitor = new ExtensionVisitor(extensionInfo);

        AnnotationVisitor annotationVisitor = extensionVisitor.visitAnnotation("Lorg/pf4j/Extension;", true);
        AnnotationVisitor arrayVisitor = annotationVisitor.visitArray("plugins");

        arrayVisitor.visit("key", "plugin1");

        assertTrue(extensionInfo.getPlugins().contains("plugin1"));
    }

    @Test
    void visitArrayShouldHandlePointsAttribute() {
        ExtensionInfo extensionInfo = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        ClassVisitor extensionVisitor = new ExtensionVisitor(extensionInfo);

        AnnotationVisitor annotationVisitor = extensionVisitor.visitAnnotation("Lorg/pf4j/Extension;", true);
        AnnotationVisitor arrayVisitor = annotationVisitor.visitArray("points");

        arrayVisitor.visit("key", Type.getType("Lorg/pf4j/Point;"));

        assertTrue(extensionInfo.getPoints().contains("org.pf4j.Point"));
    }

    /**
     * Reads every attribute of an {@link org.pf4j.Extension} annotation from a class produced by
     * javac, rather than from hand made visitor calls.
     */
    @Test
    void shouldReadAllAttributesFromCompiledExtension() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceLines("OrdinalGreeting",
            "package test;",
            "import org.pf4j.Extension;",
            "",
            "@Extension(ordinal = 5, plugins = {\"alpha\", \"beta\"}, points = {Greeting.class})",
            "public class OrdinalGreeting implements Greeting {",
            "",
            "    @Override",
            "    public String getGreeting() {",
            "        return \"Ordinal\";",
            "    }",
            "",
            "}");

        ClassLoader classLoader = writeClasses(JavaSources.compileAll(JavaSources.GREETING, source));
        ExtensionInfo extensionInfo = ExtensionInfo.load("test.OrdinalGreeting", classLoader);

        assertNotNull(extensionInfo);
        assertEquals(5, extensionInfo.getOrdinal());
        assertEquals(Arrays.asList("alpha", "beta"), extensionInfo.getPlugins());
        assertEquals(Collections.singletonList(JavaSources.GREETING_CLASS_NAME), extensionInfo.getPoints());
    }

    /**
     * Writes the compiled classes to a directory, so that they can be read as resources by
     * {@link ExtensionInfo#load(String, ClassLoader)}.
     */
    private ClassLoader writeClasses(List<JavaFileObject> objects) throws Exception {
        for (JavaFileObject object : objects) {
            if (object.getKind() != JavaFileObject.Kind.CLASS) {
                continue;
            }

            Path classFile = tempDir.resolve(JavaFileObjectUtils.getClassName(object).replace('.', '/') + ".class");
            Files.createDirectories(classFile.getParent());
            Files.write(classFile, JavaFileObjectUtils.getAllBytes(object));
        }

        return new URLClassLoader(new URL[]{tempDir.toUri().toURL()}, null);
    }

}
