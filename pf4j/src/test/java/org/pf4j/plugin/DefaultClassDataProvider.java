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
package org.pf4j.plugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Get class data from the class path.
 *
 * @author Decebal Suiu
 */
public class DefaultClassDataProvider implements ClassDataProvider {

    @Override
    public byte[] getClassData(String className) {
        String path = className.replace('.', '/') + ".class";
        InputStream classDataStream = getClass().getClassLoader().getResourceAsStream(path);
        if (classDataStream == null) {
            throw new RuntimeException("Cannot find class data");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            copyStream(classDataStream, outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];

        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

}
