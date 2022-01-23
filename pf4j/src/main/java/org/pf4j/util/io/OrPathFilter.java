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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This filter providing conditional OR logic across a list of io filters.
 * This filter returns {@code true} if one filter in the list return {@code true}. Otherwise, it returns {@code false}.
 * Checking of the io filter list stops when the first filter returns {@code true}.
 *
 * @author Decebal Suiu
 */
public class OrPathFilter implements PathFilter {

    /** The list of io filters. */
    private List<PathFilter> pathFilters;

    public OrPathFilter() {
        this(new ArrayList<>());
    }

    public OrPathFilter(PathFilter... pathFilters) {
        this(Arrays.asList(pathFilters));
    }

    public OrPathFilter(List<PathFilter> pathFilters) {
        this.pathFilters = new ArrayList<>(pathFilters);
    }

    public OrPathFilter addPathFilter(PathFilter pathFilter) {
        pathFilters.add(pathFilter);

        return this;
    }

    public List<PathFilter> getPathFilters() {
        return Collections.unmodifiableList(pathFilters);
    }

    public boolean removePathFilter(PathFilter pathFilter) {
        return pathFilters.remove(pathFilter);
    }

    public void setPathFilters(List<PathFilter> pathFilters) {
        this.pathFilters = new ArrayList<>(pathFilters);
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
