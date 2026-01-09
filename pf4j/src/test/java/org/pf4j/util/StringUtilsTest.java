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
package org.pf4j.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    void isNullOrEmpty() {
        assertTrue(StringUtils.isNullOrEmpty(null));
        assertTrue(StringUtils.isNullOrEmpty(""));
        assertFalse(StringUtils.isNullOrEmpty(" "));
        assertFalse(StringUtils.isNullOrEmpty("test"));
    }

    @Test
    void isNotNullOrEmpty() {
        assertFalse(StringUtils.isNotNullOrEmpty(null));
        assertFalse(StringUtils.isNotNullOrEmpty(""));
        assertTrue(StringUtils.isNotNullOrEmpty(" "));
        assertTrue(StringUtils.isNotNullOrEmpty("test"));
    }

    @Test
    void format() {
        assertEquals("Hello World", StringUtils.format("Hello {}", "World"));
        assertEquals("Hello World!", StringUtils.format("Hello {}!", "World"));
        assertEquals("a b c", StringUtils.format("{} {} {}", "a", "b", "c"));
        assertEquals("test", StringUtils.format("test"));
    }

    @Test
    void formatWithMultiplePlaceholders() {
        assertEquals("Plugin 'plugin1' already loaded",
                StringUtils.format("Plugin '{}' already loaded", "plugin1"));
        assertEquals("Plugin 'plugin1' not found in 'path/to/plugin'",
                StringUtils.format("Plugin '{}' not found in '{}'", "plugin1", "path/to/plugin"));
    }

    @Test
    void addStartWithNullOrEmptyAdd() {
        assertEquals("test", StringUtils.addStart("test", null));
        assertEquals("test", StringUtils.addStart("test", ""));
    }

    @Test
    void addStartWithNullOrEmptyStr() {
        assertEquals("prefix", StringUtils.addStart(null, "prefix"));
        assertEquals("prefix", StringUtils.addStart("", "prefix"));
    }

    @Test
    void addStartWhenAlreadyHasPrefix() {
        assertEquals("www.domain.com", StringUtils.addStart("www.domain.com", "www."));
        assertEquals("abc123", StringUtils.addStart("abc123", "abc"));
    }

    @Test
    void addStartWhenNeedsPrefix() {
        assertEquals("www.domain.com", StringUtils.addStart("domain.com", "www."));
        assertEquals("prefix-text", StringUtils.addStart("text", "prefix-"));
    }

}