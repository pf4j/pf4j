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

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Get class data from {@link JavaFileObject}.
 * If {@code JavaFileObject} type is {@link JavaFileObject.Kind#SOURCE} them the source is compiled.
 *
 * @author Decebal Suiu
 */
public class JavaFileObjectDataProvider implements ClassDataProvider {

    private final Map<String, JavaFileObject> classes;

    public JavaFileObjectDataProvider(Map<String, JavaFileObject> classes) {
        this.classes = classes;
    }

    public static JavaFileObjectDataProvider of(List<JavaFileObject> objects) {
        List<JavaFileObject> tmp = new ArrayList<>(objects.size());
        for (JavaFileObject object : objects) {
            if (object.getKind() == JavaFileObject.Kind.CLASS) {
                tmp.add(object);
            } else if (object.getKind() == JavaFileObject.Kind.SOURCE) {
                tmp.add(JavaSources.compile(object));
            } else {
                throw new IllegalStateException("Type " + object.getKind() + " is not supported");
            }
        }

        // TODO JavaFileObjectUtils.getClassName() ?!
        Map<String, JavaFileObject> classes = tmp.stream().collect(Collectors.toMap(FileObject::getName, c -> c));

        return new JavaFileObjectDataProvider(classes);
    }

    @Override
    public byte[] getClassData(String className) {
        return JavaFileObjectUtils.getAllBytes(classes.get(className));
    }

}
