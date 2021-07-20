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
package org.pf4j.processor;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * It's a storage (database) that persists {@link org.pf4j.Extension}s.
 * The standard operations supported by storage are {@link #read} and {@link #write}.
 * The storage is populated by {@link ExtensionAnnotationProcessor}.
 *
 * @author Decebal Suiu
 */
public abstract class ExtensionStorage {

    private static final Pattern COMMENT = Pattern.compile("#.*");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    protected final ExtensionAnnotationProcessor processor;

    public ExtensionStorage(ExtensionAnnotationProcessor processor) {
        this.processor = processor;
    }

    public abstract Map<String, Set<String>> read();

    public abstract void write(Map<String, Set<String>> extensions);

    /**
     * Helper method.
     */
    protected Filer getFiler() {
        return processor.getProcessingEnvironment().getFiler();
    }

    /**
     * Helper method.
     */
    protected void error(String message, Object... args) {
        processor.error(message, args);
    }

    /**
     * Helper method.
     */
    protected void error(Element element, String message, Object... args) {
        processor.error(element, message, args);
    }

    /**
     * Helper method.
     */
    protected void info(String message, Object... args) {
        processor.info(message, args);
    }

    /**
     * Helper method.
     */
    protected void info(Element element, String message, Object... args) {
        processor.info(element, message, args);
    }

    public static void read(Reader reader, Set<String> entries) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = COMMENT.matcher(line).replaceFirst("");
                line = WHITESPACE.matcher(line).replaceAll("");
                if (line.length() > 0) {
                    entries.add(line);
                }
            }
            reader.close();
        }
    }

}
