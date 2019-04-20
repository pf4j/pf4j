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
import org.pf4j.util.NameFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class DefaultPluginRepository extends BasePluginRepository {

    private static final Logger log = LoggerFactory.getLogger(DefaultPluginRepository.class);

    public DefaultPluginRepository(Path pluginsRoot, boolean development) {
        super(pluginsRoot);

        AndFileFilter pluginsFilter = new AndFileFilter(new DirectoryFileFilter());
        pluginsFilter.addFileFilter(new NotFileFilter(createHiddenPluginFilter(development)));
        setFilter(pluginsFilter);
    }

    @Override
    public List<Path> getPluginsPaths() {
        extractZipFiles();
        return super.getPluginsPaths();
    }

    @Override
    public boolean deletePluginPath(Path pluginPath) throws PluginException {
        FileUtils.optimisticDelete(FileUtils.findWithEnding(pluginPath, ".zip", ".ZIP", ".Zip"));
        return super.deletePluginPath(pluginPath);
    }

    protected FileFilter createHiddenPluginFilter(boolean development) {
        OrFileFilter hiddenPluginFilter = new OrFileFilter(new HiddenFilter());

        if (development) {
            // skip default build output folders since these will cause errors in the logs
            hiddenPluginFilter
                .addFileFilter(new NameFileFilter("target")) // MAVEN
                .addFileFilter(new NameFileFilter("build")); // GRADLE
        }

        return hiddenPluginFilter;
    }

    private void extractZipFiles() {
        // expand plugins zip files
        File[] zipFiles = pluginsRoot.toFile().listFiles(new ZipFileFilter());
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

}
