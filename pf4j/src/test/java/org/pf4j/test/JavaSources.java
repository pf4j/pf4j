/*
 * Copyright (C) 2012-present the original author or authors.
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
package org.pf4j.test;

import com.google.testing.compile.JavaFileObjects;

import javax.tools.JavaFileObject;
import java.util.List;

import static com.google.testing.compile.Compiler.javac;

/**
 * Keep common Java sources (useful in many tests).
 * For Java 13+ is recommended to use Text Block feature (it's more clear).
 *
 * @author Decebal Suiu
 */
public class JavaSources {

    public static final String GREETING_CLASS_NAME = "test.Greeting";
    public static final JavaFileObject Greeting = JavaFileObjects.forSourceLines("Greeting",
        "package test;",
        "import org.pf4j.ExtensionPoint;",
        "",
        "public interface Greeting extends ExtensionPoint {",
        "   String getGreeting();",
        "}");

    public static final String WHAZZUP_GREETING_CLASS_NAME = "test.WhazzupGreeting";
    public static final JavaFileObject WhazzupGreeting = JavaFileObjects.forSourceLines("WhazzupGreeting",
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

    /**
     * Compile a list of sources using javac compiler.
     */
    public static List<JavaFileObject> compileAll(JavaFileObject... sources) {
        return javac().compile(sources).generatedFiles();
    }

    public static JavaFileObject compile(JavaFileObject source) {
        return compileAll(source).get(0);
    }

}
