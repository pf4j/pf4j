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

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompoundPluginRepositoryTest {

    @Test
    void addRepositorySuccessfully() {
        CompoundPluginRepository compoundRepository = new CompoundPluginRepository();
        PluginRepository mockRepository = mock(PluginRepository.class);
        compoundRepository.add(mockRepository);
        assertTrue(compoundRepository.getRepositories().contains(mockRepository));
    }

    @Test
    void addRepositoryWithConditionTrue() {
        CompoundPluginRepository compoundRepository = new CompoundPluginRepository();
        PluginRepository mockRepository = mock(PluginRepository.class);
        compoundRepository.add(mockRepository, () -> true);
        assertTrue(compoundRepository.getRepositories().contains(mockRepository));
    }

    @Test
    void addRepositoryWithConditionFalse() {
        CompoundPluginRepository compoundRepository = new CompoundPluginRepository();
        PluginRepository mockRepository = mock(PluginRepository.class);
        compoundRepository.add(mockRepository, () -> false);
        assertFalse(compoundRepository.getRepositories().contains(mockRepository));
    }

    @Test
    void getPluginPathsFromMultipleRepositories() {
        CompoundPluginRepository compoundRepository = new CompoundPluginRepository();
        PluginRepository mockRepository1 = mock(PluginRepository.class);
        PluginRepository mockRepository2 = mock(PluginRepository.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);
        when(mockRepository1.getPluginPaths()).thenReturn(List.of(path1));
        when(mockRepository2.getPluginPaths()).thenReturn(List.of(path2));
        compoundRepository.add(mockRepository1);
        compoundRepository.add(mockRepository2);
        List<Path> paths = compoundRepository.getPluginPaths();
        assertTrue(paths.contains(path1));
        assertTrue(paths.contains(path2));
    }

    @Test
    void deletePluginPathSuccessfully() {
        CompoundPluginRepository compoundRepository = new CompoundPluginRepository();
        PluginRepository mockRepository = mock(PluginRepository.class);
        Path path = mock(Path.class);
        when(mockRepository.deletePluginPath(path)).thenReturn(true);
        compoundRepository.add(mockRepository);
        assertTrue(compoundRepository.deletePluginPath(path));
    }

    @Test
    void deletePluginPathUnsuccessfully() {
        CompoundPluginRepository compoundRepository = new CompoundPluginRepository();
        PluginRepository mockRepository = mock(PluginRepository.class);
        Path path = mock(Path.class);
        when(mockRepository.deletePluginPath(path)).thenReturn(false);
        compoundRepository.add(mockRepository);
        assertFalse(compoundRepository.deletePluginPath(path));
    }

}
