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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class extracts the content of the plugin zip into a directory.
 * It's a class for only the internal use.
 *
 * @author Decebal Suiu
 */
public class Unzip {

    private static final Logger log = LoggerFactory.getLogger(Unzip.class);

    /**
     * Holds the destination directory.
     * File will be unzipped into the destination directory.
     */
    private Path destination;

    /**
     * Holds path to zip file.
     */
    private Path source;

    public Unzip() {
    }

    public Unzip(Path source, Path destination) {
        this.source = source;
        this.destination = destination;
    }

    public void setSource(Path source) {
        this.source = source;
    }

    public void setDestination(Path destination) {
        this.destination = destination;
    }

    /**
     * Extract the content of zip file ({@code source}) to destination directory.
     * If destination directory already exists it will be deleted before.
     */
    public void extract() throws IOException {
        log.debug("Extract content of '{}' to '{}'", source, destination);

        // delete destination directory if exists
        if (Files.isDirectory(destination)) {
            FileUtils.delete(destination);
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(source))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                Path file = destination.resolve(zipEntry.getName());

                // create intermediary directories - sometimes zip don't add them
                Path dir = file.getParent();

                mkdirsOrThrow(dir);

                if (zipEntry.isDirectory()) {
                    mkdirsOrThrow(file);
                } else {
                    byte[] buffer = new byte[1024];
                    int length;
                    try (OutputStream fos = Files.newOutputStream(file)) {
                        while ((length = zipInputStream.read(buffer)) >= 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }
        }
    }

    private static void mkdirsOrThrow(Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            return;
        }

        Files.createDirectories(dir);
    }

}
