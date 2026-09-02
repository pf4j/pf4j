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
package org.pf4j.test;

import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Decebal Suiu
 */
class JavaFileObjectClassLoaderTest {

    /**
     * {@code WhazzupGreeting} implements {@code Greeting}, so defining it makes the virtual machine
     * ask for the interface. The class loader has to answer that on its own, whatever the order of
     * the objects is.
     */
    @Test
    void loadsClassesInAnyOrder() {
        List<JavaFileObject> generatedFiles = new ArrayList<>(
            JavaSources.compileAll(JavaSources.GREETING, JavaSources.WHAZZUP_GREETING));

        // put the implementation before the interface, whatever the compiler emitted
        generatedFiles.sort(Comparator.comparingInt(
            object -> JavaSources.GREETING_CLASS_NAME.equals(JavaFileObjectUtils.getClassName(object)) ? 1 : 0));

        Map<String, Class<?>> loadedClasses = new JavaFileObjectClassLoader().load(generatedFiles);

        assertEquals(2, loadedClasses.size());

        Class<?> greetingClass = loadedClasses.get(JavaSources.GREETING_CLASS_NAME);
        Class<?> whazzupGreetingClass = loadedClasses.get(JavaSources.WHAZZUP_GREETING_CLASS_NAME);
        assertNotNull(greetingClass);
        assertNotNull(whazzupGreetingClass);
        assertTrue(greetingClass.isAssignableFrom(whazzupGreetingClass));
    }

}
