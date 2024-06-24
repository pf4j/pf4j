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

import com.github.zafarkhaja.semver.Version;
import com.github.zafarkhaja.semver.expr.Expression;
import org.pf4j.util.StringUtils;

/**
 * Default implementation for {@link VersionManager}.
 * This implementation uses jSemVer (a Java implementation of the SemVer Specification).
 *
 * @author Decebal Suiu
 */
public class DefaultVersionManager implements VersionManager {

    /**
     * Checks if a version satisfies the specified SemVer {@link Expression} string.
     * If the constraint is empty or null then the method returns true.
     * Constraint examples: {@code >2.0.0} (simple), {@code ">=1.4.0 & <1.6.0"} (range).
     * See <a href="https://github.com/zafarkhaja/jsemver#semver-expressions-api-ranges">semver-expressions-api-ranges</a> for more info.
     *
     * @param version  the version to check
     * @param constraint the constraint to check
     * @return {@code true} if the version satisfies the constraint, {@code false} otherwise
     */
    @Override
    public boolean checkVersionConstraint(String version, String constraint) {
        return StringUtils.isNullOrEmpty(constraint) || "*".equals(constraint) || Version.parse(version).satisfies(constraint);
    }

    @Override
    public int compareVersions(String v1, String v2) {
        return Version.parse(v1).compareTo(Version.parse(v2));
    }

}
