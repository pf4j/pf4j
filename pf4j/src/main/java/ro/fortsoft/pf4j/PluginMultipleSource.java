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
import java.util.List;

/**
 * @author Decebal Suiu
 * @author Mário Franco
 */
public class PluginMultipleSource implements PluginSource {

    private final PluginSource[] sources;

    public PluginMultipleSource(PluginSource... sources) {
        this.sources = sources;
    }

    @Override
    public List<File> getPluginArchives(FileFilter filter) {
        List<File> listFiles = new ArrayList<>();
        for (PluginSource source : sources) {
            listFiles.addAll(source.getPluginArchives(filter));
        }
        return listFiles;
    }

    @Override
    public boolean deletePluginArchive(PluginWrapper pluginWrapper, FileFilter filter) {
        for (PluginSource source : sources) {
            if (source.deletePluginArchive(pluginWrapper, filter)) {
                return true;
            }
        }
        return false;
    }

}
