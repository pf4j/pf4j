/*
 * Copyright 2012 Decebal Suiu
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
package ro.fortsoft.pf4j.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Decebal Suiu
 */
public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    private static final List<String> ZIP_EXTENSIONS = Arrays.asList(".zip", ".ZIP", ".Zip");

    public static List<String> readLines(File file, boolean ignoreComments) throws IOException {
		if (!file.exists() || !file.isFile()) {
			return new ArrayList<>();
		}

		List<String> lines = new ArrayList<>();

		BufferedReader reader = null;
		try {
	        reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				if (ignoreComments && !line.startsWith("#") && !lines.contains(line)) {
					lines.add(line);
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return lines;
	}

    public static void writeLines(Collection<String> lines, File file) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
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

	public static List<File> getJars(Path folder) {
	    List<File> bucket = new ArrayList<>();
	    getJars(bucket, folder);

	    return bucket;
    }

    private static void getJars(final List<File> bucket, Path folder) {
        FileFilter jarFilter = new JarFileFilter();
        FileFilter directoryFilter = new DirectoryFileFilter();

        if (Files.exists(folder) && Files.isDirectory(folder)) {
            File[] jars = folder.toFile().listFiles(jarFilter);
            for (int i = 0; (jars != null) && (i < jars.length); ++i) {
                bucket.add(jars[i]);
            }

            File[] directories = folder.toFile().listFiles(directoryFilter);
            for (int i = 0; (directories != null) && (i < directories.length); ++i) {
                File directory = directories[i];
                getJars(bucket, directory.toPath());
            }
        }
    }

    /**
     * Finds a path with various endings or null if not found
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
     * Delete a file (not recursively) and ignore any errors
     * @param path the path to delete
     */
    public static void optimisticDelete(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.delete(path);
        } catch (IOException ignored) { }
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
        String fileName = filePath.getFileName().toString();
        if (!ZIP_EXTENSIONS.contains(fileName.substring(fileName.lastIndexOf(".")))) {
            return filePath;
        }
        FileTime pluginZipDate = Files.getLastModifiedTime(filePath);
        Path pluginDirectory = filePath.resolveSibling(fileName.substring(0, fileName.lastIndexOf(".")));

        // check if exists root or the '.zip' file is "newer" than root
        if (!Files.exists(pluginDirectory) || pluginZipDate.compareTo(Files.getLastModifiedTime(pluginDirectory)) > 0) {
            log.debug("Expand plugin zip '{}' in '{}'", filePath, pluginDirectory);

            // do not overwrite an old version, remove it
            if (Files.exists(pluginDirectory)) {
                FileUtils.delete(pluginDirectory);
            }

            // create root for plugin
            Files.createDirectories(pluginDirectory);

            // expand '.zip' file
            Unzip unzip = new Unzip();
            unzip.setSource(filePath.toFile());
            unzip.setDestination(pluginDirectory.toFile());
            unzip.extract();
        }
        log.debug("Expanded {} to {}", filePath, pluginDirectory);

        return pluginDirectory;
    }
}
