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

import java.util.Comparator;

/**
 * Manager responsible for versions of plugins.
 * It's used to check if a version matches a constraint and to compare two versions.
 *
 * @author Decebal Suiu
 */
public interface VersionManager {

    /**
     * Check if a {@code constraint} and a {@code version} match.
     * A possible constrain can be {@code >=1.0.0 & <2.0.0}.
     *
     * @param version the version to check
     * @param constraint the constraint to check
     * @return {@code true} if the version matches the constraint, {@code false} otherwise
     */
    boolean checkVersionConstraint(String version, String constraint);

    /**
     * Compare two versions. It's similar with {@link Comparator#compare(Object, Object)}.
     *
     * @param v1 the first version to compare
     * @param v2 the second version to compare
     */
    int compareVersions(String v1, String v2);

}
