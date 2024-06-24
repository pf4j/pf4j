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
package org.pf4j.asm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionInfoTest {

    @Test
    void loadShouldReturnExtensionInfoWhenClassExists() {
        ExtensionInfo info = ExtensionInfo.load("org.pf4j.asm.ExtensionInfo", this.getClass().getClassLoader());
        assertNotNull(info);
        assertEquals("org.pf4j.asm.ExtensionInfo", info.getClassName());
    }

    @Test
    void loadShouldReturnNullWhenClassDoesNotExist() {
        ExtensionInfo info = ExtensionInfo.load("non.existent.Class", this.getClass().getClassLoader());
        assertNull(info);
    }

    @Test
    void getClassNameShouldReturnCorrectName() {
        ExtensionInfo info = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        assertEquals("org.pf4j.asm.ExtensionInfo", info.getClassName());
    }

    @Test
    void getOrdinalShouldReturnZeroWhenNotSet() {
        ExtensionInfo info = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        assertEquals(0, info.getOrdinal());
    }

    @Test
    void getPluginsShouldReturnEmptyListWhenNotSet() {
        ExtensionInfo info = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        assertTrue(info.getPlugins().isEmpty());
    }

    @Test
    void getPointsShouldReturnEmptyListWhenNotSet() {
        ExtensionInfo info = new ExtensionInfo("org.pf4j.asm.ExtensionInfo");
        assertTrue(info.getPoints().isEmpty());
    }

}
