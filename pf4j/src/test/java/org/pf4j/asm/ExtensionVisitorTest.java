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
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionVisitorTest {

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

    @Test
    void visitArrayShouldHandleOrdinalAttribute() {
        ExtensionInfo extensionInfo = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        ClassVisitor extensionVisitor = new ExtensionVisitor(extensionInfo);

        AnnotationVisitor annotationVisitor = extensionVisitor.visitAnnotation("Lorg/pf4j/Extension;", true);
        AnnotationVisitor arrayVisitor = annotationVisitor.visitArray("ordinal");

        arrayVisitor.visit("key", 1);

        assertEquals(1, extensionInfo.getOrdinal());
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

}
