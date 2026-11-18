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
package org.pf4j.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

/**
 * Filter accepts any file ending in extension. The case of the filename is ignored.
 *
 * @author Decebal Suiu
 */
public class ExtensionFileFilter implements FileFilter {

    private String extension;

    public ExtensionFileFilter(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean accept(File file) {
        // perform a case insensitive check.
        // Locale.ROOT because the default locale decides what case means: in a
        // Turkish or Azerbaijani one "i".toUpperCase() is "İ", so "plugin.zip"
        // becomes "PLUGİN.ZİP" and no longer ends with ".ZIP".
        return file.getName().toUpperCase(Locale.ROOT).endsWith(extension.toUpperCase(Locale.ROOT));
    }

}
