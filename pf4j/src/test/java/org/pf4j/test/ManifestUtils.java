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

import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestUtils {

    private ManifestUtils() {}

    /**
     * Creates a {@link Manifest} object from the given map.
     */
    public static Manifest createManifest(Map<String, String> map) {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            attributes.put(new Attributes.Name(entry.getKey()), entry.getValue());
        }

        return manifest;
    }

}
