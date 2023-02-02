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

import com.google.common.io.ByteStreams;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Decebal Suiu
 */
public class JavaFileObjectUtils {

    private JavaFileObjectUtils() {}

    public static String getClassName(JavaFileObject object) {
        if (object.getKind() != JavaFileObject.Kind.CLASS) {
            throw new IllegalStateException("Only Kind.CLASS is supported");
        }

        String name = object.getName();
        // Remove "/CLASS_OUT/" from head and ".class" from tail
        name = name.substring(14, name.length() - 6);
        name = name.replace('/', '.');

        return name;
    }

    public static byte[] getAllBytes(JavaFileObject object) {
        try (InputStream in = object.openInputStream()) {
            return ByteStreams.toByteArray(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
