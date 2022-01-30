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
package org.pf4j.util.io;

import java.nio.file.Path;

/**
 * Filter accepts any path with this name.
 * By default, the case of the filename is ignored.
 *
 * @author Decebal Suiu
 */
public class NamePathFilter implements PathFilter {

    private final String name;
    private final boolean ignoreCase;

    public NamePathFilter(String name) {
        this(name, true);
    }

    public NamePathFilter(String name, boolean ignoreCase) {
        this.name = name;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean accept(Path path) {
        String fileName = path.getFileName().toString();
        if (ignoreCase) {
            return fileName.equalsIgnoreCase(name);
        }

        return fileName.equals(name);
    }

}
