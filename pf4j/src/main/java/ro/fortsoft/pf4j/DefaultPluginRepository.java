/*
 * Copyright 2012 Decebal Suiu
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.util.AndFileFilter;
import ro.fortsoft.pf4j.util.DirectoryFileFilter;
import ro.fortsoft.pf4j.util.FileUtils;
import ro.fortsoft.pf4j.util.HiddenFilter;
import ro.fortsoft.pf4j.util.NameFileFilter;
import ro.fortsoft.pf4j.util.NotFileFilter;
import ro.fortsoft.pf4j.util.OrFileFilter;
import ro.fortsoft.pf4j.util.Unzip;
import ro.fortsoft.pf4j.util.ZipFileFilter;

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
    public List<Path> getPluginPaths() {
        // expand plugins zip files
        File[] pluginZips = pluginsRoot.toFile().listFiles(new ZipFileFilter());
        if ((pluginZips != null) && pluginZips.length > 0) {
            for (File pluginZip : pluginZips) {
                try {
                    expandPluginZip(pluginZip);
                } catch (IOException e) {
                    log.error("Cannot expand plugin zip '{}'", pluginZip);
                    log.error(e.getMessage(), e);
                }
            }
        }

        return super.getPluginPaths();
    }

    @Override
    public boolean deletePluginPath(Path pluginPath) {
        // TODO remove plugin zip ?
        return super.deletePluginPath(pluginPath);
    }

    protected FileFilter createHiddenPluginFilter(boolean development) {
        OrFileFilter hiddenPluginFilter = new OrFileFilter(new HiddenFilter());

        if (development) {
            hiddenPluginFilter.addFileFilter(new NameFileFilter("target"));
        }

        return hiddenPluginFilter;
    }

    /**
     * Unzip a plugin zip file in a directory that has the same name as the zip file
     * and it's relative to {@code pluginsRoot}.
     * For example if the zip file is {@code my-plugin.zip} then the resulted directory
     * is {@code my-plugin}.
     *
     * @param pluginZip
     * @return
     * @throws IOException
     */
    private File expandPluginZip(File pluginZip) throws IOException {
        String fileName = pluginZip.getName();
        long pluginZipDate = pluginZip.lastModified();
        String pluginName = fileName.substring(0, fileName.length() - 4);
        File pluginDirectory = pluginsRoot.resolve(pluginName).toFile();
        // check if exists root or the '.zip' file is "newer" than root
        if (!pluginDirectory.exists() || (pluginZipDate > pluginDirectory.lastModified())) {
            log.debug("Expand plugin zip '{}' in '{}'", pluginZip, pluginDirectory);

            // do not overwrite an old version, remove it
            if (pluginDirectory.exists()) {
                FileUtils.delete(pluginDirectory.toPath());
            }

            // create root for plugin
            pluginDirectory.mkdirs();

            // expand '.zip' file
            Unzip unzip = new Unzip();
            unzip.setSource(pluginZip);
            unzip.setDestination(pluginDirectory);
            unzip.extract();
        }

        return pluginDirectory;
    }

}
