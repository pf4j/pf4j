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

import org.pf4j.util.AndFileFilter;
import org.pf4j.util.DirectoryFileFilter;
import org.pf4j.util.FileUtils;
import org.pf4j.util.HiddenFilter;
import org.pf4j.util.NotFileFilter;
import org.pf4j.util.OrFileFilter;
import org.pf4j.util.ZipFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A PluginRepository that extracts zip files in the {@code pluginsRoot} before computing the plugin paths.
 * <p>
 * Can optionally take a {@link PluginDescriptorFinder} to enable finding plugins subdirectories of {@code pluginsRoot}.
 * </p>
 *
 * @author Decebal Suiu
 */
public class DefaultPluginRepository extends BasePluginRepository {

    private static final Logger log = LoggerFactory.getLogger(DefaultPluginRepository.class);

    protected final PluginDescriptorFinder pluginDescriptorFinder;

    public DefaultPluginRepository(Path pluginsRoot) {
        this(pluginsRoot, null);
    }

    public DefaultPluginRepository(Path pluginsRoot, PluginDescriptorFinder pluginDescriptorFinder) {
        super(pluginsRoot);
        this.pluginDescriptorFinder = pluginDescriptorFinder;

        AndFileFilter pluginsFilter = new AndFileFilter(new DirectoryFileFilter());
        pluginsFilter.addFileFilter(new NotFileFilter(createHiddenPluginFilter()));
        setFilter(pluginsFilter);
    }

    @Override
    public List<Path> getPluginPaths() {
        List<Path> paths = new ArrayList<>();

        List<Path> nextRoots = Collections.singletonList(pluginsRoot);
        do {
            nextRoots.forEach(this::extractZipFiles);

            List<Path> nextPaths = nextRoots.stream()
                .flatMap(path -> streamFiles(path, filter))
                .sorted(comparator)
                .map(File::toPath)
                .collect(Collectors.toList());

            nextRoots = nextPaths.stream()
                .filter(Files::isDirectory)
                .filter(path -> !isPluginPath(path))
                .collect(Collectors.toList());

            nextPaths.removeAll(nextRoots);
            paths.addAll(nextPaths);
        } while (!nextRoots.isEmpty());

        return paths;
    }

    @Override
    public boolean deletePluginPath(Path pluginPath) {
        FileUtils.optimisticDelete(FileUtils.findWithEnding(pluginPath, ".zip", ".ZIP", ".Zip"));
        return super.deletePluginPath(pluginPath);
    }

    protected FileFilter createHiddenPluginFilter() {
        return new OrFileFilter(new HiddenFilter());
    }

    private void extractZipFiles(Path directory) {
        // expand plugins zip files
        File[] zipFiles = directory.toFile().listFiles(new ZipFileFilter());
        if ((zipFiles != null) && zipFiles.length > 0) {
            for (File pluginZip : zipFiles) {
                try {
                    FileUtils.expandIfZip(pluginZip.toPath());
                } catch (IOException e) {
                    log.error("Cannot expand plugin zip '{}'", pluginZip);
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    protected Stream<File> streamFiles(Path directory, FileFilter filter) {
        File[] files = directory.toFile().listFiles(filter);
        return files != null
            ? Arrays.stream(files)
            : Stream.empty();
    }

    protected boolean isPluginPath(Path path) {
        if (pluginDescriptorFinder == null) {
            return true;
        }

        try {
            pluginDescriptorFinder.find(path);
            return true;
        } catch (Exception e) {
            log.debug("No pluginDescriptor found for path '{}'.", path, e);
            return false;
        }
    }
}
