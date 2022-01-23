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

import org.pf4j.util.FileUtils;
import org.pf4j.util.io.PathFilter;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Decebal Suiu
 * @author Mário Franco
 */
public class BasePluginRepository implements PluginRepository {

    protected final List<Path> pluginsRoots;

    protected PathFilter filter;
    protected Comparator<Path> comparator;

    public BasePluginRepository(Path... pluginsRoots) {
        this(Arrays.asList(pluginsRoots));
    }

    public BasePluginRepository(List<Path> pluginsRoots) {
        this(pluginsRoots, null);
    }

    public BasePluginRepository(List<Path> pluginsRoots, PathFilter filter) {
        this.pluginsRoots = pluginsRoots;
        this.filter = filter;

        // last modified file is first
        this.comparator = Comparator.comparingLong(path -> {
            try {
                return Files.getLastModifiedTime(path).toMillis();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public void setFilter(PathFilter filter) {
        this.filter = filter;
    }

    /**
     * Set a {@link File} {@link Comparator} used to sort the listed files from {@code pluginsRoot}.
     * This comparator is used in {@link #getPluginPaths()} method.
     * By default, it's used a file comparator that returns the last modified files first.
     * If you don't want a file comparator, then call this method with {@code null}.
     */
    public void setComparator(Comparator<Path> comparator) {
        this.comparator = comparator;
    }

    @Override
    public List<Path> getPluginPaths() {
        return pluginsRoots.stream()
            .flatMap(path -> FileUtils.findPaths(path, 1, filter))
            .sorted(comparator)
            .collect(Collectors.toList());
    }

    @Override
    public boolean deletePluginPath(Path pluginPath) {
        if (!filter.accept(pluginPath)) {
            return false;
        }

        try {
            FileUtils.delete(pluginPath);
            return true;
        } catch (NoSuchFileException e) {
            return false; // Return false on not found to be compatible with previous API (#135)
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
