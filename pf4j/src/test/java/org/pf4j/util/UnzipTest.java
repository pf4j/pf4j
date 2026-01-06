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
        assertTrue(exception.getMessage().contains("attempting to write outside the target directory"));
    }

    /**
     * Test for issue #618: Bypass using sibling directory with matching prefix.
     * Attack: If destination is "/tmp/zipSlip", attacker uses "../zipSlip_evil/malicious.sh"
     * The canonical path becomes "/tmp/zipSlip_evil/malicious.sh" which startsWith("/tmp/zipSlip")
     * This demonstrates the vulnerability: the file SHOULD be rejected but validation passes.
     */
    @Test
    public void zipSlipBypasWithSiblingDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory("test");
        Path destination = Files.createDirectory(tempDir.resolve("zipSlip"));
        Path siblingDir = Files.createDirectory(tempDir.resolve("zipSlip_evil"));

        // Create malicious zip that tries to escape to sibling directory
        File zipFile = createZipWithEntry("../zipSlip_evil/malicious.sh");

        Unzip unzip = new Unzip();
        unzip.setSource(zipFile);
        unzip.setDestination(destination.toFile());

        Exception exception = assertThrows(ZipException.class, unzip::extract);
        assertTrue(exception.getMessage().contains("attempting to write outside the target directory"));
    }

    /**
     * Test for issue #623: Bypass using partial prefix match.
     * Attack: If destination is "/tmp/dir", path "/tmp/directory/file" passes startsWith check
     * This demonstrates the vulnerability: the file SHOULD be rejected but validation passes.
     */
    @Test
    public void zipSlipBypassWithPartialPrefixMatch() throws IOException {
        Path tempDir = Files.createTempDirectory("test");
        Path destination = Files.createDirectory(tempDir.resolve("dir"));
        Path similarDir = Files.createDirectory(tempDir.resolve("directory"));

        // Create malicious zip that tries to escape to directory with similar name
        File zipFile = createZipWithEntry("../directory/malicious.sh");

        Unzip unzip = new Unzip();
        unzip.setSource(zipFile);
        unzip.setDestination(destination.toFile());

        Exception exception = assertThrows(ZipException.class, unzip::extract);
        assertTrue(exception.getMessage().contains("attempting to write outside the target directory"));
    }

    /**
     * Positive test: Verify legitimate nested paths work correctly.
     */
    @Test
    public void extractLegitimateNestedPaths() throws IOException {
        Path destination = Files.createTempDirectory("legitimate");
        File zipFile = createZipWithEntry("subdir/nested/file.txt");

        Unzip unzip = new Unzip();
        unzip.setSource(zipFile);
        unzip.setDestination(destination.toFile());

        // Should extract without throwing exception
        unzip.extract();

        Path extractedFile = destination.resolve("subdir/nested/file.txt");
        assertTrue(Files.exists(extractedFile));
    }

    private File createMaliciousZipFile() throws IOException {
        return createZipWithEntry("../malicious.sh");
    }

    private File createZipWithEntry(String entryName) throws IOException {
        File zipFile = File.createTempFile("test", ".zip");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry(entryName);
            zipOutputStream.putNextEntry(entry);
            zipOutputStream.write("Test content".getBytes());
            zipOutputStream.closeEntry();
        }
        return zipFile;
    }

}
