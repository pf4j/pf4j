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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vdurmont.semver4j.SemverException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Wolfram Haussig
 */
public class Semver4jVersionManagerTest {

    private VersionManager versionManager;

    @BeforeEach
    public void init() {
        versionManager = new Semver4jVersionManager();
    }

    @Test
    public void checkVersionConstraint() {
        assertFalse(versionManager.checkVersionConstraint("1.4.3", ">2.0.0")); // simple
        // range
        assertFalse(versionManager.checkVersionConstraint("1.3.9", ">=1.4.0 <1.6.0"));
        assertTrue(versionManager.checkVersionConstraint("1.4.0", ">=1.4.0 <1.6.0"));
        assertTrue(versionManager.checkVersionConstraint("1.4.3", ">=1.4.0 <1.6.0"));
        assertFalse(versionManager.checkVersionConstraint("1.6.0", ">=1.4.0 <1.6.0"));
        assertTrue(versionManager.checkVersionConstraint("undefined", "*"));
    }

    @Test
    public void nullOrEmptyVersion() {
        assertThrows(SemverException.class, () -> versionManager.checkVersionConstraint(null, ">2.0.0"));
    }

    @Test
    public void invalidVersion() {
        assertFalse(versionManager.checkVersionConstraint("1.0", ">2.0.0"));
    }

    @Test
    public void compareVersions() {
        assertTrue(versionManager.compareVersions("1.1.0", "1.0.0") > 0);
    }

    @Test
    public void isStable() {
        assertTrue(versionManager.isStable("1.1.0"));
        assertFalse(versionManager.isStable("1.1.0-SNAPSHOT"));
    }
    @Test
    public void supportsSnapshotVersions() {
        assertTrue(versionManager.compareVersions("1.0.0-SNAPSHOT", "1.0.0-SNAPSHOT") >= 0);
        assertTrue(versionManager.checkVersionConstraint("1.0.0", ">=1.0.0-SNAPSHOT"));
    }

}
