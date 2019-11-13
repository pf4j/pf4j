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

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;
import org.pf4j.processor.ExtensionAnnotationProcessor;
import org.pf4j.processor.LegacyExtensionStorage;

import javax.tools.JavaFileObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
public class ExtensionAnnotationProcessorTest {

    private static final JavaFileObject Greeting = JavaFileObjects.forSourceLines(
        "Greeting",
        "package test;",
        "import org.pf4j.ExtensionPoint;",
        "",
        "public interface Greeting extends ExtensionPoint {",
        "   String getGreeting();",
        "}");

    private static final JavaFileObject WhazzupGreeting = JavaFileObjects.forSourceLines(
        "WhazzupGreeting",
        "package test;",
        "import org.pf4j.Extension;",
        "",
        "@Extension",
        "public class WhazzupGreeting implements Greeting {",
        "   @Override",
        "    public String getGreeting() {",
        "       return \"Whazzup\";",
        "    }",
        "}");

    private static final JavaFileObject WhazzupGreeting_No_ExtensionPoint = JavaFileObjects.forSourceLines(
        "WhazzupGreeting",
        "package test;",
        "import org.pf4j.Extension;",
        "",
        "@Extension",
        "public class WhazzupGreeting {",
        "   @Override",
        "    public String getGreeting() {",
        "       return \"Whazzup\";",
        "    }",
        "}");

    @Test
    public void getSupportedAnnotationTypes() {
        ExtensionAnnotationProcessor instance = new ExtensionAnnotationProcessor();
        Set<String> result = instance.getSupportedAnnotationTypes();
        assertEquals(1, result.size());
        assertEquals("*", result.iterator().next());
    }

    @Test
    public void getSupportedOptions() {
        ExtensionAnnotationProcessor instance = new ExtensionAnnotationProcessor();
        Set<String> result = instance.getSupportedOptions();
        assertEquals(2, result.size());
    }

    @Test
    public void options() {
        ExtensionAnnotationProcessor processor = new ExtensionAnnotationProcessor();
        Compilation compilation = javac().withProcessors(processor).withOptions("-Ab=2", "-Ac=3")
            .compile(Greeting, WhazzupGreeting);
        assertEquals(compilation.status(), Compilation.Status.SUCCESS);
        Map<String, String> options = new HashMap<>();
        options.put("b", "2");
        options.put("c", "3");
        assertEquals(options, processor.getProcessingEnvironment().getOptions());
    }

    @Test
    public void storage() {
        ExtensionAnnotationProcessor processor = new ExtensionAnnotationProcessor();
        Compilation compilation = javac().withProcessors(processor).compile(Greeting, WhazzupGreeting);
        assertEquals(compilation.status(), Compilation.Status.SUCCESS);
        assertEquals(processor.getStorage().getClass(), LegacyExtensionStorage.class);
    }

    @Test
    public void compileWithoutError() {
        ExtensionAnnotationProcessor processor = new ExtensionAnnotationProcessor();
         Compilation compilation = javac().withProcessors(processor).compile(Greeting, WhazzupGreeting);
         assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void compileWithError() {
        ExtensionAnnotationProcessor processor = new ExtensionAnnotationProcessor();
        Compilation compilation = javac().withProcessors(processor).compile(Greeting, WhazzupGreeting_No_ExtensionPoint);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("it doesn't implement ExtensionPoint")
            .inFile(WhazzupGreeting_No_ExtensionPoint)
            .onLine(5)
            .atColumn(8);
    }

    @Test
    public void getExtensions() {
        ExtensionAnnotationProcessor processor = new ExtensionAnnotationProcessor();
        Compilation compilation = javac().withProcessors(processor).compile(Greeting, WhazzupGreeting);
        assertThat(compilation).succeededWithoutWarnings();
        Map<String, Set<String>> extensions = new HashMap<>();
        extensions.put("test.Greeting", new HashSet<>(Collections.singletonList("test.WhazzupGreeting")));
        assertEquals(extensions, processor.getExtensions());
    }

}
