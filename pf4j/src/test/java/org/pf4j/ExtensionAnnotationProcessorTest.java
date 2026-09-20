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
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.processor.ExtensionAnnotationProcessor;
import org.pf4j.processor.IndexedExtensionStorage;
import org.pf4j.processor.ServiceProviderExtensionStorage;
import org.pf4j.test.JavaSources;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mario Franco
 * @author Decebal Suiu
 */
public class ExtensionAnnotationProcessorTest {

    public static final JavaFileObject WhazzupGreeting_NoExtensionPoint = JavaFileObjects.forSourceLines("WhazzupGreeting",
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

    public static final JavaFileObject SpinnakerExtension = JavaFileObjects.forSourceLines("SpinnakerExtension",
        "package test;",
        "",
        "import org.pf4j.Extension;",
        "import java.lang.annotation.Documented;",
        "import java.lang.annotation.ElementType;",
        "import java.lang.annotation.Retention;",
        "import java.lang.annotation.RetentionPolicy;",
        "import java.lang.annotation.Target;",
        "",
        "@Extension",
        "@Retention(RetentionPolicy.RUNTIME)",
        "@Target(ElementType.TYPE)",
        "@Documented",
        "public @interface SpinnakerExtension {",
        "}");

    public static final JavaFileObject WhazzupGreeting_SpinnakerExtension = JavaFileObjects.forSourceLines("WhazzupGreeting",
        "package test;",
        "",
        "@SpinnakerExtension",
        "public class WhazzupGreeting implements Greeting {",
        "   @Override",
        "    public String getGreeting() {",
        "       return \"Whazzup\";",
        "    }",
        "}");

    /**
     * The same as {@link #SpinnakerExtension} but without {@code Extension} annotation.
     */
    public static final JavaFileObject SpinnakerExtension_NoExtension = JavaFileObjects.forSourceLines("SpinnakerExtension",
        "package test;",
        "",
        "import org.pf4j.Extension;",
        "import java.lang.annotation.Documented;",
        "import java.lang.annotation.ElementType;",
        "import java.lang.annotation.Retention;",
        "import java.lang.annotation.RetentionPolicy;",
        "import java.lang.annotation.Target;",
        "",
//        "@Extension",
        "@Retention(RetentionPolicy.RUNTIME)",
        "@Target(ElementType.TYPE)",
        "@Documented",
        "public @interface SpinnakerExtension {",
        "}");

    public static final JavaFileObject BaseGreeting = JavaFileObjects.forSourceLines("BaseGreeting",
        "package test;",
        "import org.pf4j.Extension;",
        "",
        "@Extension",
        "public abstract class BaseGreeting implements Greeting {",
        "}");

    public static final JavaFileObject SpanishGreeting = JavaFileObjects.forSourceLines("SpanishGreeting",
        "package test;",
        "",
        "public class SpanishGreeting extends BaseGreeting {",
        "   @Override",
        "    public String getGreeting() {",
        "       return \"Hola\";",
        "    }",
        "}");

    public static final JavaFileObject GreetingAdapter = JavaFileObjects.forSourceLines("GreetingAdapter",
        "package test;",
        "",
        "public abstract class GreetingAdapter implements Greeting {",
        "   @Override",
        "    public String getGreeting() {",
        "       return \"\";",
        "    }",
        "}");

    public static final JavaFileObject FrenchGreeting = JavaFileObjects.forSourceLines("FrenchGreeting",
        "package test;",
        "import org.pf4j.Extension;",
        "",
        "@Extension",
        "public class FrenchGreeting extends GreetingAdapter {",
        "   @Override",
        "    public String getGreeting() {",
        "       return \"Bonjour\";",
        "    }",
        "}");

    public static final JavaFileObject PlainGreeting = JavaFileObjects.forSourceLines("PlainGreeting",
        "package test;",
        "import org.pf4j.Extension;",
        "import org.pf4j.ExtensionPoint;",
        "",
        "@Extension",
        "public class PlainGreeting implements ExtensionPoint {",
        "}");

    public static final JavaFileObject LoudGreeting = JavaFileObjects.forSourceLines("LoudGreeting",
        "package test;",
        "import org.pf4j.Extension;",
        "",
        "@Extension",
        "public interface LoudGreeting extends Greeting {",
        "}");

    private ExtensionAnnotationProcessor annotationProcessor;

    @BeforeEach
    public void setUp() throws IOException {
        annotationProcessor = new ExtensionAnnotationProcessor();
    }

    @Test
    public void getSupportedAnnotationTypes() {
        Set<String> result = annotationProcessor.getSupportedAnnotationTypes();
        assertEquals(1, result.size());
        assertEquals("*", result.iterator().next());
    }

    @Test
    public void getSupportedOptions() {
        Set<String> result = annotationProcessor.getSupportedOptions();
        assertEquals(2, result.size());
    }

    @Test
    public void options() {
        Compilation compilation = compiler().withOptions("-Ab=2", "-Ac=3")
            .compile(JavaSources.GREETING, JavaSources.WHAZZUP_GREETING);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        Map<String, String> options = new HashMap<>();
        options.put("b", "2");
        options.put("c", "3");
        assertEquals(options, annotationProcessor.getProcessingEnvironment().getOptions());
    }

    @Test
    public void storage() {
        Compilation compilation = compile(JavaSources.GREETING, JavaSources.WHAZZUP_GREETING);
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
        assertEquals(annotationProcessor.getStorage().getClass(), IndexedExtensionStorage.class);
    }

    @Test
    public void compileWithoutError() {
         Compilation compilation = compile(JavaSources.GREETING, JavaSources.WHAZZUP_GREETING);
         assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void compileWithError() {
        Compilation compilation = compile(JavaSources.GREETING, WhazzupGreeting_NoExtensionPoint);
        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("it doesn't implement ExtensionPoint")
            .inFile(WhazzupGreeting_NoExtensionPoint)
            .onLine(5)
            .atColumn(8);
    }

    @Test
    public void getExtensions() {
        Compilation compilation = compile(JavaSources.GREETING, JavaSources.WHAZZUP_GREETING);
        assertThat(compilation).succeededWithoutWarnings();
        Map<String, Set<String>> extensions = new HashMap<>();
        extensions.put(JavaSources.GREETING_CLASS_NAME, new HashSet<>(Collections.singletonList(JavaSources.WHAZZUP_GREETING_CLASS_NAME)));
        assertEquals(extensions, annotationProcessor.getExtensions());
    }

    @Test
    public void compileNestedExtensionAnnotation() {
        Compilation compilation = compile(JavaSources.GREETING, SpinnakerExtension, WhazzupGreeting_SpinnakerExtension);
        assertThat(compilation).succeededWithoutWarnings();
        Map<String, Set<String>> extensions = new HashMap<>();
        extensions.put(JavaSources.GREETING_CLASS_NAME, new HashSet<>(Collections.singletonList(JavaSources.WHAZZUP_GREETING_CLASS_NAME)));
        assertEquals(extensions, annotationProcessor.getExtensions());
    }

    @Test
    public void compileAbstractExtension() {
        Compilation compilation = compile(JavaSources.GREETING, BaseGreeting, SpanishGreeting);
        assertThat(compilation).succeededWithoutWarnings();
        Map<String, Set<String>> extensions = new HashMap<>();
        extensions.put("test.BaseGreeting", new HashSet<>(Collections.singletonList("test.SpanishGreeting")));
        extensions.put(JavaSources.GREETING_CLASS_NAME, new HashSet<>(Collections.singletonList("test.SpanishGreeting")));
        assertEquals(extensions, annotationProcessor.getExtensions());
    }

    @Test
    public void compileInterfaceExtension() {
        Compilation compilation = compile(JavaSources.GREETING, LoudGreeting);
        assertThat(compilation).succeededWithoutWarnings();
        assertEquals(Collections.emptyMap(), annotationProcessor.getExtensions());
    }

    @Test
    public void compileExtensionOfAbstractAdapter() {
        Compilation compilation = compile(JavaSources.GREETING, GreetingAdapter, FrenchGreeting);
        assertThat(compilation).succeededWithoutWarnings();
        Map<String, Set<String>> extensions = new HashMap<>();
        extensions.put("test.GreetingAdapter", new HashSet<>(Collections.singletonList("test.FrenchGreeting")));
        extensions.put(JavaSources.GREETING_CLASS_NAME, new HashSet<>(Collections.singletonList("test.FrenchGreeting")));
        assertEquals(extensions, annotationProcessor.getExtensions());
    }

    @Test
    public void compileExtensionPointItself() {
        Compilation compilation = compile(PlainGreeting);
        assertThat(compilation).succeededWithoutWarnings();
        Map<String, Set<String>> extensions = new HashMap<>();
        extensions.put(ExtensionPoint.class.getName(), new HashSet<>(Collections.singletonList("test.PlainGreeting")));
        assertEquals(extensions, annotationProcessor.getExtensions());
    }

    @Test
    public void indexAnExtensionOnce() throws IOException {
        Compilation compilation = compile(JavaSources.GREETING, GreetingAdapter, FrenchGreeting);
        assertThat(compilation).succeededWithoutWarnings();
        JavaFileObject index = compilation.generatedFile(StandardLocation.CLASS_OUTPUT, IndexedExtensionStorage.EXTENSIONS_RESOURCE).get();
        String[] lines = index.getCharContent(true).toString().split("\\R");
        assertEquals(2, lines.length); // the header and the extension, attributed to two extension points
        assertEquals("test.FrenchGreeting", lines[1]);
    }

    @Test
    public void serviceProviderStorageNamesEachExtensionPoint() throws IOException {
        Compilation compilation = compiler()
            .withOptions("-Apf4j.storageClassName=" + ServiceProviderExtensionStorage.class.getName())
            .compile(JavaSources.GREETING, GreetingAdapter, FrenchGreeting);
        assertThat(compilation).succeededWithoutWarnings();
        assertTrue(readServiceFile(compilation, JavaSources.GREETING_CLASS_NAME).contains("test.FrenchGreeting"));
        assertTrue(readServiceFile(compilation, "test.GreetingAdapter").contains("test.FrenchGreeting"));
    }

    private String readServiceFile(Compilation compilation, String extensionPoint) throws IOException {
        JavaFileObject file = compilation.generatedFile(StandardLocation.CLASS_OUTPUT,
            ServiceProviderExtensionStorage.EXTENSIONS_RESOURCE + "/" + extensionPoint).get();

        return file.getCharContent(true).toString();
    }

    private Compiler compiler() {
        return javac().withProcessors(annotationProcessor);
    }

    private Compilation compile(JavaFileObject... sources) {
        return compiler().compile(sources);
    }

}
