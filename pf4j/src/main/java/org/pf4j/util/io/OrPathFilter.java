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
import java.util.List;

/**
 * This filter providing conditional OR logic across a list of io filters.
 * This filter returns {@code true} if one filter in the list return {@code true}. Otherwise, it returns {@code false}.
 * Checking of the io filter list stops when the first filter returns {@code true}.
 *
 * @author Decebal Suiu
 */
public class OrPathFilter extends CompoundPathFilter {

    public OrPathFilter() {
        super();
    }

    public OrPathFilter(PathFilter... pathFilters) {
        super(pathFilters);
    }

    public OrPathFilter(List<PathFilter> pathFilters) {
        super(pathFilters);
    }

    @Override
    public boolean accept(Path path) {
        if (this.pathFilters.isEmpty()) {
            return true;
        }

        for (PathFilter pathFilter : this.pathFilters) {
            if (pathFilter.accept(path)) {
                return true;
            }
        }

        return false;
    }

}
