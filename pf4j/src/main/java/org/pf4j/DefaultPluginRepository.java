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
import org.pf4j.util.io.AndPathFilter;
import org.pf4j.util.io.DirectoryPathFilter;
import org.pf4j.util.io.HiddenPathFilter;
import org.pf4j.util.io.NotPathFilter;
import org.pf4j.util.io.OrPathFilter;
import org.pf4j.util.io.PathFilter;
import org.pf4j.util.io.ZipPathFilter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DefaultPluginRepository extends BasePluginRepository {

    public DefaultPluginRepository(Path... pluginsRoots) {
        this(Arrays.asList(pluginsRoots));
    }

    public DefaultPluginRepository(List<Path> pluginsRoots) {
        super(pluginsRoots);

        AndPathFilter pluginsFilter = new AndPathFilter(new DirectoryPathFilter());
        pluginsFilter.addPathFilter(new NotPathFilter(createHiddenPluginFilter()));
        setFilter(pluginsFilter);
    }

    @Override
    public List<Path> getPluginPaths() {
        extractZipFiles();
        return super.getPluginPaths();
    }

    @Override
    public boolean deletePluginPath(Path pluginPath) {
        FileUtils.optimisticDelete(FileUtils.findWithEnding(pluginPath, ".zip", ".ZIP", ".Zip"));
        return super.deletePluginPath(pluginPath);
    }

    protected PathFilter createHiddenPluginFilter() {
        return new OrPathFilter(new HiddenPathFilter());
    }

    private void extractZipFiles() {
        // expand plugins zip files
        pluginsRoots.stream()
            .flatMap(path -> FileUtils.findPaths(path, new ZipPathFilter()))
            .forEach(this::expandIfZip);
    }

    private void expandIfZip(Path filePath) {
        try {
            FileUtils.expandIfZip(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot expand plugin zip '" + filePath + "'", e);
        }
    }

}
