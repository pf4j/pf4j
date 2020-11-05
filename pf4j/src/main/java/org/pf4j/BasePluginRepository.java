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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Decebal Suiu
 * @author Mário Franco
 */
public class BasePluginRepository implements PluginRepository {

    protected final List<Path> pluginsRoots;

    protected FileFilter filter;
    protected Comparator<File> comparator;

    public BasePluginRepository(Path... pluginsRoots) {
        this(Arrays.asList(pluginsRoots));
    }

    public BasePluginRepository(List<Path> pluginsRoots) {
        this(pluginsRoots, null);
    }

    public BasePluginRepository(List<Path> pluginsRoots, FileFilter filter) {
        this.pluginsRoots = pluginsRoots;
        this.filter = filter;

        // last modified file is first
        this.comparator = Comparator.comparingLong(File::lastModified);
    }

    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    /**
     * Set a {@link File} {@link Comparator} used to sort the listed files from {@code pluginsRoot}.
     * This comparator is used in {@link #getPluginPaths()} method.
     * By default is used a file comparator that returns the last modified files first.
     * If you don't want a file comparator, then call this method with {@code null}.
     */
    public void setComparator(Comparator<File> comparator) {
        this.comparator = comparator;
    }

    @Override
    public List<Path> getPluginPaths() {
        return pluginsRoots.stream()
            .flatMap(path -> streamFiles(path, filter))
            .sorted(comparator)
            .map(File::toPath)
            .collect(Collectors.toList());
    }

    @Override
    public boolean deletePluginPath(Path pluginPath) {
        if (!filter.accept(pluginPath.toFile())) {
            return false;
        }

        try {
            FileUtils.delete(pluginPath);
            return true;
        } catch (NoSuchFileException e) {
            return false; // Return false on not found to be compatible with previous API (#135)
        } catch (IOException e) {
            throw new PluginRuntimeException(e);
        }
    }

    protected Stream<File> streamFiles(Path directory, FileFilter filter) {
        File[] files = directory.toFile().listFiles(filter);
        return files != null
            ? Arrays.stream(files)
            : Stream.empty();
    }

}
