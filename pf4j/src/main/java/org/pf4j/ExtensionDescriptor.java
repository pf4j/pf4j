/*
 * Copyright 2014 Decebal Suiu
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
package org.pf4j;

/**
 * @author Decebal Suiu
 */
public class ExtensionDescriptor {

    private int ordinal;
    private Class<?> extensionClass;

    public Class<?> getExtensionClass() {
        return extensionClass;
    }

    public int getOrdinal() {
        return ordinal;
    }

    void setExtensionClass(Class<?> extensionClass) {
        this.extensionClass = extensionClass;
    }

    void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

}
