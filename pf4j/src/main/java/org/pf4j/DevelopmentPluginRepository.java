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

import org.pf4j.util.io.AndPathFilter;
import org.pf4j.util.io.DirectoryPathFilter;
import org.pf4j.util.io.HiddenPathFilter;
import org.pf4j.util.io.NamePathFilter;
import org.pf4j.util.io.NotPathFilter;
import org.pf4j.util.io.OrPathFilter;
import org.pf4j.util.io.PathFilter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DevelopmentPluginRepository extends BasePluginRepository {

    public static final String MAVEN_BUILD_DIR = "target";
    public static final String GRADLE_BUILD_DIR = "build";

    public DevelopmentPluginRepository(Path... pluginsRoots) {
        this(Arrays.asList(pluginsRoots));
    }

    public DevelopmentPluginRepository(List<Path> pluginsRoots) {
        super(pluginsRoots);

        AndPathFilter pluginsFilter = new AndPathFilter(new DirectoryPathFilter());
        pluginsFilter.addPathFilter(new NotPathFilter(createHiddenPluginFilter()));
        setFilter(pluginsFilter);
    }

    protected PathFilter createHiddenPluginFilter() {
        OrPathFilter hiddenPluginFilter = new OrPathFilter(new HiddenPathFilter());

        // skip default build output folders since these will cause errors in the logs
        hiddenPluginFilter
            .addPathFilter(new NamePathFilter(MAVEN_BUILD_DIR))
            .addPathFilter(new NamePathFilter(GRADLE_BUILD_DIR));

        return hiddenPluginFilter;
    }

}
