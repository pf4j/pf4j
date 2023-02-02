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

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link ClassLoader} that loads {@link JavaFileObject.Kind#CLASS}s.
 * If {@code JavaFileObject} type is {@link JavaFileObject.Kind#SOURCE} them the source is compiled.
 *
 * @author Decebal Suiu
 */
public class JavaFileObjectClassLoader extends ClassLoader {

    public Map<String, Class<?>> load(JavaFileObject... objects) {
        return load(Arrays.asList(objects));
    }

    public Map<String, Class<?>> load(List<JavaFileObject> objects) {
        Objects.requireNonNull(objects);

        List<JavaFileObject> mutableObjects = new ArrayList<>(objects);

        // Sort generated ".class" by lastModified field
        mutableObjects.sort(Comparator.comparingLong(JavaFileObject::getLastModified));

        // Compile Java sources (if exists)
        for (int i = 0; i < mutableObjects.size(); i++) {
            JavaFileObject object = mutableObjects.get(i);
            if (object.getKind() == JavaFileObject.Kind.CLASS) {
                continue;
            }

            if (object.getKind() == JavaFileObject.Kind.SOURCE) {
                mutableObjects.set(i, JavaSources.compile(object));
            } else {
                throw new IllegalStateException("Type " + object.getKind() + " is not supported");
            }
        }

        // Load objects
        Map<String, Class<?>> loadedClasses = new HashMap<>();
        for (JavaFileObject object : mutableObjects) {
            String className = JavaFileObjectUtils.getClassName(object);
            byte[] data = JavaFileObjectUtils.getAllBytes(object);
            Class<?> loadedClass = defineClass(className, data, 0, data.length);
            loadedClasses.put(className, loadedClass);
        }

        return loadedClasses;
    }

}
