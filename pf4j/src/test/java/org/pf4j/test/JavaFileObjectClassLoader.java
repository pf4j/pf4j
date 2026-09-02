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
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private final Map<String, byte[]> classes = new HashMap<>();

    public Map<String, Class<?>> load(JavaFileObject... objects) {
        return load(Arrays.asList(objects));
    }

    public Map<String, Class<?>> load(List<JavaFileObject> objects) {
        Objects.requireNonNull(objects);

        List<JavaFileObject> mutableObjects = new ArrayList<>(objects);

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

        // Keep the bytes of every class before defining any of them, a class is defined on demand
        for (JavaFileObject object : mutableObjects) {
            classes.put(JavaFileObjectUtils.getClassName(object), JavaFileObjectUtils.getAllBytes(object));
        }

        // Load objects
        Map<String, Class<?>> loadedClasses = new LinkedHashMap<>();
        for (JavaFileObject object : mutableObjects) {
            String className = JavaFileObjectUtils.getClassName(object);
            loadedClasses.put(className, loadGeneratedClass(className));
        }

        return loadedClasses;
    }

    /**
     * Defines a class from the bytes collected by {@link #load}.
     * <p>
     * A class is defined when it is first asked for, either by {@link #load} or by the virtual machine
     * while it resolves the super types of another class, so the order of the objects does not matter.
     *
     * @param name the name of the class
     * @return the loaded class
     * @throws ClassNotFoundException if the class was not given to {@link #load}
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = classes.remove(name);
        if (data == null) {
            throw new ClassNotFoundException(name);
        }

        return defineClass(name, data, 0, data.length);
    }

    private Class<?> loadGeneratedClass(String className) {
        Class<?> loadedClass = findLoadedClass(className);
        if (loadedClass != null) {
            return loadedClass;
        }

        try {
            return findClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

}
