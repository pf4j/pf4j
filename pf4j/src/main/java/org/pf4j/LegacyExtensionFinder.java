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

import org.pf4j.processor.IndexedExtensionStorage;

/**
 * All extensions declared in a plugin are indexed in a file {@code META-INF/extensions.idx}.
 * This class lookup extensions in all extensions index files {@code META-INF/extensions.idx}.
 *
 * @author Decebal Suiu
 * @deprecated Use {@link IndexedExtensionFinder} instead. This class will be removed in a future release.
 */
@Deprecated
public class LegacyExtensionFinder extends IndexedExtensionFinder {

    public static final String EXTENSIONS_RESOURCE = IndexedExtensionStorage.EXTENSIONS_RESOURCE;

    public LegacyExtensionFinder(PluginManager pluginManager) {
        super(pluginManager);
    }

}
