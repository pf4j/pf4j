/*
 * Copyright 2015 Decebal Suiu
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
package ro.fortsoft.pf4j.processor;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import java.util.Map;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public abstract class ExtensionStorage {

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

}
