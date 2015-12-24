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

import ro.fortsoft.pf4j.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Decebal Suiu
 * @author Mário Franco
 */
public class DefaultPluginRepository implements PluginRepository {

    private final File directory;
    private final FileFilter filter;

    public DefaultPluginRepository(File directory, FileFilter filter) {
        this.directory = directory;
        this.filter = filter;
    }

    @Override
    public List<File> getPluginArchives() {
        File[] files = directory.listFiles(filter);

        return (files != null) ? Arrays.asList(files) : Collections.<File>emptyList();
    }

    @Override
    public boolean deletePluginArchive(String pluginPath) {
        File[] files = directory.listFiles(filter);
        if (files != null) {
            File pluginArchive = null;
            // strip prepended "/" from the plugin path
            String dirName = pluginPath.substring(1);
            // find the zip file that matches the plugin path
            for (File archive : files) {
                String name = archive.getName().substring(0, archive.getName().lastIndexOf('.'));
                if (name.equals(dirName)) {
                    pluginArchive = archive;
                    break;
                }
            }
            if (pluginArchive != null && pluginArchive.exists()) {
                return FileUtils.delete(pluginArchive);
            }
        }

        return false;
    }

}
