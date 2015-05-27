/*
 * Copyright 2012 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pf4j;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ro.fortsoft.pf4j.util.FileUtils;

/**
 * @author Decebal Suiu
 * @author Mário Franco
 */
public class PluginDirectorySource implements PluginSource {

    private final File directory;

    public PluginDirectorySource(File directory) {
        this.directory = directory;
    }

    @Override
    public List<File> getPluginArchives(FileFilter filter) {
        File[] listFiles = directory.listFiles(filter);
        if (listFiles != null) {
            return Arrays.asList(listFiles);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean deletePluginArchive(PluginWrapper pluginWrapper, FileFilter filter) {
        File[] listFiles = directory.listFiles(filter);
        if (listFiles != null) {
            File pluginArchive = null;
            // strip prepended / from the plugin path
            String dirName = pluginWrapper.getPluginPath().substring(1);
            // find the zip file that matches the plugin path
            for (File archive : listFiles) {
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
