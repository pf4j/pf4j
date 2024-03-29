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
package org.pf4j;

/**
 * Describes an extension.
 * The extension is described by the class and the ordinal (the order of the extension).

 *
 * @author Decebal Suiu
 */
public class ExtensionDescriptor {

    public final int ordinal;
    public final Class<?> extensionClass;

    public ExtensionDescriptor(int ordinal, Class<?> extensionClass) {
        this.ordinal = ordinal;
        this.extensionClass = extensionClass;
    }

}
