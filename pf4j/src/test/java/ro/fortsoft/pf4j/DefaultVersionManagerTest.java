/*
 * Copyright 2017 Decebal Suiu
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
package ro.fortsoft.pf4j;

import com.github.zafarkhaja.semver.ParseException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Decebal Suiu
 */
public class DefaultVersionManagerTest {

    private VersionManager versionManager;

    @Before
    public void init() {
        versionManager = new DefaultVersionManager();
    }

    @Test
    public void satisfies() {
        assertFalse(versionManager.satisfies(">2.0.0", "1.4.3")); // simple
        assertTrue(versionManager.satisfies(">=1.4.0 & <1.6.0", "1.4.3")); // range
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullOrEmptyVersion() {
        assertFalse(versionManager.satisfies(">2.0.0", null));
    }

    @Test(expected = ParseException.class)
    public void invalidVersion() {
        assertFalse(versionManager.satisfies(">2.0.0", "1.0"));
    }

}
