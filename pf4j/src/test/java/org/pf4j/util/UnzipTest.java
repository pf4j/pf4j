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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UnzipTest {

    @Test
    public void zipSlip() throws IOException {
        File zipFile = createMaliciousZipFile();
        Path destination = Files.createTempDirectory("zipSlip");

        Unzip unzip = new Unzip();
        unzip.setSource(zipFile);
        unzip.setDestination(destination.toFile());

        Exception exception = assertThrows(ZipException.class, unzip::extract);
        assertTrue(exception.getMessage().contains("is trying to leave the target output directory"));
    }

    private File createMaliciousZipFile() throws IOException {
        File zipFile = File.createTempFile("malicious", ".zip");
        String maliciousFileName = "../malicious.sh";
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry(maliciousFileName);
            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write("Malicious content".getBytes());
            zipOutputStream.closeEntry();
        }

        return zipFile;
    }

}
