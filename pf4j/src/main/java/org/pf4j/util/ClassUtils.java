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
package org.pf4j.util;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Decebal Suiu
 */
public class ClassUtils {

    public static List<String> getAllInterfacesNames(Class<?> aClass) {
        return toString(getAllInterfaces(aClass));
    }

    public static List<Class<?>> getAllInterfaces(Class<?> aClass) {
        List<Class<?>> list = new ArrayList<>();

        while (aClass != null) {
            Class<?>[] interfaces = aClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (!list.contains(anInterface)) {
                    list.add(anInterface);
                }

                List<Class<?>> superInterfaces = getAllInterfaces(anInterface);
                for (Class<?> superInterface : superInterfaces) {
                    if (!list.contains(superInterface)) {
                        list.add(superInterface);
                    }
                }
            }

            aClass = aClass.getSuperclass();
        }

        return list;
    }

    /*
    public static List<String> getAllAbstractClassesNames(Class<?> aClass) {
        return toString(getAllInterfaces(aClass));
    }

    public static List getAllAbstractClasses(Class aClass) {
        List<Class<?>> list = new ArrayList<>();

        Class<?> superclass = aClass.getSuperclass();
        while (superclass != null) {
            if (Modifier.isAbstract(superclass.getModifiers())) {
                list.add(superclass);
            }
            superclass = superclass.getSuperclass();
        }

        return list;
    }
    */

    /**
     * Get a certain annotation of a {@link TypeElement}.
     * See <a href="https://stackoverflow.com/a/10167558">stackoverflow.com</a> for more information.
     *
     * @param typeElement the type element, that contains the requested annotation
     * @param clazz the class of the requested annotation
     * @return the requested annotation or null, if no annotation of the provided class was found on the type element
     * @throws NullPointerException if one of the parameters is null
     */
    public static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
        String clazzName = clazz.getName();
        for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Get a certain parameter of an {@link AnnotationMirror}.
     * See <a href="https://stackoverflow.com/a/10167558">stackoverflow.com</a> for more information.
     *
     * @param annotationMirror the annotation, that contains the requested parameter
     * @param key the name of the requested parameter
     * @return the requested parameter or null, if no parameter of the provided name was found on the annotation
     * @throws NullPointerException if the annotationMirror parameter is null
     */
    public static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Uses {@link Class#getSimpleName()} to convert from {@link Class} to {@link String}.
     *
     * @param classes
     * @return
     */
    private static List<String> toString(List<Class<?>> classes) {
        List<String> list = new ArrayList<>();

        for (Class<?> aClass : classes) {
            list.add(aClass.getSimpleName());
        }

        return list;
    }

}
