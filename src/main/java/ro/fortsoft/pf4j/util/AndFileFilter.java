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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This filter providing conditional AND logic across a list of
 * file filters. This filter returns <code>true</code> if all filters in the
 * list return <code>true</code>. Otherwise, it returns <code>false</code>.
 * Checking of the file filter list stops when the first filter returns
 * <code>false</code>.
 *
 * @author Decebal Suiu
 */
public class AndFileFilter implements FileFilter {

    /** The list of file filters. */
    private List<FileFilter> fileFilters;

    public AndFileFilter() {
        this.fileFilters = new ArrayList<FileFilter>();
    }

    public AndFileFilter(List<FileFilter> fileFilters) {
        this.fileFilters = new ArrayList<FileFilter>(fileFilters);
    }

    public void addFileFilter(FileFilter fileFilter) {
        fileFilters.add(fileFilter);
    }

    public List<FileFilter> getFileFilters() {
        return Collections.unmodifiableList(fileFilters);
    }

    public boolean removeFileFilter(FileFilter fileFilter) {
        return fileFilters.remove(fileFilter);
    }

    public void setFileFilters(List<FileFilter> fileFilters) {
        this.fileFilters = new ArrayList<FileFilter>(fileFilters);
    }

    @Override
    public boolean accept(File file) {
        if (this.fileFilters.size() == 0) {
            return false;
        }

        for (Iterator<FileFilter> iter = this.fileFilters.iterator(); iter.hasNext();) {
            FileFilter fileFilter = (FileFilter) iter.next();
            if (!fileFilter.accept(file)) {
                return false;
            }
        }

        return true;
    }

}
