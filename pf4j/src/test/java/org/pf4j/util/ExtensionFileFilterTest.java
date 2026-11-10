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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtensionFileFilterTest {

    @TempDir
    Path folder;

    private final Locale defaultLocale = Locale.getDefault();

    @AfterEach
    public void restoreLocale() {
        Locale.setDefault(defaultLocale);
    }

    private File file(String name) throws Exception {
        return Files.createFile(folder.resolve(name)).toFile();
    }

    @Test
    public void acceptsAnyCaseOfTheExtension() throws Exception {
        ZipFileFilter zip = new ZipFileFilter();
        JarFileFilter jar = new JarFileFilter();

        assertTrue(zip.accept(file("alpha.zip")));
        assertTrue(zip.accept(file("BETA.ZIP")));
        assertTrue(zip.accept(file("Gamma.Zip")));
        assertFalse(zip.accept(file("delta.jar")));

        assertTrue(jar.accept(file("epsilon.jar")));
        assertTrue(jar.accept(file("ZETA.JAR")));
        assertFalse(jar.accept(file("eta.txt")));
    }

    /**
     * The case of a letter is a question the default locale answers. Turkish and
     * Azerbaijani upper case "i" to "İ", so "plugin.zip" becomes "PLUGİN.ZİP"
     * and no longer ends with ".ZIP" - a plugin named the ordinary way is not
     * seen at all.
     */
    @Test
    public void acceptsWhateverTheDefaultLocaleIs() throws Exception {
        File lower = file("alpha.zip");
        File upper = file("BETA.ZIP");
        File mixed = file("Gamma.Zip");
        File jarFile = file("delta.jar");

        for (String tag : new String[]{"en-US", "tr-TR", "az-AZ", "lt-LT"}) {
            Locale.setDefault(Locale.forLanguageTag(tag));

            ZipFileFilter zip = new ZipFileFilter();
            assertTrue(zip.accept(lower), tag);
            assertTrue(zip.accept(upper), tag);
            assertTrue(zip.accept(mixed), tag);
            assertFalse(zip.accept(jarFile), tag);

            assertTrue(new JarFileFilter().accept(jarFile), tag);
        }
    }

}
