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
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndFileFilterTest {

    @TempDir
    Path tempDir;

    @Test
    void emptyFilterReturnsFalse() {
        AndFileFilter filter = new AndFileFilter();
        File testFile = tempDir.resolve("test.txt").toFile();

        assertFalse(filter.accept(testFile));
    }

    @Test
    void singleFilterReturnsItsResult() {
        FileFilter alwaysTrue = file -> true;
        FileFilter alwaysFalse = file -> false;

        File testFile = tempDir.resolve("test.txt").toFile();

        assertTrue(new AndFileFilter(alwaysTrue).accept(testFile));
        assertFalse(new AndFileFilter(alwaysFalse).accept(testFile));
    }

    @Test
    void allFiltersMustReturnTrue() {
        FileFilter alwaysTrue = file -> true;
        FileFilter alsoTrue = file -> true;

        File testFile = tempDir.resolve("test.txt").toFile();

        AndFileFilter filter = new AndFileFilter(alwaysTrue, alsoTrue);
        assertTrue(filter.accept(testFile));
    }

    @Test
    void returnsFalseIfAnyFilterReturnsFalse() {
        FileFilter alwaysTrue = file -> true;
        FileFilter alwaysFalse = file -> false;

        File testFile = tempDir.resolve("test.txt").toFile();

        AndFileFilter filter = new AndFileFilter(alwaysTrue, alwaysFalse);
        assertFalse(filter.accept(testFile));

        filter = new AndFileFilter(alwaysFalse, alwaysTrue);
        assertFalse(filter.accept(testFile));
    }

    @Test
    void addFileFilter() {
        FileFilter alwaysTrue = file -> true;

        AndFileFilter filter = new AndFileFilter();
        filter.addFileFilter(alwaysTrue);

        File testFile = tempDir.resolve("test.txt").toFile();
        assertTrue(filter.accept(testFile));
    }

    @Test
    void removeFileFilter() {
        FileFilter alwaysTrue = file -> true;
        FileFilter alwaysFalse = file -> false;

        AndFileFilter filter = new AndFileFilter(alwaysTrue, alwaysFalse);
        File testFile = tempDir.resolve("test.txt").toFile();

        assertFalse(filter.accept(testFile));

        assertTrue(filter.removeFileFilter(alwaysFalse));
        assertTrue(filter.accept(testFile));

        assertFalse(filter.removeFileFilter(alwaysFalse));
    }

    @Test
    void setFileFilters() {
        FileFilter alwaysTrue = file -> true;
        FileFilter alwaysFalse = file -> false;

        AndFileFilter filter = new AndFileFilter();
        filter.setFileFilters(Arrays.asList(alwaysTrue, alwaysFalse));

        File testFile = tempDir.resolve("test.txt").toFile();
        assertFalse(filter.accept(testFile));
    }

    @Test
    void getFileFilters() {
        FileFilter filter1 = file -> true;
        FileFilter filter2 = file -> false;

        AndFileFilter andFilter = new AndFileFilter(filter1, filter2);

        assertEquals(2, andFilter.getFileFilters().size());
        assertTrue(andFilter.getFileFilters().contains(filter1));
        assertTrue(andFilter.getFileFilters().contains(filter2));
    }

    @Test
    void constructorWithList() {
        FileFilter filter1 = file -> true;
        FileFilter filter2 = file -> true;

        AndFileFilter andFilter = new AndFileFilter(Arrays.asList(filter1, filter2));

        File testFile = tempDir.resolve("test.txt").toFile();
        assertTrue(andFilter.accept(testFile));
    }

}