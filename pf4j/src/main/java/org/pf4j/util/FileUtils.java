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

import org.pf4j.util.io.JarPathFilter;
import org.pf4j.util.io.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Decebal Suiu
 */
public final class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
        // prevent instantiation
    }

    public static List<String> readLines(Path path, boolean ignoreComments) throws IOException {
        if (!Files.isRegularFile(path)) {
            return new ArrayList<>();
        }

        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (ignoreComments && !line.startsWith("#") && !lines.contains(line)) {
                    lines.add(line);
                }
            }
        }

        return lines;
    }

    public static void writeLines(Collection<String> lines, Path path) throws IOException {
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    /**
     * Delete a file or recursively delete a folder, do not follow symlinks.
     *
     * @param path the file or folder to delete
     * @throws IOException if something goes wrong
     */
    public static void delete(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

           @Override
           public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
               if (!attrs.isSymbolicLink()) {
                   Files.delete(path);
               }

               return FileVisitResult.CONTINUE;
           }

           @Override
           public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
               Files.delete(dir);

               return FileVisitResult.CONTINUE;
           }

        });
    }

    public static List<Path> getJars(Path folder) {
        return findPaths(folder, Integer.MAX_VALUE, new JarPathFilter()).collect(Collectors.toList());
    }

    /**
     * Finds a path with various endings or null if not found.
     *
     * @param basePath the base name
     * @param endings a list of endings to search for
     * @return new path or null if not found
     */
    public static Path findWithEnding(Path basePath, String... endings) {
        for (String ending : endings) {
            Path newPath = basePath.resolveSibling(basePath.getFileName() + ending);
            if (Files.exists(newPath)) {
                return newPath;
            }
        }

        return null;
    }

    /**
     * Delete a file (not recursively) and ignore any errors.
     *
     * @param path the path to delete
     */
    public static void optimisticDelete(Path path) {
        if (path == null) {
            return;
        }

        try {
            Files.delete(path);
        } catch (IOException ignored) {
            // ignored
        }
    }

    /**
     * Unzip a zip file in a directory that has the same name as the zip file.
     * For example if the zip file is {@code my-plugin.zip} then the resulted directory
     * is {@code my-plugin}.
     *
     * @param filePath the file to evaluate
     * @return Path of unzipped folder or original path if this was not a zip file
     * @throws IOException on error
     */
    public static Path expandIfZip(Path filePath) throws IOException {
        if (!isZipFile(filePath)) {
            return filePath;
        }

        FileTime pluginZipDate = Files.getLastModifiedTime(filePath);
        String fileName = filePath.getFileName().toString();
        String directoryName = fileName.substring(0, fileName.lastIndexOf("."));
        Path pluginDirectory = filePath.resolveSibling(directoryName);

        if (!Files.exists(pluginDirectory) || pluginZipDate.compareTo(Files.getLastModifiedTime(pluginDirectory)) > 0) {
            // expand '.zip' file
            Unzip unzip = new Unzip();
            unzip.setSource(filePath);
            unzip.setDestination(pluginDirectory);
            unzip.extract();
            log.info("Expanded plugin zip '{}' in '{}'", filePath.getFileName(), pluginDirectory.getFileName());
        }

        return pluginDirectory;
    }

    /**
     * Return true only if path is a zip file.
     *
     * @param path to a file/dir
     * @return true if file with {@code .zip} ending
     */
    public static boolean isZipFile(Path path) {
        return Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".zip");
    }

    /**
     * Return true only if path is a jar file.
     *
     * @param path to a file/dir
     * @return true if file with {@code .jar} ending
     */
    public static boolean isJarFile(Path path) {
        return Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".jar");
    }

    /**
     * Return true only if path is a jar or zip file.
     *
     * @param path to a file/dir
     * @return true if file ending in {@code .zip} or {@code .jar}
     */
    public static boolean isZipOrJarFile(Path path) {
        return isZipFile(path) || isJarFile(path);
    }

    public static Path getPath(Path path, String first, String... more) throws IOException {
        URI uri = path.toUri();
        if (isZipOrJarFile(path)) {
            String pathString = path.toAbsolutePath().toString();
            // transformation for Windows OS
            pathString = StringUtils.addStart(pathString.replace("\\", "/"), "/");
            // space is replaced with %20
            pathString = pathString.replace(" ","%20");
            uri = URI.create("jar:file:" + pathString);
        }

        return getPath(uri, first, more);
    }

    public static Path getPath(URI uri, String first, String... more) throws IOException {
        return getFileSystem(uri).getPath(first, more);
    }

    /**
     * Quietly close a path.
     */
    public static void closePath(Path path) {
        if (path != null) {
            try {
                path.getFileSystem().close();
            } catch (Exception e) {
                // close silently
            }
        }
    }

    public static Stream<Path> findPaths(Path path, PathFilter filter) {
        return findPaths(path, Integer.MAX_VALUE, filter);
    }

    public static Stream<Path> findPaths(Path path, int maxDepth, PathFilter filter) {
        if (Files.notExists(path)) {
            return Stream.empty();
        }

        try {
            return Files.walk(path, maxDepth)
                .filter(p -> !p.equals(path))
                .filter(filter::accept);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static FileSystem getFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            return FileSystems.newFileSystem(uri, Collections.<String, String>emptyMap());
        }
    }

}
