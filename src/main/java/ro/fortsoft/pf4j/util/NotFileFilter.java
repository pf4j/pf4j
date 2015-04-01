/*
 * Copyright 2013 Decebal Suiu
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
package ro.fortsoft.pf4j.util;

import java.io.File;
import java.io.FileFilter;

/**
 * This filter produces a logical NOT of the filters specified.
 *
 * @author Decebal Suiu
 */
public class NotFileFilter implements FileFilter {

    private FileFilter filter;

    public NotFileFilter(FileFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean accept(File file) {
        return !filter.accept(file);
    }

}
